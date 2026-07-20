from __future__ import annotations

import json
import re
import subprocess
import sys
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path


SKILL_ROOT = Path(__file__).resolve().parents[1]
SCRIPTS_DIR = SKILL_ROOT / "scripts"
sys.path.insert(0, str(SCRIPTS_DIR))

from create_project import (  # noqa: E402
    EXIT_BUILD,
    EXIT_TARGET,
    ProjectConfig,
    ProjectCreateError,
    build_command,
    create_project,
    package_to_path,
    to_class_name,
    validate_java_package,
    validate_project_name,
)
from validate_project import load_manifest, validate_generated_project  # noqa: E402


PLACEHOLDERS = [
    "__DDD_PROJECT_NAME__",
    "__DDD_GROUP_ID__",
    "__DDD_BASE_PACKAGE__",
    "__DDD_BASE_PACKAGE_PATH__",
    "__DDD_PROJECT_CLASS__",
]


def minimal_manifest() -> dict[str, object]:
    return {
        "template_version": "2.0.0",
        "placeholders": PLACEHOLDERS,
        "text_extensions": [".txt", ".xml", ".java", ".md", ".yml", ".yaml", ".properties", ".cmd"],
        "text_filenames": ["mvnw", ".gitignore"],
        "required_paths": ["pom.xml", "__DDD_PROJECT_NAME__-domain"],
        "forbidden_tokens": ["__DDD_"],
        "build_commands": {
            "windows": ["mvnw.cmd", "verify"],
            "posix": ["./mvnw", "verify"],
        },
        "module_dependencies": {
            "api": [],
            "domain": [],
            "application": ["domain"],
            "infra": ["application", "domain"],
            "trigger": ["api", "application"],
            "starter": ["trigger", "infra", "application", "domain"],
        },
        "java_rules": {
            "forbidden_imports": {
                "domain": ["org.springframework.", "org.apache.dubbo.", "org.mybatis.", ".application.", ".infra.", ".trigger."],
                "application": [".api.", ".facade.", ".infra.", ".trigger."],
                "trigger": [".infra."],
            },
            "suffix_packages": {
                "DO": ".infra.sampleorder.persistence.entity",
                "Controller": ".trigger.http.sampleorder",
                "Provider": ".trigger.rpc.sampleorder",
                "PersistenceAdapter": ".infra.sampleorder.adapter",
                "BeanConfiguration": ".bean",
            },
            "forbid_records": True,
            "mapstruct_required_fragments": [
                "Mappers.getMapper(",
                "unmappedTargetPolicy = ReportingPolicy.ERROR",
            ],
        },
    }


def write_fixture(root: Path, manifest: dict[str, object] | None = None) -> tuple[Path, Path]:
    template = root / "template"
    module = template / "__DDD_PROJECT_NAME__-domain"
    module.mkdir(parents=True)
    (template / "pom.xml").write_text(
        "<groupId>__DDD_GROUP_ID__</groupId><artifactId>__DDD_PROJECT_NAME__</artifactId>",
        encoding="utf-8",
    )
    (module / "package.txt").write_text(
        "__DDD_BASE_PACKAGE__|__DDD_BASE_PACKAGE_PATH__|__DDD_PROJECT_CLASS__",
        encoding="utf-8",
    )
    manifest_path = root / "manifest.json"
    manifest_path.write_text(json.dumps(manifest or minimal_manifest()), encoding="utf-8")
    return template, manifest_path


def successful_runner(command: list[str], **_: object) -> subprocess.CompletedProcess[str]:
    return subprocess.CompletedProcess(command, 0, stdout="BUILD SUCCESS\n", stderr="")


