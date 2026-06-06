import { spawn } from "node:child_process";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repoRoot = dirname(dirname(fileURLToPath(import.meta.url)));
const apiRoot = join(repoRoot, "apps", "api");
const command = process.platform === "win32" ? "mvnw.cmd" : "./mvnw";
const args = process.argv.slice(2);

const child = spawn(command, args, {
  cwd: apiRoot,
  shell: process.platform === "win32",
  stdio: "inherit",
});

child.on("exit", (code) => {
  process.exit(code ?? 1);
});
