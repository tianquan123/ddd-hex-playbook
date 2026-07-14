# Awesome DDD JVM Research Report Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a source-backed, offline static HTML report that distills JVM DDD sample projects and community material into actionable guidance for the repository's three skills.

**Architecture:** The report lives under `research/awesome-ddd-jvm/` and uses five focused HTML pages sharing one stylesheet and one dependency-free script. A Python `unittest` contract suite parses every page, checks navigation, required sections, source links, local assets, file size, and content invariants; browser verification covers interaction and responsive rendering.

**Tech Stack:** HTML5, CSS3, vanilla JavaScript, Python 3 standard library `unittest` and `html.parser`, browser-based visual verification.

## Global Constraints

- Analyze JVM/Java sample projects only; Blogs and Community Resources may provide language-independent supporting ideas.
- Deeply analyze 6–8 core JVM projects; index the remaining useful JVM links more briefly.
- Keep each HTML file below 100 KB.
- Use no frontend framework, CDN, remote font, build tool, or third-party runtime dependency.
- Important claims must link to an original repository, official project documentation, representative source/test file, or an article directly referenced by Awesome DDD.
- Short code excerpts must be attributed and limited to what is needed to support commentary.
- Separate stable DDD principles, team defaults, conditional choices, and examples.
- Do not modify existing skills or generated-project templates in this implementation.

---

## File Map

- Create `research/awesome-ddd-jvm/index.html`: report entry point, executive synthesis, reading paths, and navigation.
- Create `research/awesome-ddd-jvm/projects.html`: seven core JVM case studies with evidence and limitations.
- Create `research/awesome-ddd-jvm/patterns.html`: cross-project comparison of eight design themes.
- Create `research/awesome-ddd-jvm/resources.html`: categorized JVM project, blog, and community resource index.
- Create `research/awesome-ddd-jvm/skill-gaps.html`: evidence-backed gap matrix for the three repository skills.
- Create `research/awesome-ddd-jvm/assets/styles.css`: visual system, layout, responsive, print, focus, and code styles.
- Create `research/awesome-ddd-jvm/assets/app.js`: navigation state, resource filtering, and code disclosure helpers.
- Create `research/awesome-ddd-jvm/source-notes.md`: checked URLs, access date, evidence locations, and caveats used to author the report.
- Create `research/awesome-ddd-jvm/tests/test_report.py`: offline structural and content contract tests.

### Task 1: Verify sources and build the evidence matrix

**Files:**
- Create: `research/awesome-ddd-jvm/source-notes.md`

**Interfaces:**
- Consumes: the approved design and the Awesome DDD JVM/Blogs/Community Resources sections.
- Produces: one evidence record per core project with `URL`, `Accessed`, `Business`, `Architecture`, `Consistency`, `Tests`, `Code`, and `Caveat` fields for Tasks 3–5.

- [ ] **Step 1: Create the source note schema and fixed candidate list**

```markdown
# Source Notes

Accessed: 2026-07-14

## Core projects

### ddd-by-examples/library
- URL: https://github.com/ddd-by-examples/library
- Business:
- Architecture:
- Consistency:
- Tests:
- Code:
- Caveat:
```

Repeat the same fields for `citerus/dddsample-core`, `eclipse-ee4j/cargotracker`, `VaughnVernon/IDDD_Samples`, `asc-lab/java-cqrs-intro`, `andreschaffer/event-sourcing-cqrs-examples`, and `mkopylec/project-manager`. Add `humank/ddd-practitioners-ref` under supporting workflows.

- [ ] **Step 2: Inspect each core repository README, source tree, and representative tests**

For each record, write concise factual notes and at least two precise evidence URLs. Mark author statements as `stated` and report interpretations as `inference`. Record age, incomplete tests, or teaching-only status under `Caveat`.

- [ ] **Step 3: Verify supporting sources**

Add records for Awesome DDD, DDD Crew Context Mapping, Effective Aggregate Design, DDD Heuristics, EventStorming by Alberto Brandolini, and any other selected Blog/Community source. Each record must state which report theme it supports.

- [ ] **Step 4: Run a source-note completeness check**

Run:

```powershell
$text = Get-Content -Raw -Encoding UTF8 research/awesome-ddd-jvm/source-notes.md
@('Business:','Architecture:','Consistency:','Tests:','Code:','Caveat:') | ForEach-Object { if (-not $text.Contains($_)) { throw "Missing $_" } }
```

Expected: command exits successfully with no output.

- [ ] **Step 5: Commit the evidence matrix**

```powershell
git add research/awesome-ddd-jvm/source-notes.md
git commit -m "docs: collect JVM DDD research evidence"
```

### Task 2: Establish report contracts and the shared page shell

**Files:**
- Create: `research/awesome-ddd-jvm/tests/test_report.py`
- Create: `research/awesome-ddd-jvm/assets/styles.css`
- Create: `research/awesome-ddd-jvm/assets/app.js`
- Create: `research/awesome-ddd-jvm/index.html`
- Create: `research/awesome-ddd-jvm/projects.html`
- Create: `research/awesome-ddd-jvm/patterns.html`
- Create: `research/awesome-ddd-jvm/resources.html`
- Create: `research/awesome-ddd-jvm/skill-gaps.html`