class InputValidationTests(unittest.TestCase):
    def test_accepts_valid_project_name(self) -> None:
        self.assertEqual("order-service2", validate_project_name("order-service2"))

    def test_rejects_invalid_project_names(self) -> None:
        for value in ("Order-Service", "order_service", "-order", "order-", "../order", ""):
            with self.subTest(value=value), self.assertRaises(ValueError):
                validate_project_name(value)

    def test_accepts_java_package(self) -> None:
        self.assertEqual("com.example.platform", validate_java_package("com.example.platform", "groupId"))

    def test_rejects_invalid_or_keyword_package_segments(self) -> None:
        for value in ("com.example-order", "com..orders", "com.class.orders", "9com.orders", ""):
            with self.subTest(value=value), self.assertRaises(ValueError):
                validate_java_package(value, "basePackage")

    def test_derives_class_and_package_path(self) -> None:
        self.assertEqual("OrderService", to_class_name("order-service"))
        self.assertEqual("com/example/platform", package_to_path("com.example.platform"))

    def test_selects_platform_wrapper(self) -> None:
        root = Path("project")
        self.assertEqual([str(root / "mvnw.cmd"), "verify"], build_command("win32", root))
        self.assertEqual([str(root / "mvnw"), "verify"], build_command("linux", root))
        self.assertEqual([str(root / "mvnw"), "verify"], build_command("darwin", root))


