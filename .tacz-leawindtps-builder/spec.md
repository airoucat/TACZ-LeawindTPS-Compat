# TACZ-LeawindTPS-Compat Builder Spec Snapshot

## Objective

在当前 `docs/current-truth/` 基线上，维护 `Timeless and Classics Zero` 与 `Leawind's Third Person` 的第三人称兼容行为，并把 planning、implementation、review、graphify close-out 和状态同步固定到同一条 repo-level workflow 上。

## Product Surfaces

- `WF0` Harness + CE + Graphify workflow bootstrap
- `INV1` Current implementation inventory and contract baseline
- `VER1` Verification entry normalization
- runtime compatibility surfaces:
  - bootstrap and config
  - camera / camera setup
  - crosshair / overlay
  - local player draw / animation

## Hard Constraints

- current truth 根目录固定为 `docs/current-truth/`
- memory layer 固定为 `.tacz-leawindtps-builder/`
- planning / implementation / review 默认采用 `Planner -> ce:plan`、`Generator -> ce:work`、`Evaluator -> ce:review`
- 共享文档只写 repo-relative 路径，不写机器私有绝对路径
- 只要本轮改动涉及代码文件，收尾前必须执行：
  - `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`
- Graphify 只负责上下文加速，不替代 `docs/current-truth/` 或 `.tacz-leawindtps-builder/`
- `passes` 只能在对应验证真实执行后改成 `true`

## Non-Goals

- 不把 `AI Soldier` 的产品结构、ABP 路线图或测试口径直接复制到本仓库
- 不在没有需求依据时把当前兼容层扩成新的通用相机系统
- 不把本机私有路径写进 shared docs

## Done Definition

- 当前切片的目标、边界和验证入口已经写回 `.tacz-leawindtps-builder/`
- 相关代码、测试或文档改动已落地
- 该切片声明的验证已执行并可复述
- 若涉及代码改动，已完成 graphify close-out