**Interfaces:**
- Produces: a shared navigation contract using `.site-nav`, `aria-current="page"`, `assets/styles.css`, and `assets/app.js`; filter controls use `[data-filter]` and resource cards use `[data-tags]`.

- [ ] **Step 1: Write failing offline report contract tests**

Create a `unittest.TestCase` that defines:

```python
ROOT = Path(__file__).resolve().parents[1]
PAGES = ["index.html", "projects.html", "patterns.html", "resources.html", "skill-gaps.html"]
REQUIRED_TITLES = {
    "index.html": "JVM DDD 实践地图",
    "projects.html": "JVM 核心样例",
    "patterns.html": "跨项目设计模式",
    "resources.html": "延伸资源索引",
    "skill-gaps.html": "技能差距与优化候选",
}
```

Tests must assert that every page exists, is below `100 * 1024` bytes, declares `lang="zh-CN"`, references both local assets, links all five pages, contains its required title, has exactly one `main`, contains no `http` stylesheet/script dependency, and contains no `TBD` or `TODO`. Add tests requiring at least seven `.project-study` elements in `projects.html`, eight `[data-pattern]` sections in `patterns.html`, twelve `[data-tags]` resource entries in `resources.html`, and six `.gap-row` entries in `skill-gaps.html`.

- [ ] **Step 2: Run tests and verify they fail because pages do not exist**

Run:

```powershell
python -m unittest discover -s research/awesome-ddd-jvm/tests -p "test_*.py" -v
```

Expected: FAIL with missing `index.html` and other report files.

- [ ] **Step 3: Implement the common HTML shell and minimal assets**

Each page must include this structure with its own title and active link:

```html
<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="assets/styles.css">
  <script src="assets/app.js" defer></script>
</head>
<body>
  <a class="skip-link" href="#content">跳到正文</a>
  <header class="site-header">
    <a class="brand" href="index.html">DDD / JVM FIELD NOTES</a>
    <nav class="site-nav" aria-label="主导航">
      <a href="index.html" aria-current="page">总览</a>
      <a href="projects.html">核心项目</a>
      <a href="patterns.html">设计模式</a>
      <a href="resources.html">资源索引</a>
      <a href="skill-gaps.html">技能差距</a>
    </nav>
  </header>
  <main id="content"><h1>JVM DDD 实践地图</h1></main>
  <footer>资料核对日期：2026-07-14</footer>
</body>
</html>
```

Implement CSS variables for paper, ink, orange, teal, rule, and muted colors; a desktop side-rail/grid layout; mobile single-column layout at `760px`; visible `:focus-visible`; reduced-motion support; print styles; code blocks; tags; tables; and disclosure panels. Implement JavaScript that sets `aria-pressed` on filter buttons and toggles `hidden` on resource cards based on `data-filter`/`data-tags`.

- [ ] **Step 4: Run tests and confirm only content-count contracts fail**

Run the unittest command from Step 2.

Expected: file, navigation, asset, language, title, and size tests PASS; project/pattern/resource/gap count tests FAIL.

- [ ] **Step 5: Commit the tested report shell**

```powershell
git add research/awesome-ddd-jvm
git commit -m "feat: establish JVM DDD report shell"
```

### Task 3: Author core project studies and cross-project patterns

**Files:**
- Modify: `research/awesome-ddd-jvm/projects.html`
- Modify: `research/awesome-ddd-jvm/patterns.html`
- Modify: `research/awesome-ddd-jvm/tests/test_report.py`

**Interfaces:**
- Consumes: evidence records from `source-notes.md`.
- Produces: seven `.project-study` articles with stable IDs and eight `[data-pattern]` thematic sections linked from later pages.

- [ ] **Step 1: Add failing semantic-content tests**

Assert that `projects.html` contains all seven repository names and the labels `业务问题`, `模型与边界`, `一致性`, `测试证据`, `局限`, and `可迁移结论`. Assert that `patterns.html` contains `Event Storming`, `限界上下文`, `聚合与不变式`, `端口与适配器`, `CQRS`, `事件与一致性`, `持久化与查询`, and `测试与架构守护`, plus all four rule labels `稳定原则`, `团队默认值`, `条件化方案`, and `示例`.

- [ ] **Step 2: Run the focused tests and verify failure**

Run:

```powershell
python -m unittest discover -s research/awesome-ddd-jvm/tests -p "test_*.py" -v
```

Expected: semantic-content assertions FAIL.

- [ ] **Step 3: Write seven project studies**

Use stable IDs `library`, `dddsample`, `cargotracker`, `iddd-samples`, `java-cqrs-intro`, `es-cqrs-examples`, and `project-manager`. Each article must contain the six labels tested above, two or more source links, an age/fitness badge, and one short attributed code or directory excerpt where evidence is available. Explicitly distinguish author statements from report inference.

