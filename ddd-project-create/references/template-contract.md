# Template Contract

This reference defines the maintenance boundary for `assets/project-template`, `assets/template-manifest.json`, and the generator scripts.

## Stable architecture

The generated Maven reactor contains exactly these modules in dependency order:

1. `api`
2. `domain`
3. `application`
4. `infra`
5. `trigger`
6. `starter`

`domain` must not depend on Spring or another project module. `application` depends only on `domain`. Adapters implement or invoke inward-facing contracts, while `starter` is the composition root.

Generated Java source must not declare Java Record. Use Lombok-backed classes for DTOs, commands, and value objects; preserve domain invariants in explicit constructors when generated Lombok constructors are insufficient.

The sample HTTP boundary must use static MapStruct to convert request DTOs into application commands. The sample Infra adapter must persist through a MyBatis mapper interface and XML SQL, never an in-memory map. Keep datasource and MyBatis settings in Starter YAML; its `xxxxx` connection values are intentional placeholders and must not be treated as working credentials.

## Placeholders

The manifest is the source of truth for every supported placeholder. A placeholder must have one semantic purpose and must be replaced in both paths and eligible text files.

- `__DDD_PROJECT_NAME__`: kebab-case project and artifact name
- `__DDD_GROUP_ID__`: Maven group ID
- `__DDD_BASE_PACKAGE__`: dotted Java package
- `__DDD_BASE_PACKAGE_PATH__`: slash-separated Java package path
- `__DDD_PROJECT_CLASS__`: UpperCamelCase application class prefix

Adding, removing, or renaming a placeholder requires matching updates to the manifest, generator, validator, and tests. Unknown `__DDD_*__` tokens are validation failures.

## Required paths and content

`template-manifest.json` lists paths that every generated project must contain and content checks that prove key substitutions occurred. Keep those checks small and architectural; do not duplicate the entire template in the manifest.

The template must not contain generated build output, IDE metadata, secrets, symlinks, or machine-specific Maven configuration. The generator also excludes module `target` directories and common IDE artifacts as a defense in depth.

## Verification

After any template change:

1. Run `python -m unittest discover -s ddd-project-create/tests -v` from the repository root.
2. Run `mvnw.cmd verify` in `assets/project-template` on Windows, or `./mvnw verify` on Unix-like systems.
3. Generate a project with non-default values and run its wrapper `verify` command.
4. Confirm no `__DDD_*__` placeholder remains in the generated tree.

Do not publish a template change when any step fails.
