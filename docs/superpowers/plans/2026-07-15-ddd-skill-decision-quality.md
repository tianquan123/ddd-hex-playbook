# DDD Skill Decision Quality Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve the decision quality of `ddd-modeling` and `java-spring-hex-playbook` with evidence-driven workflows, explicit handoff contracts, graded CQRS/event decisions, and repeatable skill evaluations without changing the generated six-module project template.

**Architecture:** Keep the existing three-skill boundary. Add progressively loaded references beneath the two decision skills, keep their `SKILL.md` files as concise routers and output contracts, and add repository-level deterministic contracts plus isolated behavioral runs. Establish and score the baseline before changing skill behavior, then run the identical cases against the candidate revision.

**Tech Stack:** Markdown skills and references, JSON evaluation fixtures, Python 3 standard-library `unittest`, Codex isolated evaluation sessions, existing Maven Wrapper/JDK 21 template verification, skill-creator metadata generator and validator.

## Global Constraints

- Do not add a new skill or workflow orchestrator.
- Do not change `ddd-project-create/assets/project-template`, its six modules, dependencies, or generated Java behavior.
- Do not add ArchUnit or another runtime/template dependency.
- Do not make EventStorming, CQRS, Outbox, or Event Sourcing mandatory.
- Do not automatically create or modify a consumer project's `CONTEXT.md`, Context Map, or ADR.
- Keep references one level below `SKILL.md`; references must not require nested reference routing.
- Keep the direct-answer exception for simple definition questions.
- Use Python standard library for repository contract tests; temporary PyYAML is allowed only to run skill-creator's external validator.
- Behavioral runners receive only the target skill, case prompt, and raw fixture. They must not receive rubrics, expected behavior, forbidden behavior, design rationale, or another run's output.
- A behavioral run fails publication if any critical forbidden behavior occurs, even when the numeric score is at least 90%.
- Because `/docs/` and `/evals/` are ignored, stage intentional plan/evaluation artifacts with `git add -f`.

---

## File Map

| Path | Responsibility |
| --- | --- |
| `tests/test_skill_contracts.py` | UTF-8, frontmatter, direct-reference, UI metadata, routing, and evaluation-schema contracts |
| `evals/cases.json` | Ten stable prompts with must/forbidden/question/alternative scoring criteria |
| `evals/rubric.md` | Semantic scoring and critical-failure rules |
| `evals/runs/baseline/` and `evals/runs/candidate/` | Run metadata, raw isolated responses, scores, and comparison summary |
| `ddd-modeling/SKILL.md` | Adaptive-track router, modeling workflow, output contract, stop conditions |
| `ddd-modeling/references/discovery-evidence.md` | Timeline, policy, hotspot, example, and counterexample discovery procedure |
| `ddd-modeling/references/context-mapping.md` | Small map, relationship ledger, authority, translation, and relationship patterns |
| `ddd-modeling/references/engineering-handoff.md` | Modeling decision package and Java/Spring handoff schema |
| `ddd-modeling/references/bounded-contexts.md` | Bounded-context candidate evidence only; no duplicated relationship procedure |
| `java-spring-hex-playbook/SKILL.md` | Handoff check and routing to local architecture, CQRS, and event reviews |
| `java-spring-hex-playbook/references/module-strategies.md` | CRUD, rich-domain/hexagonal, and independent-read-model local strategies |
| `java-spring-hex-playbook/references/cqrs-decision-ladder.md` | Six CQRS levels, evidence, costs, and stop rules |
| `java-spring-hex-playbook/references/transactions-and-events.md` | Local domain, reliable integration, and Event Sourcing review profiles |
| `README.md` | Updated collaboration chain, evaluation commands, and frozen-template boundary |

---

### Task 1: Deterministic Skill Contracts and UTF-8 UI Metadata

**Files:**
- Create: `tests/test_skill_contracts.py`
- Modify: `ddd-modeling/agents/openai.yaml`
- Modify: `java-spring-hex-playbook/agents/openai.yaml`
- Test: `tests/test_skill_contracts.py`

**Interfaces:**
- Consumes: Existing skill directories and Markdown link syntax.
- Produces: `read_utf8(path) -> str`, `read_frontmatter(path) -> dict[str, str]`, and `direct_reference_targets(skill_dir) -> list[Path]` helpers reused by later contract tests.

- [ ] **Step 1: Write the failing UTF-8 and metadata contracts**

Create `tests/test_skill_contracts.py` with these helpers and initial tests:

```python
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
```

- [ ] **Step 2: Run the contracts and verify the metadata test fails**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
```

Expected: `test_frontmatter_and_direct_references_are_valid` passes; `test_ui_metadata_is_utf8_and_matches_decision_skills` fails because the two short descriptions do not match the approved UTF-8 text.

- [ ] **Step 3: Regenerate both metadata files deterministically**

Run:

```powershell
python -X utf8 "$HOME\.codex\skills\.system\skill-creator\scripts\generate_openai_yaml.py" ddd-modeling `
  --name ddd-modeling `
  --interface 'display_name=DDD Modeling' `
  --interface 'short_description=从真实业务场景、不变式与语言冲突推导可验证的领域模型' `
  --interface 'default_prompt=Use $ddd-modeling to derive a domain model from business scenarios, invariants, and language boundaries.'

