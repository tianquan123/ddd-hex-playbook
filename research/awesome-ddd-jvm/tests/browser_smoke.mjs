import { createRequire } from "node:module";
import os from "node:os";
import path from "node:path";

const require = createRequire(import.meta.url);
const { chromium } = require(process.env.PLAYWRIGHT_MODULE);
const baseUrl = process.env.REPORT_BASE_URL || "http://127.0.0.1:8765";
const edge = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";

const browser = await chromium.launch({ headless: true, executablePath: edge });
const page = await browser.newPage({ viewport: { width: 1440, height: 960 } });
const consoleErrors = [];
page.on("console", (message) => {
  if (message.type() === "error") consoleErrors.push(message.text());
});

await page.goto(`${baseUrl}/index.html`, { waitUntil: "networkidle" });
if (!(await page.title()).includes("JVM")) throw new Error("homepage title is missing JVM");
if ((await page.locator("main").count()) !== 1) throw new Error("homepage must contain one main");

await page.locator("header a[href='resources.html']").click();
await page.waitForLoadState("networkidle");
const allCount = await page.locator("[data-tags]:visible").count();
await page.locator("button[data-filter='events']").click();
const eventCount = await page.locator("[data-tags]:visible").count();
if (!(eventCount > 0 && eventCount < allCount)) throw new Error("resource filter did not narrow results");
if ((await page.locator("button[data-filter='events']").getAttribute("aria-pressed")) !== "true") {
  throw new Error("resource filter did not expose pressed state");
}
if ((await page.locator("[data-tags][hidden]").count()) !== allCount - eventCount) {
  throw new Error("resource filter hidden count is inconsistent");
}

await page.goto(`${baseUrl}/projects.html`, { waitUntil: "networkidle" });
const firstDetails = page.locator("details[data-code]").first();
await firstDetails.locator("summary").click();
if ((await firstDetails.getAttribute("open")) === null) throw new Error("code details did not open");
if ((await firstDetails.locator("summary").getAttribute("aria-expanded")) !== "true") {
  throw new Error("code details did not update aria-expanded");
}
if (!(await firstDetails.locator("pre").isVisible())) throw new Error("opened code example is not visible");

await page.setViewportSize({ width: 390, height: 844 });
await page.goto(`${baseUrl}/index.html`, { waitUntil: "networkidle" });
const overflow = await page.evaluate(
  () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
);
if (overflow > 1) throw new Error(`mobile horizontal overflow: ${overflow}px`);

await page.emulateMedia({ media: "print" });
const headerDisplay = await page.locator(".site-header").evaluate((el) => getComputedStyle(el).display);
if (headerDisplay !== "none") throw new Error("print stylesheet does not hide navigation");
await page.screenshot({ path: path.join(os.tmpdir(), "ddd-jvm-report-mobile.png"), fullPage: true });

if (consoleErrors.length) throw new Error(`browser console errors: ${consoleErrors.join(" | ")}`);
await browser.close();

console.log(`browser smoke passed: resources=${allCount}, events=${eventCount}, mobile-overflow=${overflow}px`);
