# TACZ-LeawindTPS-Compat 另一台机器初始化说明

本文说明如何在另一台机器上把当前仓库的默认 `harness + ce + graphify` workflow 跑起来。

## 你最终会得到什么

- `AGENTS.md` 里的默认入口、快速路由和 current truth 约束
- `docs/current-truth/`、`docs/harness/tacz-leawindtps-compat-builder.md`、`.tacz-leawindtps-builder/` 组成的 workflow 上下文
- `.githooks/`、`.codex/hooks.json`、`graphify-out/` 组成的 repo-local Graphify 自动化
- 当前仓库默认 close-out 命令：
  - `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`

## 前提条件

- Git
- Python 3
- 可用的 Java 17+ / Gradle 环境
- Codex 已安装，并且本机具备：
  - `ce:brainstorm`
  - `ce:plan`
  - `ce:work`
  - `ce:review`

## 第一步：clone 仓库

```bash
git clone <your-repo-url>
cd TACZ-LeawindTPS-Compat
```

## 第二步：先读仓库入口

新机器上的第一轮阅读顺序保持和 `AGENTS.md` 一致：

1. `AGENTS.md`
2. 当前机器对应的 `AGENTS.win.md` 或 `AGENTS.mac.md`
3. `docs/current-truth/README.md`
4. `docs/current-truth/2026-05-03-baseline-requirements.md`
5. `docs/harness/tacz-leawindtps-compat-builder.md`
6. `.tacz-leawindtps-builder/spec.md`
7. `.tacz-leawindtps-builder/feature_list.json`
8. `.tacz-leawindtps-builder/sprint_plan.json`
9. `.tacz-leawindtps-builder/progress.md`

## 第三步：初始化 Graphify 本地自动化

只需要执行一次：

```bash
python3 scripts/dev/setup_graphify_local.py
```

这一步会自动完成：

- 安装或复用本机 `graphifyy`
- 配置本地 `git config --local core.hooksPath .githooks`
- 补齐本机 `.codex/hooks.json`
- 如果本地还没有 `graphify-out/graph.json`，自动生成首轮代码图

## 第四步：验证初始化是否成功

至少确认下面几件事成立：

```bash
git config --local --get core.hooksPath
python3 -m graphify query "show the main flow"
```

预期检查点：

- `git config --local --get core.hooksPath` 返回 `.githooks`
- `.codex/hooks.json` 已存在
- `graphify-out/graph.json` 和 `graphify-out/GRAPH_REPORT.md` 已存在
- `python3 -m graphify query "show the main flow"` 能返回结果

## 第五步：验证仓库工具链

至少跑一条仓库级验证，确认新机器的 Java / Gradle 环境没有偏掉：

```bash
./gradlew test
```

如果当前任务涉及客户端行为，也可以进一步跑：

```bash
./gradlew runClient
```

## 第六步：日常怎么继续

平时不需要手工重复维护 workflow。新的会话窗口默认按仓库规则推进：

1. 读 `AGENTS.md`
2. 读 `docs/harness/tacz-leawindtps-compat-builder.md`
3. 读 `.tacz-leawindtps-builder/`
4. 根据当前任务自动选择 `ce:plan`、`ce:work`、`ce:review`
5. 如果本轮涉及代码文件，收尾执行：
   - `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`