python -X utf8 "$HOME\.codex\skills\.system\skill-creator\scripts\generate_openai_yaml.py" java-spring-hex-playbook `
  --name java-spring-hex-playbook `
  --interface 'display_name=Java Spring Hex Playbook' `
  --interface 'short_description=基于项目事实审查 Java/Spring 六边形架构决策' `
  --interface 'default_prompt=Use $java-spring-hex-playbook to review a Java/Spring architecture decision from project facts and dependency boundaries.'
```

Expected: both commands report a generated `agents/openai.yaml`; files contain only quoted interface strings and remain UTF-8.

- [ ] **Step 4: Run the contracts and verify they pass**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
```

Expected: `Ran 3 tests` and `OK`.

- [ ] **Step 5: Commit the contract foundation**

```powershell
git add tests/test_skill_contracts.py ddd-modeling/agents/openai.yaml java-spring-hex-playbook/agents/openai.yaml
git commit -m "test: add skill contracts and repair metadata"
```

---

### Task 2: Behavioral Evaluation Corpus and Baseline Run

**Files:**
- Modify: `tests/test_skill_contracts.py`
- Create: `evals/cases.json`
- Create: `evals/rubric.md`
- Create: `evals/runs/baseline/manifest.json`
- Create: ten files in `evals/runs/baseline/responses/`, each named after an exact case ID from `cases.json`
- Create: `evals/runs/baseline/scores.json`

**Interfaces:**
- Consumes: `read_utf8()` from Task 1 and the current, unmodified decision skills.
- Produces: a stable JSON case schema with fields `id`, `skill`, `prompt`, `fixture`, `must`, `forbidden`, `critical_forbidden`, `questions`, and `alternatives`.

- [ ] **Step 1: Add failing evaluation-schema tests**

Add imports `json` and these tests to `SkillContractTests`:

```python
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
```

Also add `import json` at the top.

