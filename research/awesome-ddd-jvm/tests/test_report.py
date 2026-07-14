from html.parser import HTMLParser
from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[1]
PAGES = [
    "index.html",
    "projects.html",
    "patterns.html",
    "resources.html",
    "skill-gaps.html",
]
REQUIRED_TITLES = {
    "index.html": "JVM DDD 实践地图",
    "projects.html": "JVM 核心样例",
    "patterns.html": "跨项目设计模式",
    "resources.html": "延伸资源索引",
    "skill-gaps.html": "技能差距与优化候选",
}


class DocumentParser(HTMLParser):
    def __init__(self):
        super().__init__()
        self.main_count = 0
        self.links = []
        self.stylesheets = []
        self.scripts = []
        self.lang = None
        self.class_names = []
        self.data_patterns = 0
        self.data_tags = 0

    def handle_starttag(self, tag, attrs):
        values = dict(attrs)
        if tag == "html":
            self.lang = values.get("lang")
        if tag == "main":
            self.main_count += 1
        if tag == "a" and values.get("href"):
            self.links.append(values["href"])
        if tag == "link" and values.get("rel") == "stylesheet":
            self.stylesheets.append(values.get("href", ""))
        if tag == "script" and values.get("src"):
            self.scripts.append(values["src"])
        self.class_names.extend(values.get("class", "").split())
        if "data-pattern" in values:
            self.data_patterns += 1
        if "data-tags" in values:
            self.data_tags += 1


class ReportContractTests(unittest.TestCase):
    def read_page(self, name):
        path = ROOT / name
        self.assertTrue(path.exists(), f"missing report page: {name}")
        return path.read_text(encoding="utf-8")

    def parse_page(self, name):
        parser = DocumentParser()
        parser.feed(self.read_page(name))
        return parser

    def test_all_pages_exist_and_stay_small(self):
        for name in PAGES:
            with self.subTest(page=name):
                path = ROOT / name
                self.assertTrue(path.exists(), f"missing report page: {name}")
                self.assertLess(path.stat().st_size, 100 * 1024)

    def test_every_page_has_local_assets_navigation_and_single_main(self):
        for name in PAGES:
            with self.subTest(page=name):
                parser = self.parse_page(name)
                self.assertEqual(parser.lang, "zh-CN")
                self.assertEqual(parser.main_count, 1)
                self.assertIn("assets/styles.css", parser.stylesheets)
                self.assertIn("assets/app.js", parser.scripts)
                for page in PAGES:
                    self.assertIn(page, parser.links)
                self.assertFalse(any(url.startswith("http") for url in parser.stylesheets))
                self.assertFalse(any(url.startswith("http") for url in parser.scripts))

    def test_pages_have_expected_titles_and_no_unfinished_markers(self):
        forbidden = ("T" + "ODO", "T" + "BD")
        for name, title in REQUIRED_TITLES.items():
            with self.subTest(page=name):
                text = self.read_page(name)
                self.assertIn(title, text)
                self.assertFalse(any(marker in text for marker in forbidden))

    def test_content_pages_have_required_entry_counts(self):
        projects = self.parse_page("projects.html")
        patterns = self.parse_page("patterns.html")
        resources = self.parse_page("resources.html")
        gaps = self.parse_page("skill-gaps.html")
        self.assertGreaterEqual(projects.class_names.count("project-study"), 7)
        self.assertGreaterEqual(patterns.data_patterns, 8)
        self.assertGreaterEqual(resources.data_tags, 12)
        self.assertGreaterEqual(gaps.class_names.count("gap-row"), 6)

    def test_local_asset_references_resolve(self):
        for name in PAGES:
            with self.subTest(page=name):
                parser = self.parse_page(name)
                for reference in parser.stylesheets + parser.scripts:
                    self.assertTrue((ROOT / reference).exists(), reference)


if __name__ == "__main__":
    unittest.main()
