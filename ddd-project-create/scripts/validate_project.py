#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Any


REQUIRED_MANIFEST_KEYS = {
    "template_version",
    "placeholders",
    "text_extensions",
    "text_filenames",
    "required_paths",
    "forbidden_tokens",
    "build_commands",
    "module_dependencies",
    "java_rules",
}

MODULES = ("api", "domain", "application", "infra", "trigger", "starter")
PACKAGE_PATTERN = re.compile(r"^\s*package\s+([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\s*;", re.MULTILINE)
IMPORT_PATTERN = re.compile(r"^\s*import\s+(?:static\s+)?([^;]+);", re.MULTILINE)
RECORD_PATTERN = re.compile(r"\brecord\s+[A-Za-z_$][A-Za-z0-9_$]*\s*\(")


class ManifestError(ValueError):
    pass


def load_manifest(path: Path) -> dict[str, Any]:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ManifestError(f"cannot read manifest {path}: {exc}") from exc
    if not isinstance(data, dict):
        raise ManifestError("manifest root must be an object")
    missing = sorted(REQUIRED_MANIFEST_KEYS - data.keys())
    if missing:
        raise ManifestError(f"manifest is missing keys: {', '.join(missing)}")
    placeholders = data["placeholders"]
    if not isinstance(placeholders, list) or not placeholders or not all(isinstance(item, str) for item in placeholders):
        raise ManifestError("placeholders must be a non-empty string list")
    if len(placeholders) != len(set(placeholders)):
        raise ManifestError("placeholders must be unique")
    if not all(item.startswith("__DDD_") and item.endswith("__") for item in placeholders):
        raise ManifestError("every placeholder must use the __DDD_*__ namespace")
    for key in ("text_extensions", "text_filenames", "required_paths", "forbidden_tokens"):
        value = data[key]
        if not isinstance(value, list) or not all(isinstance(item, str) for item in value):
            raise ManifestError(f"{key} must be a string list")
    commands = data["build_commands"]
    if not isinstance(commands, dict) or set(commands) != {"windows", "posix"}:
        raise ManifestError("build_commands must define windows and posix")
    module_dependencies = data["module_dependencies"]
    if not isinstance(module_dependencies, dict) or set(module_dependencies) != set(MODULES):
        raise ManifestError("module_dependencies must define exactly api, domain, application, infra, trigger, starter")
    if any(not isinstance(value, list) or not all(isinstance(item, str) for item in value)
           for value in module_dependencies.values()):
        raise ManifestError("module_dependencies values must be string lists")
    java_rules = data["java_rules"]
    expected_java_rules = {
        "forbidden_imports", "suffix_packages", "forbid_records", "mapstruct_required_fragments"
    }
    if not isinstance(java_rules, dict) or set(java_rules) != expected_java_rules:
        raise ManifestError("java_rules must define forbidden_imports, suffix_packages, forbid_records, and mapstruct_required_fragments")
    if not isinstance(java_rules["forbidden_imports"], dict) or any(
        not isinstance(value, list) or not all(isinstance(item, str) for item in value)
        for value in java_rules["forbidden_imports"].values()
    ):
        raise ManifestError("java_rules.forbidden_imports must map modules to string lists")
    if not isinstance(java_rules["suffix_packages"], dict) or any(
        not isinstance(key, str) or not isinstance(value, str)
        for key, value in java_rules["suffix_packages"].items()
    ):
        raise ManifestError("java_rules.suffix_packages must be a string map")
    if not isinstance(java_rules["forbid_records"], bool):
        raise ManifestError("java_rules.forbid_records must be a boolean")
    fragments = java_rules["mapstruct_required_fragments"]
    if not isinstance(fragments, list) or not all(isinstance(item, str) for item in fragments):
        raise ManifestError("java_rules.mapstruct_required_fragments must be a string list")
    return data


def substitute(value: str, replacements: dict[str, str]) -> str:
    result = value
    for token, replacement in replacements.items():
        result = result.replace(token, replacement)
    return result


def _is_text_file(path: Path, manifest: dict[str, Any]) -> bool:
    return path.suffix in set(manifest["text_extensions"]) or path.name in set(manifest["text_filenames"])


def _module_name(path: Path, project_name: str) -> str | None:
    prefix = f"{project_name}-"
    for part in path.parts:
        if part.startswith(prefix) and part[len(prefix):] in MODULES:
            return part[len(prefix):]
    return None


def _declared_package(text: str) -> str | None:
    match = PACKAGE_PATTERN.search(text)
    return match.group(1) if match else None