- [ ] **Step 2: Run the new tests and verify they fail**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
```

Expected: two errors with `FileNotFoundError` for `evals/cases.json` and `evals/rubric.md`.

- [ ] **Step 3: Create the ten-case corpus**

Create `evals/cases.json` with this exact content:

```json
[
  {
    "id": "db-table-to-aggregate",
    "skill": "ddd-modeling",
    "prompt": "我们有 orders、order_items、payments 三张表，请直接根据外键关系生成聚合边界。",
    "fixture": ["当前只提供了表名和外键，没有业务场景、业务不变式或一致性要求。"],
    "must": ["拒绝仅凭表结构确定聚合。", "说明还需要命令、成功/失败结果、不变式和一致性证据。", "把当前结论标记为未验证候选。"],
    "forbidden": ["把一张表等同于一个实体或聚合。"],
    "critical_forbidden": ["依据外键或数据库事务直接宣布三张表属于同一聚合。"],
    "questions": ["哪个业务操作必须让订单、明细和支付状态立即同时成立？"],
    "alternatives": ["可以先给出数据关系图，但必须明确它不是领域模型。"]
  },
  {
    "id": "ambiguous-account-contexts",
    "skill": "ddd-modeling",
    "prompt": "身份系统和结算系统都使用 Account，应该共享一个标准 Account 模型吗？",
    "fixture": ["身份系统负责登录、凭证和账号停用。", "结算系统负责余额、资金冻结和解冻。", "两个团队独立发布。"],
    "must": ["识别 Account 在两个上下文中的不同含义。", "分别指出身份事实和资金事实的权威来源。", "给出只覆盖这两个上下文的小型 Context Map。", "明确模型或契约翻译责任。"],
    "forbidden": ["因为名称相同就共享完整模型。"],
    "critical_forbidden": ["要求两个系统使用一个权威的标准 Account 实体。"],
    "questions": ["跨上下文实际需要传播的是身份状态、客户标识还是资金账户状态？"],
    "alternatives": ["使用 Published Language 交换稳定标识。", "由下游 ACL 翻译上游契约。"]
  },
  {
    "id": "order-payment-aggregate",
    "skill": "ddd-modeling",
    "prompt": "Order 和 Payment 是否应该放在同一个聚合？",
    "fixture": ["订单确认后可以等待支付。", "支付可能晚到、失败或被独立补偿。", "订单取消和退款由不同业务规则控制。"],
    "must": ["从即时一致性而不是对象关联判断边界。", "比较订单与支付的生命周期和失败时间。", "说明延迟一致或补偿对边界的影响。", "列出被排除方案及证据。"],
    "forbidden": ["仅因 Order 引用了 Payment 就合并。"],
    "critical_forbidden": ["因为同一数据库事务能更新两者就认定为一个聚合。"],
    "questions": ["是否存在必须在同一次业务提交中同时成立且不可补偿的订单—支付不变式？"],
    "alternatives": ["独立聚合并通过领域事实协调。", "若存在经证实的即时不变式，可重新评估更强边界。"]
  },
  {
    "id": "simple-catalog-crud",
    "skill": "java-spring-hex-playbook",
    "prompt": "一个以查询为主、只有简单后台编辑的商品目录，在现有六模块项目里应如何组织？",
    "fixture": ["编辑只有名称、描述和展示状态。", "字段之间没有跨字段不变式。", "没有复杂生命周期。"],
    "must": ["选择或允许简单 CRUD/Transaction Script 局部策略。", "解释为什么当前证据不需要富领域聚合。", "说明六模块不等于上下文内部复制六层。", "给出最小测试建议。"],
    "forbidden": ["为每个字段创建值对象和领域服务。"],
    "critical_forbidden": ["强制该上下文拥有完整六层内部结构或领域事件。"],
    "questions": [],
    "alternatives": ["若后续出现价格、发布或合规不变式，再演进为富领域模型。"]
  },
  {
    "id": "order-lifecycle-rich-domain",
    "skill": "java-spring-hex-playbook",
    "prompt": "订单支持确认、取消和发货，确认后不能修改数量，发货后不能取消，应该怎样落位？",
    "fixture": ["确认前允许调整数量。", "确认后数量冻结。", "发货后禁止取消。", "HTTP、定时关闭任务都可能触发用例。"],
    "must": ["为订单上下文选择富领域/六边形局部策略。", "让聚合保护生命周期不变式。", "让 Application 编排用例和事务。", "区分 HTTP 与 Job 入站适配器。", "提出 Domain 和 Application 测试。"],
    "forbidden": ["把业务规则复制到不同入口。"],
    "critical_forbidden": ["只在 Controller、Job 或 Mapper 中检查订单生命周期规则。"],
    "questions": [],
    "alternatives": ["若取消涉及独立履约补偿，可把履约保持为独立上下文并显式协调。"]
  },
  {
    "id": "simple-pagination-cqrs",
    "skill": "java-spring-hex-playbook",
    "prompt": "普通订单分页列表是否应该上 CQRS、异步投影和独立读库？",
    "fixture": ["查询只涉及订单表和少量筛选。", "数据库当前满足延迟目标。", "读写使用同一服务和同一数据库。"],
    "must": ["按最小充分复杂度停在 CQRS 级别 1 或 2。", "说明当前没有异步投影或独立存储证据。", "给出未来升级触发条件。", "建议直接返回用例读模型而不是重建聚合。"],
    "forbidden": ["把 CQRS 描述为成熟度要求。"],
    "critical_forbidden": ["无证据要求异步投影、独立读库或 Event Sourcing。"],
    "questions": [],
    "alternatives": ["Application Query Port 与写用例分接口但共享数据库。"]
  },
  {
    "id": "operations-report-cqrs",
    "skill": "java-spring-hex-playbook",
    "prompt": "运营报表需要组合订单、库存和履约数据，结构与写模型完全不同，允许五分钟延迟，CQRS 应走到哪一级？",
    "fixture": ["报表查询量明显高于写入量。", "允许五分钟陈旧。", "报表可在失败后重建。", "团队可以运维一个投影任务，但不希望立即拆独立数据库。"],
    "must": ["选择满足证据的最低 CQRS 级别。", "明确五分钟一致性窗口。", "说明投影检查点和失败重建。", "说明新增运维成本。", "解释为什么暂不选择更高级别。"],
    "forbidden": ["忽略投影恢复和监控成本。"],
    "critical_forbidden": ["把 Event Sourcing 描述为最终成熟形态或无条件目标。"],
    "questions": [],
    "alternatives": ["级别 3 的独立读模型共库。", "证据足够时使用级别 4 的异步投影但仍共用存储基础设施。"]
  },
  {
    "id": "local-domain-event",
    "skill": "java-spring-hex-playbook",
    "prompt": "订单确认后，同一进程内更新一个非关键统计，允许偶尔丢失并可离线修复，需要 Outbox 和 MQ 吗？",
    "fixture": ["消费者与订单在同一进程。", "统计不是交易正确性的组成部分。", "允许弱可靠性。", "希望在订单事务提交后处理。"],
    "must": ["把事实与技术传输区分开。", "选择本地 Domain Event/进程内协调档位。", "检查事件在活跃事务内发布和提交后处理的时序。", "解释为什么当前不需要 Outbox。"],
    "forbidden": ["把 Domain Event Java 类直接声明为长期外部契约。"],
    "critical_forbidden": ["无条件要求 Outbox、消息代理或 Event Sourcing。"],
    "questions": [],
    "alternatives": ["直接的提交后回调也可接受，但必须说明弱可靠性和修复方式。"]
  },
  {
    "id": "integration-event",
    "skill": "java-spring-hex-playbook",
    "prompt": "订单上下文必须可靠通知履约上下文 OrderConfirmed，怎样设计才完整？",
    "fixture": ["订单和履约独立部署。", "消息代理提供至少一次投递。", "履约不能重复创建任务。", "订单提交成功后即使代理暂时不可用也必须最终发送。"],
    "must": ["明确外部契约所有者和版本策略。", "要求业务状态与 Outbox 原子写入。", "说明分区或顺序键。", "定义消费者幂等键。", "覆盖重试、DLQ 和未知结果。", "说明 at-least-once 语义。"],
    "forbidden": ["复用内部 Domain Event 类作为永久外部契约。"],
    "critical_forbidden": ["声称 exactly-once 可消除幂等，或把写入 Outbox 当成完整终点。"],
    "questions": ["同一订单的履约事件是否必须严格有序，还是仅需按任务幂等？"],
    "alternatives": ["事务日志/CDC relay 可替代轮询 relay，但不能省略消费者幂等。"]
  },
  {
    "id": "event-sourcing",
    "skill": "java-spring-hex-playbook",
    "prompt": "资金账户以事件流为事实来源，并通过异步投影提供余额查询，需要审查哪些正确性问题？",
    "fixture": ["命令可能并发修改同一账户。", "投影允许短暂延迟。", "必须支持从头重建投影。", "事件契约未来可能演进。"],
    "must": ["覆盖事件版本和乐观并发。", "要求回放处理幂等。", "定义投影检查点和重建流程。", "把快照作为条件化优化。", "覆盖 schema 演进和历史迁移。", "说明查询投影的一致性窗口。"],
    "forbidden": ["把快照当作事实来源。"],
    "critical_forbidden": ["把可变数据库行当作事件源，或省略投影失败恢复。"],
    "questions": ["业务是否要求按账户分区严格排序？", "允许的查询陈旧窗口是多少？"],
    "alternatives": ["低事件量时先不使用快照。", "使用 upcaster 或版本化处理器演进历史事件。"]
  }
]
```

- [ ] **Step 4: Create the semantic rubric**

Create `evals/rubric.md` with these sections and rules:

```markdown
# DDD Skill Behavior Rubric

