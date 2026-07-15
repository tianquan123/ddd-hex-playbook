from __future__ import annotations

import json
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

    def test_behavior_cases_have_stable_schema_and_ids(self) -> None:
        cases = json.loads(read_utf8(ROOT / "evals" / "cases.json"))
        expected_ids = {
            "db-table-to-aggregate",
            "ambiguous-account-contexts",
            "order-payment-aggregate",
            "simple-catalog-crud",
            "order-lifecycle-rich-domain",
            "simple-pagination-cqrs",
            "operations-report-cqrs",
            "local-domain-event",
            "integration-event",
            "event-sourcing",
        }
        self.assertEqual(expected_ids, {case["id"] for case in cases})
        self.assertEqual(len(cases), len({case["id"] for case in cases}))
        required = {
            "id", "skill", "prompt", "fixture", "must", "forbidden",
            "critical_forbidden", "questions", "alternatives",
        }
        for case in cases:
            with self.subTest(case=case["id"]):
                self.assertEqual(required, set(case))
                self.assertIn(case["skill"], SKILLS)
                self.assertTrue(case["prompt"])
                self.assertTrue(case["must"])
                self.assertTrue(case["critical_forbidden"])

    def test_behavior_rubric_defines_semantic_scoring_and_release_gate(self) -> None:
        rubric = read_utf8(ROOT / "evals" / "rubric.md")
        for text in (
            "语义评分", "关键禁行项", "90%", "不得向运行者泄露",
            "不是逐字匹配", "原始响应",
        ):
            self.assertIn(text, rubric)

    def test_modeling_decision_references_define_approved_contracts(self) -> None:
        expected = {
            "discovery-evidence.md": (
                "参与者", "命令", "领域事件", "策略", "外部系统",
                "业务时间边界", "热点", "示例", "反例",
            ),
            "context-mapping.md": (
                "小地图", "关系表", "事实权威", "上游", "下游",
                "翻译责任", "先描述关系事实",
            ),
            "engineering-handoff.md": (
                "业务目标", "成功结果", "失败结果", "即时一致",
                "补偿", "查询需求", "一致性窗口", "禁止固化",
            ),
        }
        root = SKILLS["ddd-modeling"] / "references"
        for filename, fragments in expected.items():
            text = read_utf8(root / filename)
            with self.subTest(reference=filename):
                for fragment in fragments:
                    self.assertIn(fragment, text)
                self.assertNotRegex(text, r"\]\([^)]*/references/")


if __name__ == "__main__":
    unittest.main()
