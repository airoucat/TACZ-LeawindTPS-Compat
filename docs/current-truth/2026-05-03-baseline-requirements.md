# TACZ-LeawindTPS-Compat 基线需求快照

## Objective

在不破坏 `Forge 1.20.1` 现有运行面的前提下，维护 `Timeless and Classics Zero` 与 `Leawind's Third Person` 的第三人称射击兼容行为，并让后续 planning、implementation、review 都能通过 repo 内统一的 harness 入口推进。

## Product Surfaces

- 模组 bootstrap 与 config 注册
- client camera / camera setup 注入
- crosshair / overlay 兼容
- local player draw 与动画兼容
- build、run、test 与 graphify close-out 工作流

## Hard Constraints

- shared current truth 根目录固定为 `docs/current-truth/`
- repo-level memory layer 固定为 `.tacz-leawindtps-builder/`
- 默认执行协议固定为 `Planner -> ce:plan`、`Generator -> ce:work`、`Evaluator -> ce:review`
- 共享文档只写 repo-relative 路径，不写机器私有绝对路径
- 只要本轮改动涉及代码文件，收尾前必须执行：
  - `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`
- `graphify-out/` 只负责上下文加速，不替代 current truth 文档或 memory layer
- 在没有实际验证前，不把 `passes` 提前标成 `true`

## Known Runtime Assumptions

- 当前仓库的主代码位于 `com.levanilla.TacZtps`
- 当前 mod id 是 `levanilla_tacztps`
- 当前兼容实现以 client mixin 为主，不默认假设存在完整 server-side runtime
- 当前仓库还没有系统化测试矩阵文档；如果后续补齐，应把验证入口和期望结果写回 `.tacz-leawindtps-builder/`

## Non-Goals

- 不把 `AI Soldier` 的 ABP 结构、产品目标或测试口径直接复制到本仓库
- 不在没有需求依据时把兼容层扩成新的通用相机框架
- 不通过 shared 文档记录本机私有测试实例路径
- 不把 graphify 报告当成 authoritative contract

## Done Definition

- 当前任务的目标、边界、验证入口已记录到 `.tacz-leawindtps-builder/`
- 相关代码、测试或文档改动已经落地
- 该任务声明的验证已实际执行并可复述
- 若涉及代码改动，已执行 graphify close-out