## 输入隔离

运行者只接收目标技能、案例 prompt 和 fixture。不得向运行者泄露 must、forbidden、critical_forbidden、设计规格、候选修改或其他 run 的响应。

## 语义评分

评分不是逐字匹配。每个 must 行为按 0、0.5、1 计分：缺失为 0，提及但不可执行为 0.5，结合案例证据形成可复核决策为 1。questions 只在确有关键事实缺失时评分；无意义追问扣 0.5。

## 禁止行为

普通 forbidden 每项扣 1。任何关键禁行项出现时，该案例直接失败，并阻止候选版本发布。

## 通过门槛

总得分除以 must 满分得到百分比。静态和路由测试必须 100% 通过，行为评测必须至少 90%，且关键禁行项为零。

## 运行记录

每次 run 保存提交、日期、模型、技能路径、原始响应、分项得分、关键禁行项和与基线的变化。原始响应不得在评分前改写。
```

- [ ] **Step 5: Run the schema tests and verify they pass**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
```

Expected: `Ran 5 tests` and `OK`.

- [ ] **Step 6: Run isolated baseline evaluations**

For each case, start a fresh evaluation agent/session. Construct its message from that case's exact `skill`, `prompt`, and `fixture` values in `cases.json` using this shape:

```text
Use the named skill from its folder under C:\Users\tianquan\Desktop\ddd-hex-playbook to answer this request.

Then include the case's prompt verbatim.

Business facts:
- Include each fixture string as one bullet, without any other facts.
```

Do not send the whole `cases.json`, the rubric, this plan, the design specification, or expected outcomes. Save each raw final response unchanged to the response file named after that case ID. The ten filenames are `db-table-to-aggregate.md`, `ambiguous-account-contexts.md`, `order-payment-aggregate.md`, `simple-catalog-crud.md`, `order-lifecycle-rich-domain.md`, `simple-pagination-cqrs.md`, `operations-report-cqrs.md`, `local-domain-event.md`, `integration-event.md`, and `event-sourcing.md`.

Before the run, execute `git rev-parse HEAD`. Create `evals/runs/baseline/manifest.json` with `run_id` equal to `baseline`, `skill_commit` equal to that exact command output, `date` equal to `2026-07-15`, `cases_file` equal to `evals/cases.json`, and numeric `response_count` equal to `10`.

- [ ] **Step 7: Score the baseline without changing raw responses**

Give a separate grading agent only `evals/cases.json`, `evals/rubric.md`, and the ten response files. Save its itemized result as `evals/runs/baseline/scores.json` with top-level keys `run_id`, `case_scores`, `percentage`, and `critical_failures`. Verify `case_scores` has ten entries. Do not impose the 90% candidate gate on the baseline.

- [ ] **Step 8: Commit the corpus and baseline**

