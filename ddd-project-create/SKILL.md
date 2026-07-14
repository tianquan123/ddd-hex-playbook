---
name: ddd-project-create
description: Use when creating a new team-standard Java/Spring DDD or hexagonal-architecture project from the approved six-module template.
---

# DDD Project Create

Create the team-standard six-module DDD project with the bundled deterministic generator. Do not copy or rename template files manually.

## Required inputs

Collect only values the user has not already supplied:

| Input | Meaning | Default |
| --- | --- | --- |
| `projectName` | Maven artifact and target directory name; lowercase letters, digits, and hyphens only | Required |
| `groupId` | Maven group ID and default Java package | Required |
| `basePackage` | Java source package | `basePackage = groupId` |
| `parentDirectory` | Directory in which the project directory is created | current working directory |

## Workflow

1. Validate all inputs before changing the filesystem.
2. Resolve `<parentDirectory>/<projectName>` and show the user a creation summary containing the project name, group ID, base package, absolute target path, and the six modules: `api`, `domain`, `application`, `infra`, `trigger`, and `starter`.
3. Require explicit confirmation of that summary. Do not create files before confirmation.
4. Run the generator from this skill directory:

   ```text
   python scripts/create_project.py --project-name <projectName> --group-id <groupId> --base-package <basePackage> --parent-dir <parentDirectory>
   ```

   Omit `--base-package` when the user accepts the default.
5. Report the generator result exactly. Do not describe a failed build as a successful project creation.

If the target directory is non-empty, refuse to continue and ask the user to choose another project name or parent directory. Never overwrite or merge into it.

## Result handling

The generator uses these exit codes:

| Code | Meaning | Response |
| --- | --- | --- |
| `0` | Project generated, statically validated, and Maven verified | Report success and the target path |
| `2` | Invalid input | Explain the rejected field and collect a corrected value |
| `3` | Unsafe or non-empty target | Report the target path and ask for a different destination |
| `4` | Template or rendering failure | Report the error and preserve the log for diagnosis |
| `5` | Generated-project validation failure | Report the failed invariant and preserve the log |
| `6` | Maven verification failure | Report the failed command and point to `.ddd-project-create.log` in the generated project |

On success, include the absolute target path, the six generated modules, and confirmation that Maven `verify` passed. Suggest removing or replacing the sample business slice after the team models the real domain.

On failure, include the exit code, concise cause, and log path when present. Do not hide Maven output or retry with altered project contents.

## Template maintenance

Read [references/template-contract.md](references/template-contract.md) when changing the bundled template, manifest, generator, or validator. Template changes must retain the six-module dependency direction and pass both Python contract tests and a clean Maven wrapper build.

Generated Java source must use Lombok-backed classes instead of Java Record declarations. HTTP request DTOs must carry Bean Validation constraints and be validated at the Trigger boundary.

Keep the bundled sample slice's technology choices intact: convert HTTP requests to application commands with static MapStruct, and persist sample orders to MySQL through MyBatis XML. Datasource values in the generated Starter YAML are deliberate `xxxxx` placeholders; replace them with real connection details before starting the application.

## Common mistakes

- Do not ask again for values already supplied by the user.
- Do not accept Java keywords or malformed package segments in `groupId` or `basePackage`.
- Do not create a nested package by replacing dots in arbitrary file contents; let the generator rename the placeholder package path.
- Do not skip the confirmation summary or the final Maven verification.
