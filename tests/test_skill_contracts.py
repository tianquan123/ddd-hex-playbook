from __future__ import annotations

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SKILLS = {
    "ddd-modeling": ROOT / "ddd-modeling",
    "java-spring-hex-playbook": ROOT / "java-spring-hex-playbook",
    "ddd-project-create": ROOT / "ddd-project-create",
}


def read_utf8(path: Path) -> str:
    return path.read_bytes().decode("utf-8", errors="strict").replace("\r\n", "\n")


def read_frontmatter(path: Path) -> dict[str, str]:
    text = read_utf8(path)
    if not text.startswith("---\n"):
        raise AssertionError(f"missing frontmatter: {path}")
    block = text.split("---\n", 2)[1]
    result: dict[str, str] = {}
    for line in block.splitlines():
        if ":" in line:
            key, value = line.split(":", 1)
            result[key.strip()] = value.strip().strip('"')
    return result


def direct_reference_targets(skill_dir: Path) -> list[Path]:
    text = read_utf8(skill_dir / "SKILL.md")
    links = re.findall(r"\[[^]]+\]\((references/[^)]+)\)", text)
    return [skill_dir / link for link in links]


class SkillContractTests(unittest.TestCase):
    def test_frontmatter_and_direct_references_are_valid(self) -> None:
        for skill_name, skill_dir in SKILLS.items():
            with self.subTest(skill=skill_name):
                metadata = read_frontmatter(skill_dir / "SKILL.md")
                self.assertEqual(skill_name, metadata.get("name"))
                self.assertTrue(metadata.get("description"))
                for target in direct_reference_targets(skill_dir):
                    self.assertTrue(target.is_file(), target)

    def test_ui_metadata_is_utf8_and_matches_decision_skills(self) -> None:
        for skill_name, skill_dir in SKILLS.items():
            text = read_utf8(skill_dir / "agents" / "openai.yaml")
            with self.subTest(skill=skill_name, contract="base-ui"):
                self.assertIn("interface:", text)
                self.assertIn(f"${skill_name}", text)
                self.assertNotIn("�", text)
        expected = {
            "ddd-modeling": (
                'display_name: "DDD Modeling"',
                'short_description: "从真实业务场景、不变式与语言冲突推导可验证的领域模型"',
                'default_prompt: "Use $ddd-modeling to derive a domain model from business scenarios, invariants, and language boundaries."',
            ),
            "java-spring-hex-playbook": (
                'display_name: "Java Spring Hex Playbook"',
                'short_description: "基于项目事实审查 Java/Spring 六边形架构决策"',
                'default_prompt: "Use $java-spring-hex-playbook to review a Java/Spring architecture decision from project facts and dependency boundaries."',
            ),
        }
        for skill_name, fragments in expected.items():
            text = read_utf8(SKILLS[skill_name] / "agents" / "openai.yaml")
            with self.subTest(skill=skill_name):
                for fragment in fragments:
                    self.assertIn(fragment, text)
                self.assertNotIn("�", text)

    def test_references_do_not_route_to_nested_markdown(self) -> None:
        markdown_link = re.compile(r"\[[^]]+\]\([^)]+\.md(?:#[^)]+)?\)")
        for skill_name, skill_dir in SKILLS.items():
            for reference in (skill_dir / "references").glob("*.md"):
                with self.subTest(skill=skill_name, reference=reference.name):
                    self.assertNotRegex(read_utf8(reference), markdown_link)


if __name__ == "__main__":
    unittest.main()