```powershell
git add tests/test_skill_contracts.py
git add -f evals/cases.json evals/rubric.md evals/runs/baseline
git commit -m "test: capture DDD skill behavior baseline"
```

---

### Task 3: Discovery, Context Mapping, and Handoff References

**Files:**
- Modify: `tests/test_skill_contracts.py`
- Create: `ddd-modeling/references/discovery-evidence.md`
- Create: `ddd-modeling/references/context-mapping.md`
- Create: `ddd-modeling/references/engineering-handoff.md`
- Modify: `ddd-modeling/references/bounded-contexts.md`

**Interfaces:**
- Consumes: Case semantics from Task 2.
- Produces: three direct, one-level references that Task 4 routes from `ddd-modeling/SKILL.md`.

- [ ] **Step 1: Add failing reference-content contracts**

Add this test:

```python
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
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```powershell
python -m unittest tests.test_skill_contracts.SkillContractTests.test_modeling_decision_references_define_approved_contracts -v
```

Expected: error for missing `discovery-evidence.md`.

- [ ] **Step 3: Write `discovery-evidence.md`**

Use these exact sections: `Core rule`, `Adaptive depth`, `Evidence timeline`, `Examples and counterexamples`, `Hotspots`, `Exit criteria`, `Common mistakes`. Require the evidence timeline columns `业务时间`, `参与者`, `命令`, `领域事件`, `策略/规则`, `外部系统`, and `证据来源`. State that a hotspot is an unresolved contradiction or missing fact, not an excuse to invent certainty. Exit discovery only when the evidence can support immediate-consistency claims; otherwise keep model boundaries provisional.

- [ ] **Step 4: Write `context-mapping.md`**

Use sections `Core rule`, `Context card`, `Small map`, `Relationship ledger`, `Pattern labels`, `Failure probes`, `Stop conditions`. Define the relationship ledger columns `上游`, `下游`, `事实权威`, `传播内容`, `契约定义方`, `翻译责任`, `一致性`, `失败责任`, and `热点`. Require facts before labels. Cover Partnership, Shared Kernel, Customer/Supplier, Conformist, Anticorruption Layer, Open Host Service, Published Language, and Separate Ways with one-sentence costs. Prohibit a complete enterprise map when the request concerns one integration.

- [ ] **Step 5: Write `engineering-handoff.md`**

Use sections `Purpose`, `Required handoff`, `Insufficient evidence`, `Do not freeze`, `Example skeleton`. The required handoff contains the nine approved fields from the design. The example skeleton is a Markdown template with headings and empty list markers represented as descriptive prompts, not invented domain facts. State that Java/Spring decisions must return to modeling when a missing business fact changes responsibility, transaction, or consistency.

- [ ] **Step 6: Narrow `bounded-contexts.md` to boundary evidence**

Keep `Core idea`, `Evidence`, `Procedure`, `Scenario probe`, and `Common mistakes`. Replace the current detailed `Relationship choices` table with one paragraph: after candidates survive language, rule, authority, and cross-boundary probes, use `context-mapping.md` for relationship analysis. Do not add a nested Markdown link from this reference; Task 4 provides the direct route from `SKILL.md`.

- [ ] **Step 7: Run all skill contracts**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
```

Expected: `Ran 6 tests` and `OK`.

- [ ] **Step 8: Commit the modeling references**

```powershell
git add tests/test_skill_contracts.py ddd-modeling/references
git commit -m "docs: add evidence-driven DDD modeling references"
```

---

### Task 4: Adaptive `ddd-modeling` Workflow and Output Contract

**Files:**
- Modify: `tests/test_skill_contracts.py`
- Modify: `ddd-modeling/SKILL.md`

**Interfaces:**
- Consumes: the three references from Task 3.
- Produces: a concise skill router with adaptive tracks and a fixed engineering handoff.

- [ ] **Step 1: Add failing router and output-contract tests**

Add:

```python
    def test_modeling_skill_routes_adaptive_tracks_and_handoff(self) -> None:
        text = read_utf8(SKILLS["ddd-modeling"] / "SKILL.md")
        for fragment in (
            "快速评审轨", "发现建模轨", "建模决策包", "工程交接契约",
            "references/discovery-evidence.md", "references/context-mapping.md",
            "references/engineering-handoff.md", "一次只追问一个问题",
            "不自动修改", "Direct-answer exception",
        ):
            self.assertIn(fragment, text)

    def test_modeling_skill_keeps_implementation_out_of_domain_output(self) -> None:
        text = read_utf8(SKILLS["ddd-modeling"] / "SKILL.md")
        self.assertIn("不规定 Java 包和框架", text)
        self.assertIn("被排除", text)
        self.assertIn("未验证", text)
```

- [ ] **Step 2: Run the focused tests and verify failure**

Run:

```powershell
python -m unittest tests.test_skill_contracts.SkillContractTests.test_modeling_skill_routes_adaptive_tracks_and_handoff tests.test_skill_contracts.SkillContractTests.test_modeling_skill_keeps_implementation_out_of_domain_output -v
```

