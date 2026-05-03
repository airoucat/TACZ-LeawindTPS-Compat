# TACZ-LeawindTPS-Compat 协作入口

本文件是任何新对话进入本仓库时的默认入口。

如果当前机器是 macOS，读完本文件后继续阅读 `AGENTS.mac.md`。`AGENTS.mac.md` 保存机器相关的本地环境、测试实例和参考路径。

如果当前机器是 Windows，读完本文件后继续阅读 `AGENTS.win.md`。`AGENTS.win.md` 保存这台 Windows 机器上的本地环境、JDK、验证命令和测试实例信息。

## 当前仓库状态

- 当前仓库是 `Minecraft 1.20.1 + Forge 47.x` 的客户端兼容模组。
- 当前模组的目标是给 `Timeless and Classics Zero` 与 `Leawind's Third Person` 提供第三人称射击兼容层。
- 当前代码主路径集中在：
  - `src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java`
  - `src/main/java/com/levanilla/TacZtps/ClientConfig.java`
  - `src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java`
  - `src/main/java/com/levanilla/TacZtps/mixin/client/`
- 当前 shared current truth 根目录固定在：
  - `docs/current-truth/`

## 新对话的默认阅读顺序

任何新任务，默认先读：

1. `AGENTS.md`
2. 若当前机器是 macOS，再读 `AGENTS.mac.md`
3. 若当前机器是 Windows，再读 `AGENTS.win.md`
4. `docs/current-truth/README.md`
5. `docs/current-truth/2026-05-03-baseline-requirements.md`
6. `docs/harness/tacz-leawindtps-compat-builder.md`
7. `.tacz-leawindtps-builder/spec.md`
8. `.tacz-leawindtps-builder/feature_list.json`
9. `.tacz-leawindtps-builder/sprint_plan.json`
10. `.tacz-leawindtps-builder/progress.md`

## 默认工作流

- 本仓库默认采用 `harness + ce + graphify` 作为 repo-level workflow。
- repo 内的 harness 记忆层固定在：
  - `.tacz-leawindtps-builder/`
- 三段式默认映射固定为：
  - `Planner -> ce:plan`
  - `Generator -> ce:work`
  - `Evaluator -> ce:review`
- 若范围、命名或兼容边界还不清楚，可以先用 `ce:brainstorm` 补需求收口，但它不替代默认主链。
- 每次开始、完成、返工或阻塞一个工作切片时，都向 `.tacz-leawindtps-builder/progress.md` 追加记录。
- 只要本轮改动涉及代码文件，在宣告完成前必须执行：
  - `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`

## Graphify 本地自动化

- `graphify-out/` 是本地生成物，不提交到 Git。
- 仓库内版本化 hooks 位于 `.githooks/`，用于在本机的 `commit / checkout / merge / rebase` 后自动重建代码图。
- 每台新机器 clone 或首次进入仓库后，只需要执行一次：
  - `python3 scripts/dev/setup_graphify_local.py`
- 这一步会：
  - 自动安装或复用本机 `graphifyy`
  - 配置本地 `git config core.hooksPath .githooks`
  - 补齐本机 `.codex/hooks.json`
  - 在本地尚无 `graphify-out/graph.json` 时自动生成首轮代码图
- 手动重建入口统一使用：
  - `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`

## 工作规则

- 新文档一律使用 repo-relative 路径。
- 机器相关信息只写到 `AGENTS.win.md` 或 `AGENTS.mac.md`，不要写进 shared current truth 或 harness 文档。
- `graphify-out/` 只作为上下文加速，不替代 `docs/current-truth/` 或 `.tacz-leawindtps-builder/`。
- 如果当前任务涉及相机、准星、人物渲染或动画注入边界，先读 `docs/current-truth/README.md` 里的 source map，再决定改哪个 mixin。
- 在没有实际验证前，不要把 `.tacz-leawindtps-builder/feature_list.json` 里的 `passes` 改成 `true`。

## 快速路由

- “先看什么？”
  先读本文件，再读当前机器对应的 `AGENTS.win.md` 或 `AGENTS.mac.md`，然后读 `docs/current-truth/README.md`、`docs/harness/tacz-leawindtps-compat-builder.md` 和 `.tacz-leawindtps-builder/`。

- “我要理解当前模组边界”
  先读 `docs/current-truth/README.md`、`docs/current-truth/2026-05-03-baseline-requirements.md`，再对照 `src/main/java/com/levanilla/TacZtps/`。

- “我要继续默认工作流”
  先读 `docs/harness/tacz-leawindtps-compat-builder.md`、`.tacz-leawindtps-builder/spec.md`、`feature_list.json`、`sprint_plan.json`、`progress.md`，然后按 `Planner -> ce:plan`、`Generator -> ce:work`、`Evaluator -> ce:review` 推进当前切片。

- “我要在另一台机器上把当前 workflow 跑起来”
  先读 `docs/harness/another-machine-setup.md`，然后执行 `python3 scripts/dev/setup_graphify_local.py`。

- “我要重建 graphify”
  在仓库根目录执行 `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`。

## graphify

This project has a graphify knowledge graph at `graphify-out/`.

Rules:
- Before answering architecture or codebase questions, read `graphify-out/GRAPH_REPORT.md` for god nodes and community structure.
- If `graphify-out/wiki/index.md` exists, navigate it instead of reading raw files.
- Treat graphify as a context accelerator, not the source of truth over `docs/current-truth/` or `.tacz-leawindtps-builder/`.
- After modifying code files in this session, run `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`.
