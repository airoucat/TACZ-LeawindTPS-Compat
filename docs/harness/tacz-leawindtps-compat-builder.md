# TACZ-LeawindTPS-Compat Builder Harness

这是把 `harness + ce + graphify` 模板落到 `TACZ-LeawindTPS-Compat` 仓库后的默认工作流说明。

这里的目标不是引入一组松散 skill，而是把 repo 级执行协议固定下来：

- `harness` 负责项目状态、完成定义、进度同步
- `ce` 负责默认的规划、实现、审查执行
- `graphify` 负责代码图和结构化上下文加速

## Related Docs

- `docs/harness/another-machine-setup.md`
  - 在另一台机器上把当前仓库默认 workflow 跑起来时先读这里
- `docs/current-truth/README.md`
  - 当前模组的 shared current truth 与 source map

## Concept Mapping

| 模板概念 | 本仓库落地 |
| --- | --- |
| `planner` | 默认由 `ce:plan` 执行；确认当前兼容问题、边界和验证入口，并更新 `.tacz-leawindtps-builder/` |
| `generator` | 默认由 `ce:work` 执行；只实现当前切片范围内的代码、测试和必要文档 |
| `evaluator` | 默认由 `ce:review` 执行；验证行为是否诚实、是否引入回归或兼容漂移 |
| `sync` | 运行 graphify close-out，并把状态写回 `.tacz-leawindtps-builder/feature_list.json`、`sprint_plan.json`、`progress.md` |
| `memory layer` | `.tacz-leawindtps-builder/`，是 repo 内当前实施状态的唯一记忆层 |

## Memory Layer

根目录下的 `.tacz-leawindtps-builder/` 是默认的项目实施记忆层：

- `spec.md`
  - 项目目标、硬约束、非目标、完成定义
- `feature_list.json`
  - 顶层工作单元、状态与 `passes`
- `sprint_plan.json`
  - 当前 sprint / bootstrap window、依赖、退出标准、验证方式
- `progress.md`
  - 时间线、阻塞、验证结果、重要决策

## Default Execution Loop

1. `Planner`
   - 先读 `docs/current-truth/README.md`
   - 再读 `.tacz-leawindtps-builder/spec.md`、`feature_list.json`、`sprint_plan.json`、`progress.md`
   - 确认当前要推进的是哪个兼容切片、哪个验证入口
   - 若范围或资料仍不清楚，先回到 `ce:brainstorm`
   - 默认由 `ce:plan` 输出当前切片的目标、边界、退出标准
2. `Generator`
   - 只在当前切片范围内写代码、测试、脚本和必要文档
   - 默认由 `ce:work` 执行
   - 不把无关的兼容想法或大型重构偷渡进当前切片
3. `Evaluator`
   - 对照当前切片声明的验证入口做审查
   - 默认由 `ce:review` 执行
   - 优先找兼容回归、mixin 注入漂移、配置行为偏差和验证不诚实
4. `Sync`
   - 若本轮涉及代码文件，先执行：
     - `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`
   - 然后同步更新 `.tacz-leawindtps-builder/feature_list.json`
   - 同步更新 `.tacz-leawindtps-builder/sprint_plan.json`
   - 最后向 `.tacz-leawindtps-builder/progress.md` 追加记录

## Repo-Specific Rules

- current truth 根目录始终是 `docs/current-truth/`
- 当前代码边界以 `com.levanilla.TacZtps` 和 `mixin/client/` 为主
- 共享文档一律使用 repo-relative 路径，不写机器私有绝对路径
- 文档更新默认保留历史轨迹；后续如果某条约束失效，应补 update note，而不是静默改写历史
- 如果任务涉及运行时兼容行为，优先写出最小充分验证，不要只凭代码阅读宣称完成

## Validation Expectations

当前仓库的验证入口以最小充分证明为主，常见包括：

- `.\gradlew.bat test`
- `.\gradlew.bat build`
- `.\gradlew.bat runClient`
- 针对当前变更的手工游戏内验证

`passes` 只能在对应验证实际执行并通过后改成 `true`。

## Definition Of Done

一个切片只有同时满足下面条件才算完成：

- 当前范围内的代码、测试、文档已经落地
- 已执行该切片声明的验证，并且结果可复述
- 如果本轮改动涉及代码文件，已执行 `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`
- `.tacz-leawindtps-builder/feature_list.json` 与 `.tacz-leawindtps-builder/sprint_plan.json` 状态一致
- `.tacz-leawindtps-builder/progress.md` 记录了开始、结论和风险