Expected: both tests fail because adaptive-track and handoff terms are absent.

- [ ] **Step 3: Rewrite the workflow section minimally**

Keep the existing frontmatter, core principle, quick reference, and direct-answer exception. Replace `Workflow` with:

```markdown
## Track selection

- 已有具体场景或现有模型需要审查：使用快速评审轨。
- 业务事实不足、术语冲突或需要划分上下文：使用发现建模轨。
- 缺失答案会改变模型时，一次只追问一个问题。

## Fast review track

1. 读取现有模型、代码、`CONTEXT.md`、Context Map 与 ADR。
2. 提取已有场景、不变式和一致性边界。
3. 用反例挑战实体、聚合和上下文划分。
4. 输出保留、调整、被排除的设计及证据。

## Discovery track

1. 按 [discovery-evidence.md](references/discovery-evidence.md) 建立发现证据。
2. 澄清通用语言、不变式及即时/延迟/补偿规则。
3. 提出上下文、聚合和事件候选并用失败场景挑战。
4. 候选上下文稳定后，按 [context-mapping.md](references/context-mapping.md) 生成当前问题的小地图。
5. 按 [engineering-handoff.md](references/engineering-handoff.md) 形成工程交接契约。
```

Use the approved Chinese labels `快速评审轨` and `发现建模轨` in the headings or their first sentences so the contract remains readable.

- [ ] **Step 4: Replace the output contract**

Define `建模决策包` with the approved seven sections. Add stop conditions stating: do not derive aggregates from tables, do not invent missing facts, do not automatically modify project documents, and do not prescribe Java packages/frameworks. Keep direct routes to all five existing modeling references plus the three new references.

- [ ] **Step 5: Run all contracts**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
```

Expected: `Ran 8 tests` and `OK`.

- [ ] **Step 6: Commit the modeling workflow**

```powershell
git add tests/test_skill_contracts.py ddd-modeling/SKILL.md
git commit -m "feat: add adaptive DDD modeling decision workflow"
```

---

### Task 5: Local Architecture Strategies and CQRS Decision Ladder

**Files:**
- Modify: `tests/test_skill_contracts.py`
- Modify: `java-spring-hex-playbook/references/module-strategies.md`
- Create: `java-spring-hex-playbook/references/cqrs-decision-ladder.md`

**Interfaces:**
- Consumes: engineering handoff semantics from Task 3.
- Produces: per-context architecture selection and a six-level CQRS decision interface for Task 6 routing.

- [ ] **Step 1: Add failing local-strategy and CQRS contracts**

Add:

```python
    def test_local_architecture_reference_supports_three_context_strategies(self) -> None:
        text = read_utf8(
            SKILLS["java-spring-hex-playbook"] / "references" / "module-strategies.md"
        )
        for fragment in (
            "简单 CRUD", "Transaction Script", "富领域模型", "Hexagonal",
            "独立查询", "CQRS Read Side", "同一个部署单元", "局部架构",
            "不要求每个上下文内部复制六层",
        ):
            self.assertIn(fragment, text)

    def test_cqrs_reference_defines_minimum_sufficient_ladder(self) -> None:
        text = read_utf8(
            SKILLS["java-spring-hex-playbook"] / "references" / "cqrs-decision-ladder.md"
        )
        for fragment in (
            "最小充分复杂度", "分离命令与查询方法", "分离 Command/Query 接口",
            "共用数据库", "异步投影", "独立读写存储", "Event Sourcing",
            "一致性窗口", "投影恢复", "运维成本", "不是成熟度阶梯",
            "为什么不选择更高一级",
        ):
            self.assertIn(fragment, text)
```

- [ ] **Step 2: Run focused tests and verify failure**

Run:

```powershell
python -m unittest tests.test_skill_contracts.SkillContractTests.test_local_architecture_reference_supports_three_context_strategies tests.test_skill_contracts.SkillContractTests.test_cqrs_reference_defines_minimum_sufficient_ladder -v
```

Expected: first test fails on missing local-strategy wording; second errors because `cqrs-decision-ladder.md` does not exist.

- [ ] **Step 3: Extend `module-strategies.md`**

Preserve existing package-only, boundary-module, and published-contract profiles. Add a separate `Local architecture by bounded context` section with a decision table for:

- simple CRUD/Transaction Script: few rules, simple lifecycle, no artificial aggregate/event;
- rich domain/Hexagonal: explicit invariant and lifecycle, aggregate consistency, application orchestration, consumer-owned ports;
- independent query/CQRS Read Side: list/report/query shape, no aggregate reconstruction for reads.

State verbatim: `同一个部署单元可以同时包含三种局部架构；六模块不要求每个上下文内部复制六层。`

- [ ] **Step 4: Create `cqrs-decision-ladder.md`**

Use sections `Core rule`, `Decision ladder`, `Evidence required`, `Projection recovery`, `Stop rule`, `Common mistakes`. The decision ladder table contains levels 1–6 exactly as approved. For every recommendation require current level, upgrade evidence, query/read-shape difference, consistency window, recovery, operational cost, and why the next level is not selected. State that most systems should remain at levels 1 or 2 unless evidence justifies more.

- [ ] **Step 5: Run all contracts**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
```

