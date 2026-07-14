#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import sys
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
}


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
    return data


def substitute(value: str, replacements: dict[str, str]) -> str:
    result = value
    for token, replacement in replacements.items():
        result = result.replace(token, replacement)
    return result


def _is_text_file(path: Path, manifest: dict[str, Any]) -> bool:
    return path.suffix in set(manifest["text_extensions"]) or path.name in set(manifest["text_filenames"])


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
