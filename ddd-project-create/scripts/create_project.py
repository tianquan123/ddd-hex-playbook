#!/usr/bin/env python3
from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Callable

from validate_project import ManifestError, load_manifest, substitute, validate_generated_project


EXIT_INPUT = 2
EXIT_TARGET = 3
EXIT_TEMPLATE = 4
EXIT_STATIC = 5
EXIT_BUILD = 6

PROJECT_PATTERN = re.compile(r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
PACKAGE_SEGMENT_PATTERN = re.compile(r"^[A-Za-z_$][A-Za-z0-9_$]*$")
JAVA_KEYWORDS = {
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
    "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
    "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
    "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
    "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
    "_", "exports", "module", "open", "opens", "permits", "provides", "record", "requires", "sealed",
    "to", "transitive", "uses", "var", "when", "with", "yield",
}


class ProjectCreateError(RuntimeError):
    def __init__(self, exit_code: int, message: str) -> None:
        super().__init__(message)
        self.exit_code = exit_code


@dataclass(frozen=True)
class ProjectConfig:
    project_name: str
    group_id: str
    base_package: str | None
    parent_dir: Path
    template_dir: Path
    manifest_path: Path
    platform_name: str = sys.platform


def validate_project_name(value: str) -> str:
    if not PROJECT_PATTERN.fullmatch(value):
        raise ValueError("projectName must contain lowercase letters, digits, and single hyphens")
    return value


def validate_java_package(value: str, field_name: str) -> str:
    segments = value.split(".")
    if not value or any(not segment for segment in segments):
        raise ValueError(f"{field_name} must be a dot-separated Java package")
    for segment in segments:
        if not PACKAGE_SEGMENT_PATTERN.fullmatch(segment) or segment in JAVA_KEYWORDS:
            raise ValueError(f"{field_name} contains an invalid Java identifier: {segment}")
    return value


def to_class_name(project_name: str) -> str:
    validate_project_name(project_name)
    return "".join(part[0].upper() + part[1:] for part in project_name.split("-"))


def package_to_path(base_package: str) -> str:
    return validate_java_package(base_package, "basePackage").replace(".", "/")


def build_command(platform_name: str, project_dir: Path) -> list[str]:
    wrapper = "mvnw.cmd" if platform_name.startswith("win") else "mvnw"
    return [str(project_dir / wrapper), "verify"]


def _replacements(config: ProjectConfig) -> dict[str, str]:
    project_name = validate_project_name(config.project_name)
    group_id = validate_java_package(config.group_id, "groupId")
    base_package = validate_java_package(config.base_package or group_id, "basePackage")
    return {
        "__DDD_PROJECT_NAME__": project_name,
        "__DDD_GROUP_ID__": group_id,
        "__DDD_BASE_PACKAGE__": base_package,
        "__DDD_BASE_PACKAGE_PATH__": package_to_path(base_package),
        "__DDD_PROJECT_CLASS__": to_class_name(project_name),
    }


def _preflight(config: ProjectConfig, manifest: dict[str, Any]) -> tuple[Path, dict[str, str]]:
    replacements = _replacements(config)
    parent = config.parent_dir.expanduser().resolve()
    if not parent.is_dir() or not os.access(parent, os.W_OK):
        raise ProjectCreateError(EXIT_TARGET, f"parent directory does not exist or is not writable: {parent}")
    target = (parent / config.project_name).resolve()
    if target.parent != parent:
        raise ProjectCreateError(EXIT_TARGET, "target must be a direct child of the parent directory")
    if target.exists() and (not target.is_dir() or any(target.iterdir())):
        raise ProjectCreateError(EXIT_TARGET, f"target directory is not empty: {target}")
    if not config.template_dir.is_dir():
        raise ProjectCreateError(EXIT_TEMPLATE, f"template directory is missing: {config.template_dir}")
    symlinks = [path for path in config.template_dir.rglob("*") if path.is_symlink()]
    if symlinks:
        raise ProjectCreateError(EXIT_TEMPLATE, f"template contains a symbolic link: {symlinks[0]}")
    if set(manifest["placeholders"]) != set(replacements):
        raise ProjectCreateError(EXIT_TEMPLATE, "manifest placeholders do not match generator replacements")
    return target, replacements


def _rename_paths(target: Path, replacements: dict[str, str]) -> None:
    paths = sorted(target.rglob("*"), key=lambda path: len(path.relative_to(target).parts), reverse=True)
    for path in paths:
        new_name = substitute(path.name, replacements)
        if new_name != path.name:
            destination = path.parent.joinpath(*new_name.split("/"))
            destination.parent.mkdir(parents=True, exist_ok=True)
            path.rename(destination)


def _replace_text(target: Path, manifest: dict[str, Any], replacements: dict[str, str]) -> None:
    suffixes = set(manifest["text_extensions"])
    filenames = set(manifest["text_filenames"])
    for path in target.rglob("*"):
        if not path.is_file() or (path.suffix not in suffixes and path.name not in filenames):
            continue
        text = path.read_text(encoding="utf-8")
        replaced = substitute(text, replacements)
        if replaced != text:
            path.write_text(replaced, encoding="utf-8", newline="\n")


def _write_failure_log(target: Path, heading: str, details: str) -> None:
    (target / ".ddd-project-create.log").write_text(f"{heading}\n\n{details}", encoding="utf-8")


def create_project(
    config: ProjectConfig,
    runner: Callable[..., subprocess.CompletedProcess[str]] = subprocess.run,
) -> Path:
    try:
        manifest = load_manifest(config.manifest_path)
    except ManifestError as exc:
        raise ProjectCreateError(EXIT_TEMPLATE, str(exc)) from exc
    target, replacements = _preflight(config, manifest)
    try:
        shutil.copytree(
            config.template_dir,
            target,
            dirs_exist_ok=target.exists(),
            symlinks=False,
            ignore=shutil.ignore_patterns("target", ".idea", "*.iml"),
        )
        _rename_paths(target, replacements)
        _replace_text(target, manifest, replacements)
    except (OSError, UnicodeError) as exc:
        if target.exists():
            _write_failure_log(target, "Template generation failed", str(exc))
        raise ProjectCreateError(EXIT_TEMPLATE, f"template generation failed: {exc}") from exc

    errors = validate_generated_project(target, manifest, replacements)
    if errors:
        details = "\n".join(errors)
        _write_failure_log(target, "Static validation failed", details)
        raise ProjectCreateError(EXIT_STATIC, details)

    command = build_command(config.platform_name, target)
    try:
        result = runner(command, cwd=target, capture_output=True, text=True)
    except OSError as exc:
        _write_failure_log(target, "Maven build failed to start", str(exc))
        raise ProjectCreateError(EXIT_BUILD, f"Maven build failed to start: {exc}") from exc
    output = f"COMMAND: {' '.join(command)}\n\nSTDOUT:\n{result.stdout or ''}\nSTDERR:\n{result.stderr or ''}"
    if result.returncode != 0:
        _write_failure_log(target, "Maven build failed", output)
        raise ProjectCreateError(EXIT_BUILD, f"Maven build failed with exit code {result.returncode}")
    log = target / ".ddd-project-create.log"
    if log.exists():
        log.unlink()
    return target


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Create the team-standard DDD project")
    parser.add_argument("--project-name", required=True)
    parser.add_argument("--group-id", required=True)
    parser.add_argument("--base-package")
    parser.add_argument("--parent-dir", type=Path, default=Path.cwd())
    args = parser.parse_args(argv)
    skill_root = Path(__file__).resolve().parents[1]
    config = ProjectConfig(
        project_name=args.project_name,
        group_id=args.group_id,
        base_package=args.base_package,
        parent_dir=args.parent_dir,
        template_dir=skill_root / "assets" / "project-template",
        manifest_path=skill_root / "assets" / "template-manifest.json",
    )
    try:
        project = create_project(config)
    except ValueError as exc:
        print(f"Input validation failed: {exc}", file=sys.stderr)
        return EXIT_INPUT
    except ProjectCreateError as exc:
        print(f"Project creation failed [{exc.exit_code}]: {exc}", file=sys.stderr)
        return exc.exit_code
    base_package = args.base_package or args.group_id
    print("Project creation succeeded")
    print(f"Path: {project}")
    print(f"Project: {args.project_name}")
    print(f"Group ID: {args.group_id}")
    print(f"Base package: {base_package}")
    print("Modules: api, domain, application, infra, trigger, starter")
    print("Static validation: passed")
    print("Maven verify: passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