- [ ] **Step 4: Write eight pattern comparisons**

For each theme, show: observed evidence across at least two projects, a compact comparison, the appropriate rule level, what not to generalize, and a link back to the relevant project study. Include the key conclusion that bounded contexts may choose different local architectures and that CQRS should be introduced incrementally in response to query/write pressure.

- [ ] **Step 5: Run tests and confirm project and pattern contracts pass**

Run the unittest command from Step 2.

Expected: project count, pattern count, and semantic-content tests PASS.

- [ ] **Step 6: Commit the case studies**

```powershell
git add research/awesome-ddd-jvm/projects.html research/awesome-ddd-jvm/patterns.html research/awesome-ddd-jvm/tests/test_report.py
git commit -m "docs: add JVM DDD case studies and patterns"
```

### Task 4: Author the resource index, synthesis, and skill gap matrix

**Files:**
- Modify: `research/awesome-ddd-jvm/resources.html`
- Modify: `research/awesome-ddd-jvm/skill-gaps.html`
- Modify: `research/awesome-ddd-jvm/index.html`
- Modify: `research/awesome-ddd-jvm/tests/test_report.py`

**Interfaces:**
- Consumes: project study IDs and pattern section IDs from Task 3.
- Produces: filterable resource cards, six or more evidence-backed gap rows, and homepage reading routes.

- [ ] **Step 1: Add failing synthesis tests**

Require resource filters `全部`, `建模`, `边界`, `事件`, `测试`, and `演进`. Require each `.gap-row` to contain `现状`, `证据`, `建议`, `落点`, and `风险`. Require homepage phrases `先发现，再建模`, `让边界可执行`, `复杂度按需引入`, and links to every core study and gap section.

- [ ] **Step 2: Run tests and verify synthesis assertions fail**

Run the complete unittest command.

Expected: new resource, gap, and homepage assertions FAIL.

- [ ] **Step 3: Build the resource index**

Create at least twelve `[data-tags]` entries split among JVM Samples, Blogs, and Community Resources. Each entry includes title, one-sentence use, priority (`必读`, `选读`, or `参考`), source domain, checked date, and external link. Filter tags must use the exact values `modeling`, `boundaries`, `events`, `testing`, and `evolution`.

- [ ] **Step 4: Build the skill gap matrix**

Create rows for collaborative discovery/example mapping, context mapping, variable local architecture per bounded context, progressive CQRS, event ordering/idempotency/projection recovery, and executable architecture guardrails. Map each recommendation to a concrete target file or skill section, assign confidence (`高` or `中`), cite at least two sources where the recommendation is broad, and state the risk of adoption or reason to defer.

- [ ] **Step 5: Write the homepage synthesis**

Summarize no more than eight core findings, show the pipeline `业务发现 → 模型边界 → 用例编排 → 端口适配 → 一致性 → 可执行守护`, and provide three reading routes: 15-minute overview, architecture decision review, and skill optimization workshop.

- [ ] **Step 6: Run the complete tests and confirm they pass**

Run the complete unittest command.

Expected: all tests PASS.

- [ ] **Step 7: Commit the complete report content**

```powershell
git add research/awesome-ddd-jvm
git commit -m "docs: complete JVM DDD research report"
```

### Task 5: Verify interaction, accessibility, links, and presentation

**Files:**
- Modify if needed: `research/awesome-ddd-jvm/assets/styles.css`
- Modify if needed: `research/awesome-ddd-jvm/assets/app.js`
- Modify if needed: `research/awesome-ddd-jvm/*.html`
- Modify if needed: `research/awesome-ddd-jvm/tests/test_report.py`

**Interfaces:**
- Consumes: the complete static report.
- Produces: a verified offline report whose navigation, filtering, disclosures, and responsive layout work without console errors.

- [ ] **Step 1: Run automated structural verification**

```powershell
python -m unittest discover -s research/awesome-ddd-jvm/tests -p "test_*.py" -v
git diff --check
```

Expected: all tests PASS and `git diff --check` emits no errors.

- [ ] **Step 2: Open `index.html` in a browser at desktop width**

Verify the executive summary is visible without interaction, every navigation link opens the correct local file, external source links open a new tab, and the browser console contains no errors.

- [ ] **Step 3: Verify resource filters and disclosures**

On `resources.html`, activate each filter and confirm only matching `[data-tags]` cards remain visible and `aria-pressed` follows the active filter. On `projects.html`, expand and collapse every code disclosure using keyboard input.

- [ ] **Step 4: Verify narrow-screen and print presentation**

At approximately `390px` width, confirm there is no horizontal page overflow, navigation remains usable, tables can scroll locally, and code remains readable. Open print preview and confirm navigation/filter controls are hidden while citations and URLs remain legible.

- [ ] **Step 5: Re-run tests after visual fixes**

Run the commands from Step 1.

Expected: all tests PASS and no whitespace errors.

- [ ] **Step 6: Commit verification fixes**

```powershell
git add research/awesome-ddd-jvm
git commit -m "fix: polish and verify JVM DDD report"
```