class ManifestValidationTests(unittest.TestCase):
    def write_java(
        self,
        project: Path,
        module: str,
        package: str,
        class_name: str,
        body: str = "public class Sample {}",
    ) -> Path:
        path = (project / f"order-service-{module}" / "src" / "main" / "java"
                / Path(*package.split(".")) / f"{class_name}.java")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(f"package {package};\n{body}\n", encoding="utf-8")
        return path

    def replacements(self) -> dict[str, str]:
        return {
            "__DDD_PROJECT_NAME__": "order-service",
            "__DDD_GROUP_ID__": "com.example",
            "__DDD_BASE_PACKAGE__": "com.example",
            "__DDD_BASE_PACKAGE_PATH__": "com/example",
            "__DDD_PROJECT_CLASS__": "OrderService",
        }

    def write_reactor(self, project: Path, dependencies: dict[str, list[str]]) -> None:
        namespace = "http://maven.apache.org/POM/4.0.0"
        for module, required in dependencies.items():
            module_dir = project / f"order-service-{module}"
            module_dir.mkdir(parents=True, exist_ok=True)
            dependency_xml = "".join(
                "<dependency><groupId>com.example</groupId>"
                f"<artifactId>order-service-{dependency}</artifactId></dependency>"
                for dependency in required
            )
            (module_dir / "pom.xml").write_text(
                f'<project xmlns="{namespace}"><modelVersion>4.0.0</modelVersion>'
                f'<groupId>com.example</groupId><artifactId>order-service-{module}</artifactId>'
                f'<dependencies>{dependency_xml}</dependencies></project>',
                encoding="utf-8",
            )

    def test_manifest_2_requires_architecture_contract(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            manifest = minimal_manifest()
            manifest.pop("module_dependencies")
            manifest.pop("java_rules")
            path = Path(tmp) / "manifest.json"
            path.write_text(json.dumps(manifest), encoding="utf-8")

            with self.assertRaisesRegex(ValueError, "java_rules.*module_dependencies"):
                load_manifest(path)

    def test_rejects_java_package_that_does_not_match_source_path(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            path = project / "order-service-domain/src/main/java/com/example/domain/sampleorder/model/Sample.java"
            path.parent.mkdir(parents=True)
            path.write_text("package com.example.domain.wrong; public class Sample {}", encoding="utf-8")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("package path" in error for error in errors))

    def test_rejects_forbidden_domain_import(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            self.write_java(project, "domain", "com.example.domain.sampleorder.model", "SampleOrder",
                            "import org.springframework.stereotype.Service; public class SampleOrder {}")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("forbidden import" in error for error in errors))

    def test_rejects_forbidden_application_import(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            self.write_java(project, "application", "com.example.application.sampleorder.service", "SampleOrderAppService",
                            "import com.example.infra.sampleorder.adapter.SampleOrderPersistenceAdapter; public interface SampleOrderAppService {}")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("forbidden import" in error for error in errors))

    def test_allows_external_package_with_same_segment_as_internal_module(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            self.write_java(project, "application", "com.example.application.sampleorder.service",
                            "SampleOrderAppServiceTest",
                            "import org.junit.jupiter.api.Test; public class SampleOrderAppServiceTest {}")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertFalse(any("forbidden import" in error for error in errors))

    def test_rejects_trigger_to_infra_import(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            self.write_java(project, "trigger", "com.example.trigger.http.sampleorder", "SampleOrderController",
                            "import com.example.infra.sampleorder.adapter.SampleOrderPersistenceAdapter; public class SampleOrderController {}")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("forbidden import" in error for error in errors))

    def test_rejects_misplaced_role_suffix(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            self.write_java(project, "infra", "com.example.infra.sampleorder.adapter", "SampleOrderDO")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("misplaced suffix" in error for error in errors))

    def test_rejects_java_record_declaration(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            self.write_java(project, "domain", "com.example.domain.sampleorder.model", "SampleOrderId",
                            "public record SampleOrderId(String value) {}")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("record declaration" in error for error in errors))

    def test_rejects_mapstruct_converter_without_static_instance_or_error_policy(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            self.write_java(project, "application", "com.example.application.sampleorder.mapping", "SampleOrderConverter",
                            "import org.mapstruct.Mapper; @Mapper public interface SampleOrderConverter {}")

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("MapStruct contract" in error for error in errors))

    def test_rejects_wrong_internal_module_dependency(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            dependencies = dict(minimal_manifest()["module_dependencies"])
            dependencies["trigger"] = ["api", "application", "infra"]
            self.write_reactor(project, dependencies)

            errors = validate_generated_project(project, minimal_manifest(), self.replacements())

            self.assertTrue(any("module dependency" in error for error in errors))

    def real_internal_dependencies(self, module: str) -> list[str]:
        template = SKILL_ROOT / "assets" / "project-template"
        pom = template / f"__DDD_PROJECT_NAME__-{module}" / "pom.xml"
        root = ET.parse(pom).getroot()
        namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
        prefix = "__DDD_PROJECT_NAME__-"
        result: list[str] = []
        for dependency in root.findall("m:dependencies/m:dependency", namespace):
            artifact = dependency.findtext("m:artifactId", default="", namespaces=namespace)
            if artifact.startswith(prefix):
                result.append(artifact[len(prefix):])
        return result

    def test_real_template_declares_exact_internal_module_graph(self) -> None:
        expected = minimal_manifest()["module_dependencies"]
        actual = {module: self.real_internal_dependencies(module) for module in expected}
        self.assertEqual(expected, actual)

    def test_real_template_pins_approved_public_dependency_versions(self) -> None:
        pom = (SKILL_ROOT / "assets" / "project-template" / "pom.xml").read_text(encoding="utf-8")
        for fragment in (
            "<java.version>21</java.version>",
            "<mapstruct.version>1.6.3</mapstruct.version>",
            "<mybatis-spring-boot.version>3.0.4</mybatis-spring-boot.version>",
            "<dubbo.version>3.3.0</dubbo.version>",
            "<h2.version>2.2.224</h2.version>",
        ):
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, pom)

    def test_dubbo_version_does_not_override_spring_boot_dependency_management(self) -> None:
        template = SKILL_ROOT / "assets" / "project-template"
        root_pom = (template / "pom.xml").read_text(encoding="utf-8")
        trigger_pom = (template / "__DDD_PROJECT_NAME__-trigger" / "pom.xml").read_text(encoding="utf-8")

        self.assertNotIn("<artifactId>dubbo-bom</artifactId>", root_pom)
        self.assertRegex(
            trigger_pom,
            r"<artifactId>dubbo-spring-boot-starter</artifactId>\s*<version>\$\{dubbo\.version\}</version>",
        )

    def test_domain_pom_enforces_framework_free_boundary(self) -> None:
        pom = (SKILL_ROOT / "assets" / "project-template" / "__DDD_PROJECT_NAME__-domain"
               / "pom.xml").read_text(encoding="utf-8")
        for fragment in (
            "org.springframework:*",
            "org.springframework.boot:*",
            "org.apache.dubbo:*",
            "org.mybatis:*",
            "__DDD_GROUP_ID__:__DDD_PROJECT_NAME__-api",
            "__DDD_GROUP_ID__:__DDD_PROJECT_NAME__-application",
            "__DDD_GROUP_ID__:__DDD_PROJECT_NAME__-infra",
            "__DDD_GROUP_ID__:__DDD_PROJECT_NAME__-trigger",
            "__DDD_GROUP_ID__:__DDD_PROJECT_NAME__-starter",
        ):
            with self.subTest(fragment=fragment):
                self.assertIn(fragment, pom)

    def test_real_template_uses_mapstruct_and_mybatis_xml(self) -> None:
        template = SKILL_ROOT / "assets" / "project-template"
        controller = (template / "__DDD_PROJECT_NAME__-trigger" / "src" / "main" / "java"
                      / "__DDD_BASE_PACKAGE_PATH__" / "trigger" / "http" / "sampleorder"
                      / "SampleOrderController.java").read_text(encoding="utf-8")
        http_converter = (template / "__DDD_PROJECT_NAME__-trigger" / "src" / "main" / "java"
                          / "__DDD_BASE_PACKAGE_PATH__" / "trigger" / "http" / "sampleorder"
                          / "SampleOrderHttpConverter.java").read_text(encoding="utf-8")
        mapper_xml = (template / "__DDD_PROJECT_NAME__-infra" / "src" / "main" / "resources"
                      / "mapper" / "sampleorder" / "SampleOrderMapper.xml").read_text(encoding="utf-8")
        yaml = (template / "__DDD_PROJECT_NAME__-starter" / "src" / "main" / "resources"
                / "application.yml").read_text(encoding="utf-8")

        self.assertIn("SampleOrderHttpConverter.INSTANCE.toCommand(request)", controller)
        self.assertIn("Mappers.getMapper(SampleOrderHttpConverter.class)", http_converter)
        self.assertIn('unmappedTargetPolicy = ReportingPolicy.ERROR', http_converter)
        self.assertIn('<insert id="insert"', mapper_xml)
        self.assertIn("jdbc:h2:mem:sample-order", yaml)
        self.assertEqual([], list(template.rglob("InMemorySampleOrderRepository.java")))

    def test_real_template_uses_business_slice_packages_and_names(self) -> None:
        template = SKILL_ROOT / "assets" / "project-template"
        java_sources = list(template.rglob("*.java"))
        source_text = "\n".join(path.read_text(encoding="utf-8") for path in java_sources)

        self.assertNotIn(".sample.", source_text)
        self.assertNotRegex(source_text, r"\b[A-Za-z0-9_]*Aggregate\b")
        self.assertTrue(any("/domain/sampleorder/model/SampleOrder.java" in path.as_posix()
                            for path in java_sources))
        self.assertTrue(any("/bean/SampleOrderBeanConfiguration.java" in path.as_posix()
                            for path in java_sources))

    def test_template_java_sources_do_not_declare_records(self) -> None:
        template = SKILL_ROOT / "assets" / "project-template"
        declaration = re.compile(r"\brecord\s+[A-Za-z_$][A-Za-z0-9_$]*\s*\(")
        matches = [
            str(path.relative_to(template))
            for path in template.rglob("*.java")
            if declaration.search(path.read_text(encoding="utf-8"))
        ]

        self.assertEqual([], matches)

    def test_windows_wrapper_guards_null_symlink_target(self) -> None:
        wrapper = (SKILL_ROOT / "assets" / "project-template" / "mvnw.cmd").read_text(encoding="utf-8")

        self.assertNotIn("(Get-Item $MAVEN_M2_PATH).Target[0]", wrapper)
        self.assertIn("$m2Item.Target[0] -ne $null", wrapper)

    def test_real_template_contains_every_required_path(self) -> None:
        manifest = load_manifest(SKILL_ROOT / "assets" / "template-manifest.json")
        template = SKILL_ROOT / "assets" / "project-template"
        missing = [path for path in manifest["required_paths"] if not (template / path).exists()]
        self.assertEqual([], missing)

    def test_loads_valid_manifest(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "manifest.json"
            path.write_text(json.dumps(minimal_manifest()), encoding="utf-8")
            self.assertEqual("2.0.0", load_manifest(path)["template_version"])

    def test_reports_missing_required_path(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            manifest = minimal_manifest()
            replacements = {token: "value" for token in PLACEHOLDERS}
            errors = validate_generated_project(project, manifest, replacements)
            self.assertTrue(any("required path" in error for error in errors))

    def test_reports_unknown_placeholder(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            project = Path(tmp)
            (project / "pom.xml").write_text("__DDD_UNKNOWN__", encoding="utf-8")
            (project / "value-domain").mkdir()
            manifest = minimal_manifest()
            replacements = {token: "value" for token in PLACEHOLDERS}
            errors = validate_generated_project(project, manifest, replacements)
            self.assertTrue(any("placeholder" in error for error in errors))


class ProjectCreationTests(unittest.TestCase):
    def config(self, root: Path, template: Path, manifest: Path, **overrides: object) -> ProjectConfig:
        values: dict[str, object] = {
            "project_name": "order-service",
            "group_id": "com.example.platform",
            "base_package": "com.example.platform",
            "parent_dir": root / "output",
            "template_dir": template,
            "manifest_path": manifest,
            "platform_name": "win32",
        }
        values.update(overrides)
        return ProjectConfig(**values)

    def test_copies_and_replaces_paths_and_text(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            template, manifest = write_fixture(root)
            config = self.config(root, template, manifest)
            config.parent_dir.mkdir()

            project = create_project(config, successful_runner)

            self.assertEqual(config.parent_dir / "order-service", project)
            self.assertTrue((project / "order-service-domain" / "package.txt").is_file())
            self.assertEqual(
                "com.example.platform|com/example/platform|OrderService",
                (project / "order-service-domain" / "package.txt").read_text(encoding="utf-8"),
            )
            self.assertNotIn("__DDD_", (project / "pom.xml").read_text(encoding="utf-8"))
            self.assertFalse((project / ".ddd-project-create.log").exists())

    def test_replaces_package_path_placeholder_with_nested_directories(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            template, manifest_path = write_fixture(root)
            package_dir = template / "__DDD_PROJECT_NAME__-domain" / "src" / "__DDD_BASE_PACKAGE_PATH__"
            package_dir.mkdir(parents=True)
            (package_dir / "Sample.java").write_text(
                "package __DDD_BASE_PACKAGE__;",
                encoding="utf-8",
            )
            manifest = minimal_manifest()
            manifest["required_paths"].append(
                "__DDD_PROJECT_NAME__-domain/src/__DDD_BASE_PACKAGE_PATH__/Sample.java"
            )
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            config = self.config(root, template, manifest_path)
            config.parent_dir.mkdir()

            try:
                project = create_project(config, successful_runner)
            except ValueError as exc:
                self.fail(f"package path placeholder was treated as a filename: {exc}")

            self.assertTrue(
                (project / "order-service-domain" / "src" / "com" / "example" / "platform" / "Sample.java").is_file()
            )

    def test_defaults_base_package_to_group_id(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            template, manifest = write_fixture(root)
            config = self.config(root, template, manifest, base_package=None)
            config.parent_dir.mkdir()

            project = create_project(config, successful_runner)

            text = (project / "order-service-domain" / "package.txt").read_text(encoding="utf-8")
            self.assertTrue(text.startswith("com.example.platform|com/example/platform"))

    def test_excludes_template_build_and_ide_artifacts(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            template, manifest = write_fixture(root)
            (template / "target").mkdir()
            (template / "target" / "root.jar").write_bytes(b"artifact")
            module_target = template / "__DDD_PROJECT_NAME__-domain" / "target"
            module_target.mkdir()
            (module_target / "classes.bin").write_bytes(b"artifact")
            (template / ".idea").mkdir()
            (template / ".idea" / "workspace.xml").write_text("local", encoding="utf-8")
            (template / "project.iml").write_text("local", encoding="utf-8")
            config = self.config(root, template, manifest)
            config.parent_dir.mkdir()

            project = create_project(config, successful_runner)

            self.assertFalse((project / "target").exists())
            self.assertFalse((project / "order-service-domain" / "target").exists())
            self.assertFalse((project / ".idea").exists())
            self.assertFalse((project / "project.iml").exists())

    def test_rejects_non_empty_target_without_modifying_sentinel(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            template, manifest = write_fixture(root)
            config = self.config(root, template, manifest)
            target = config.parent_dir / config.project_name
            target.mkdir(parents=True)
            sentinel = target / "sentinel.txt"
            sentinel.write_text("keep", encoding="utf-8")

            with self.assertRaises(ProjectCreateError) as caught:
                create_project(config, successful_runner)

            self.assertEqual(EXIT_TARGET, caught.exception.exit_code)
            self.assertEqual("keep", sentinel.read_text(encoding="utf-8"))
            self.assertEqual([sentinel], list(target.iterdir()))

    def test_build_failure_keeps_project_and_log(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            template, manifest = write_fixture(root)
            config = self.config(root, template, manifest)
            config.parent_dir.mkdir()

            def failing_runner(command: list[str], **_: object) -> subprocess.CompletedProcess[str]:
                return subprocess.CompletedProcess(command, 1, stdout="BUILD FAILURE\n", stderr="broken\n")

            with self.assertRaises(ProjectCreateError) as caught:
                create_project(config, failing_runner)

            target = config.parent_dir / config.project_name
            self.assertEqual(EXIT_BUILD, caught.exception.exit_code)
            self.assertTrue(target.is_dir())
            self.assertIn("BUILD FAILURE", (target / ".ddd-project-create.log").read_text(encoding="utf-8"))


class SkillContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.text = (SKILL_ROOT / "SKILL.md").read_text(encoding="utf-8")

    def test_frontmatter_is_discoverable_and_complete(self) -> None:
        self.assertIn("name: ddd-project-create", self.text)
        self.assertRegex(self.text, r"description: Use when .+")
        self.assertNotIn("TODO", self.text)

    def test_collects_required_inputs_and_defaults(self) -> None:
        for token in ("projectName", "groupId", "basePackage", "parentDirectory"):
            with self.subTest(token=token):
                self.assertIn(token, self.text)
        self.assertIn("basePackage = groupId", self.text)
        self.assertIn("current working directory", self.text)

    def test_requires_confirmation_and_forbids_overwrite(self) -> None:
        self.assertIn("absolute target path", self.text)
        self.assertIn("explicit confirmation", self.text)
        self.assertIn("non-empty", self.text)
        self.assertNotIn("--force", self.text)

    def test_documents_sample_mapping_and_persistence_choices(self) -> None:
        self.assertIn("static MapStruct", self.text)
        self.assertIn("MyBatis XML", self.text)
        self.assertIn("default H2", self.text)
        self.assertIn("`bean/*BeanConfiguration`", self.text)
        self.assertIn("not an `Aggregate` suffix", self.text)

    def test_invokes_generator_and_explains_exit_codes(self) -> None:
        self.assertIn("python scripts/create_project.py", self.text)
        for exit_code in ("`2`", "`3`", "`4`", "`5`", "`6`"):
            with self.subTest(exit_code=exit_code):
                self.assertIn(exit_code, self.text)


if __name__ == "__main__":
    unittest.main()