def _validate_module_dependencies(
    project_dir: Path,
    manifest: dict[str, Any],
    replacements: dict[str, str],
) -> list[str]:
    errors: list[str] = []
    project_name = replacements["__DDD_PROJECT_NAME__"]
    group_id = replacements["__DDD_GROUP_ID__"]
    namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
    for module, expected in manifest["module_dependencies"].items():
        pom = project_dir / f"{project_name}-{module}" / "pom.xml"
        if not pom.is_file():
            continue
        try:
            root = ET.parse(pom).getroot()
        except (ET.ParseError, OSError) as exc:
            errors.append(f"cannot inspect module dependency for {module}: {exc}")
            continue
        actual: list[str] = []
        for dependency in root.findall("m:dependencies/m:dependency", namespace):
            dependency_group = dependency.findtext("m:groupId", default="", namespaces=namespace)
            artifact = dependency.findtext("m:artifactId", default="", namespaces=namespace)
            prefix = f"{project_name}-"
            if dependency_group == group_id and artifact.startswith(prefix):
                candidate = artifact[len(prefix):]
                if candidate in MODULES:
                    actual.append(candidate)
        if actual != expected:
            errors.append(
                f"module dependency mismatch for {module}: expected {expected}, found {actual}"
            )
    return errors


def _validate_java_source(
    path: Path,
    project_dir: Path,
    manifest: dict[str, Any],
    replacements: dict[str, str],
) -> list[str]:
    errors: list[str] = []
    relative = path.relative_to(project_dir)
    relative_text = relative.as_posix()
    try:
        text = path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return [f"declared Java source is not UTF-8: {relative_text}"]
    source_parts = relative.parts
    java_index = next((index for index, part in enumerate(source_parts) if part == "java"), None)
    declared = _declared_package(text)
    if java_index is not None and java_index < len(source_parts) - 2:
        expected = ".".join(source_parts[java_index + 1:-1])
        if declared != expected:
            errors.append(
                f"package path mismatch in {relative_text}: expected {expected}, found {declared or '<missing>'}"
            )
    project_name = replacements["__DDD_PROJECT_NAME__"]
    module = _module_name(relative, project_name)
    forbidden = manifest["java_rules"]["forbidden_imports"].get(module, [])
    for imported in IMPORT_PATTERN.findall(text):
        if any(token in imported for token in forbidden):
            errors.append(f"forbidden import in {relative_text}: {imported}")
    package_name = declared or ""
    for suffix, required_package in manifest["java_rules"]["suffix_packages"].items():
        if path.stem.endswith(suffix) and not package_name.endswith(required_package):
            errors.append(
                f"misplaced suffix {suffix} in {relative_text}: expected package ending {required_package}"
            )
    if manifest["java_rules"]["forbid_records"] and RECORD_PATTERN.search(text):
        errors.append(f"record declaration is forbidden in {relative_text}")
    if "org.mapstruct.Mapper" in text and "@Mapper" in text:
        missing = [
            fragment for fragment in manifest["java_rules"]["mapstruct_required_fragments"]
            if fragment not in text
        ]
        if missing:
            errors.append(f"MapStruct contract missing in {relative_text}: {', '.join(missing)}")
    return errors


def validate_generated_project(
    project_dir: Path,
    manifest: dict[str, Any],
    replacements: dict[str, str],
) -> list[str]:
    errors: list[str] = []
    missing_replacements = sorted(set(manifest["placeholders"]) - replacements.keys())
    if missing_replacements:
        errors.append(f"missing replacements: {', '.join(missing_replacements)}")

    for raw_path in manifest["required_paths"]:
        relative = Path(substitute(raw_path, replacements))
        if not (project_dir / relative).exists():
            errors.append(f"required path is missing: {relative.as_posix()}")

    forbidden_tokens = tuple(manifest["forbidden_tokens"])
    for path in project_dir.rglob("*"):
        relative = path.relative_to(project_dir).as_posix()
        if "__DDD_" in relative or any(token in relative for token in forbidden_tokens):
            errors.append(f"placeholder remains in path: {relative}")
        if not path.is_file() or not _is_text_file(path, manifest):
            continue
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            errors.append(f"declared text file is not UTF-8: {relative}")
            continue
        if "__DDD_" in text or any(token in text for token in forbidden_tokens):
            errors.append(f"placeholder remains in text: {relative}")

    for raw_path, required_values in manifest.get("required_content", {}).items():
        relative = Path(substitute(raw_path, replacements))
        path = project_dir / relative
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        for raw_value in required_values:
            expected = substitute(raw_value, replacements)
            if expected not in text:
                errors.append(f"required content is missing from {relative.as_posix()}: {expected}")
    errors.extend(_validate_module_dependencies(project_dir, manifest, replacements))
    for path in project_dir.rglob("*.java"):
        errors.extend(_validate_java_source(path, project_dir, manifest, replacements))
    return sorted(set(errors))


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Validate a generated DDD project")
    parser.add_argument("--project-dir", required=True, type=Path)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--project-name", required=True)
    parser.add_argument("--group-id", required=True)
    parser.add_argument("--base-package", required=True)
    parser.add_argument("--project-class", required=True)
    args = parser.parse_args(argv)
    manifest = load_manifest(args.manifest)
    replacements = {
        "__DDD_PROJECT_NAME__": args.project_name,
        "__DDD_GROUP_ID__": args.group_id,
        "__DDD_BASE_PACKAGE__": args.base_package,
        "__DDD_BASE_PACKAGE_PATH__": args.base_package.replace(".", "/"),
        "__DDD_PROJECT_CLASS__": args.project_class,
    }
    errors = validate_generated_project(args.project_dir.resolve(), manifest, replacements)
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 5
    print(f"Static validation passed: {args.project_dir.resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