Expected: `Ran 10 tests` and `OK`.

- [ ] **Step 6: Commit local architecture and CQRS guidance**

```powershell
git add tests/test_skill_contracts.py java-spring-hex-playbook/references/module-strategies.md java-spring-hex-playbook/references/cqrs-decision-ladder.md
git commit -m "docs: add local architecture and CQRS decisions"
```

---

### Task 6: Three-Level Event Review and Engineering Skill Routing

**Files:**
- Modify: `tests/test_skill_contracts.py`
- Modify: `java-spring-hex-playbook/references/transactions-and-events.md`
- Modify: `java-spring-hex-playbook/SKILL.md`

**Interfaces:**
- Consumes: `engineering-handoff.md`, `module-strategies.md`, and `cqrs-decision-ladder.md`.
- Produces: the complete engineering decision workflow and event-profile output used in candidate evaluations.

- [ ] **Step 1: Add failing event and routing contracts**

Add:

```python
    def test_event_reference_uses_three_semantic_profiles(self) -> None:
        text = read_utf8(
            SKILLS["java-spring-hex-playbook"] / "references" / "transactions-and-events.md"
        )
        for fragment in (
            "本地 Domain Event", "可靠 Integration Event", "Event Sourcing",
            "事实所有者", "契约版本", "分区键", "消费者幂等键", "DLQ",
            "乐观并发", "检查点", "投影重建", "schema 演进",
            "Outbox 不是终点", "按事件语义裁剪",
        ):
            self.assertIn(fragment, text)

    def test_engineering_skill_checks_handoff_and_routes_decisions(self) -> None:
        text = read_utf8(SKILLS["java-spring-hex-playbook"] / "SKILL.md")
        for fragment in (
            "工程交接契约", "退回 `ddd-modeling`", "局部架构",
            "references/module-strategies.md", "references/cqrs-decision-ladder.md",
            "三档事件审查", "当前建议停在哪一级", "为什么不选择更高一级",
            "不自行补造业务事实",
        ):
            self.assertIn(fragment, text)
```

- [ ] **Step 2: Run focused tests and verify failure**

Run:

```powershell
python -m unittest tests.test_skill_contracts.SkillContractTests.test_event_reference_uses_three_semantic_profiles tests.test_skill_contracts.SkillContractTests.test_engineering_skill_checks_handoff_and_routes_decisions -v
```

Expected: both tests fail because the current reference and skill do not expose the approved profiles/routes.

- [ ] **Step 3: Extend `transactions-and-events.md`**

Keep the existing transaction decision sequence, Spring timing warning, reliable integration flow, and remote-call failure semantics. Add `Choose the event profile first` before technical implementation:

- local Domain Event: fact name/owner, aggregate source, technical independence, transaction-time collection;
- reliable Integration Event: external contract/version, Outbox atomicity, partition/order key, consumer idempotency, retry/DLQ/unknown result, compatibility;
- Event Sourcing: event version, optimistic concurrency, checkpoint, replay idempotency, projection rebuild, conditional snapshots, schema evolution/history migration.

Include the exact rules `按事件语义裁剪审查项` and `Outbox 不是终点`; state that Event Sourcing requirements do not automatically apply to local events.

- [ ] **Step 4: Update the engineering workflow and output contract**

In `java-spring-hex-playbook/SKILL.md`:

1. Check the engineering handoff first; return to `ddd-modeling` when missing facts change responsibility, transaction, or consistency.
2. Select local architecture per context via `module-strategies.md`.
3. Trace owners, dependencies, transactions, and consistency.
4. Route query separation to `cqrs-decision-ladder.md`.
5. Route events to the three-profile review in `transactions-and-events.md`.
6. Keep technology-specific routing unchanged.

Extend output with business evidence, local strategy, current CQRS level and upgrade trigger, event profile/correctness checklist, rejected alternatives, executable verification, and questions returned to modeling. Keep the existing priority rule and architecture invariants.

- [ ] **Step 5: Run all skill and generator unit contracts**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
python -m unittest discover -s ddd-project-create/tests -p "test_*.py" -v
```

Expected: all skill contracts pass; existing generator suite reports `Ran 24 tests` and `OK`.

- [ ] **Step 6: Commit event review and routing**

```powershell
git add tests/test_skill_contracts.py java-spring-hex-playbook/SKILL.md java-spring-hex-playbook/references/transactions-and-events.md
git commit -m "feat: grade Java DDD architecture and event decisions"
```

---

### Task 7: Candidate Evaluation, Documentation, and Full Verification

**Files:**
- Modify: `README.md`
- Create: `evals/runs/candidate/manifest.json`
- Create: ten files in `evals/runs/candidate/responses/`, using the same exact filenames as the baseline run
- Create: `evals/runs/candidate/scores.json`
- Create: `evals/runs/candidate/comparison.md`

**Interfaces:**
- Consumes: identical cases/rubric from Task 2 and revised skills from Tasks 3–6.
- Produces: publication evidence and user-facing workflow documentation.

- [ ] **Step 1: Update README without changing the generator promise**

Update the collaboration chain to show `建模决策包 / 工程交接契约` between the first two skills. Document adaptive modeling tracks, small Context Maps, local architecture choice, CQRS levels, and event profiles. Add verification commands:

```powershell
python -m unittest tests.test_skill_contracts -v
python -m unittest discover -s ddd-project-create/tests -p "test_*.py" -v
```

State that the first optimization round does not change the generated six-module template and that `/evals/runs/` stores isolated behavioral evidence.

- [ ] **Step 2: Run isolated candidate evaluations**

Repeat Task 2 Step 6 with a fresh runner for every case and the revised skill paths. Do not reuse baseline conversations. Save unchanged outputs under `evals/runs/candidate/responses/`.

After Task 6, execute `git rev-parse HEAD`. Create `evals/runs/candidate/manifest.json` with `run_id` equal to `candidate`, `skill_commit` equal to that exact command output, `date` equal to `2026-07-15`, `cases_file` equal to `evals/cases.json`, `baseline_run` equal to `evals/runs/baseline`, and numeric `response_count` equal to `10`.

- [ ] **Step 3: Score the candidate independently and enforce the gate**

Give a fresh grading agent only the corpus, rubric, and candidate responses. Save `scores.json` with the same schema as baseline. Verify:

- ten case scores exist;
- `percentage >= 90`;
- `critical_failures` is an empty array.

If the gate fails, stop publication, add a regression case when the failure reveals an uncovered behavior, make the smallest skill/reference correction, rerun affected contracts, and rerun all ten candidate cases in fresh sessions. Do not edit raw failed responses.

- [ ] **Step 4: Write the baseline comparison**

Create `evals/runs/candidate/comparison.md` with:

- baseline and candidate commit hashes;
- total percentage and critical-failure count for each run;
- a ten-row table with baseline score, candidate score, and observed decision-behavior change;
- regressions or unchanged weaknesses;
- confirmation that runners received no expected answers;
- publication decision.

- [ ] **Step 5: Run skill-creator validation in a temporary dependency directory**

Run:

```powershell
python -m pip install --disable-pip-version-check --target "$env:TEMP\ddd-skill-validator" PyYAML
$env:PYTHONPATH="$env:TEMP\ddd-skill-validator"
python -X utf8 "$HOME\.codex\skills\.system\skill-creator\scripts\quick_validate.py" ddd-modeling
python -X utf8 "$HOME\.codex\skills\.system\skill-creator\scripts\quick_validate.py" java-spring-hex-playbook
python -X utf8 "$HOME\.codex\skills\.system\skill-creator\scripts\quick_validate.py" ddd-project-create
```

Expected: each validator prints `Skill is valid!` and exits 0. Do not add PyYAML to the repository.

- [ ] **Step 6: Run full deterministic and Maven verification**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
python -m unittest discover -s ddd-project-create/tests -p "test_*.py" -v
& .\ddd-project-create\assets\project-template\mvnw.cmd `
  -f .\ddd-project-create\assets\project-template\pom.xml verify
git diff --check
git status --short
```

Expected:

- all skill contracts pass;
- generator suite reports 24 passing tests;
- Maven reactor ends with `BUILD SUCCESS`;
- `git diff --check` emits no errors;
- status contains only Task 7's intended README and evaluation artifacts.

- [ ] **Step 7: Commit documentation and publication evidence**

```powershell
git add README.md
git add -f evals/runs/candidate
git commit -m "docs: publish DDD skill evaluation evidence"
```

- [ ] **Step 8: Final clean verification**

Run:

```powershell
python -m unittest tests.test_skill_contracts -v
python -m unittest discover -s ddd-project-create/tests -p "test_*.py" -v
git status --short
git log --oneline -7
```

Expected: both test suites pass, `git status --short` is empty, and the log shows one focused commit for each task boundary.

---

## Spec Coverage Checklist

- Adaptive quick-review/discovery tracks: Task 4.
- Structured modeling decision package: Tasks 3–4.
- Small Context Map and relationship ledger: Task 3.
- Fixed engineering handoff and return-to-modeling rule: Tasks 3, 4, and 6.
- Per-context local architecture: Task 5.
- Six-level CQRS ladder: Task 5.
- Three event review profiles: Task 6.
- Static, routing, and behavioral evaluation: Tasks 1–2 and 7.
- UTF-8 UI metadata: Task 1.
- Frozen generator/template and full regression: Global Constraints and Task 7.
- Before/after evidence: Tasks 2 and 7.
