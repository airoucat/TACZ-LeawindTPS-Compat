# docs/TACZ-LeawindTPS-Compat-NeoForge-1.21.1-Staged-Execution-Plan.md

> 当前执行分支：`codex/neoforge-1.21.1`  
> 目标发布分支：`neoforge-1.21.1`  
> 目标仓库基线：Minecraft `1.20.1` + Forge `47.x` 客户端 compat mod  
> 目标运行环境：Minecraft `1.21.1` + NeoForge，适配 `MUKSC/TACZ-1.21.1`  
> 执行 shell：Windows PowerShell，从仓库根目录执行  
> Gradle 命令：统一使用 `.\gradlew.bat`  
> 文档修订状态：`READY_FOR_EXECUTION`

---

## 1. 文档目的与适用范围

本文档是执行级开发计划。执行者必须逐阶段实现，不得在执行中自行决定版本、依赖、API target、Mixin 注入点、符号方向、是否删除文件、是否更新 README、是否同步 builder workflow。

适用范围：

- 保留 `modid = levanilla_tacztps`。
- 当前执行分支固定为 `codex/neoforge-1.21.1`。
- 目标发布分支固定为 `neoforge-1.21.1`。
- 不修改 TaCZ 源码。
- 不修改 Leawind 源码。
- 不把 TaCZ、Leawind、Architectury API 打进 compat jar。
- 只通过 compat 层、事件和 Mixin 完成迁移。
- 沿用 `compat bridge + recoil guard + mixin` 路线。
- 删除旧 `MixinCamera` 反射 recoil 路线。
- 删除旧 `RenderCrosshairEvent` lambda target 路线。
- 不使用 MixinExtras。
- 不重置 `.tacz-leawindtps-builder/feature_list.json`、`.tacz-leawindtps-builder/sprint_plan.json`、`.tacz-leawindtps-builder/progress.md`。
- 保留已有 WF0 / INV1 / VER1 / bootstrap 历史，并在现有 `units` / `activeSprint` schema 下追加 NeoForge 迁移工作单元。

阶段设计：

| 阶段 | 切片 | 目的 | 关键门禁 |
|---|---|---|---|
| Phase 0 | S00–S01 | 保留 memory layer 并追加迁移工作单元，冻结证据和 blocker | `units` / `activeSprint` 存在，分支正确，PowerShell 工具链存在 |
| Phase 1 | S02 | 迁移 Gradle 与本地依赖 | 本地 jar 存在，SHA-256 已记录，descriptor gate 通过，Gradle 配置门禁通过 |
| Phase 2 | S03 | 迁移 NeoForge metadata、主类、config | resources 处理通过，metadata 静态门禁通过 |
| Phase 3 | S04 | 实现 recoil bridge 与 guard | recoil 文件内容锁定，静态门禁通过 |
| Phase 4 | S05 | 实现 crosshair bridge | 删除旧 crosshair Mixin，新增 wrapper Mixin，静态门禁通过 |
| Phase 5 | S06 | 实现 aiming/scoping perspective | `TickAnimationEvent` overload descriptor 锁定，静态门禁通过 |
| Phase 6 | S07 | 实现 draw guard，删除旧反射 Mixin，执行首次完整 build | build 通过，Forge/MixinExtras/旧 Mixin 残留清零 |
| Phase 7 | S08–S09 | 文档与全量验证 close-out | runtime/gameplay 验证通过，所有 pass 都有 evidence |

S07 build 失败不得在 S07 临时修改不属于 S07 的文件，必须按错误路径进入 `S07R-*` 对应回修切片。S09 runtime/gameplay 验证失败不得在 S09 临时修改代码，必须按失败类型进入 `S09R-*` 对应回修切片。

---

## 2. 已冻结决策

| 决策项 | 冻结值 |
|---|---|
| 执行环境 | Windows PowerShell |
| Gradle wrapper 命令 | `.\gradlew.bat` |
| 当前执行分支 | `codex/neoforge-1.21.1` |
| 目标发布分支 | `neoforge-1.21.1` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.145` |
| Gradle Wrapper | `8.8-bin` |
| Gradle 插件 | `net.neoforged.moddev` `2.0.141` |
| Java toolchain | `21` |
| Parchment | `1.21.1:2024.11.17` |
| TaCZ | `MUKSC/TACZ-1.21.1`，`1.1.7-hotfix-r5` |
| TaCZ modId | `tacz` |
| Leawind | `2.3.0` |
| Leawind modId | `leawind_third_person` |
| Architectury API | `13.0.8` |
| compat modId | `levanilla_tacztps` |
| compat version | `3.0.0-mc1.21.1-neoforge` |
| license | `GPL-3.0-only` |
| build DSL | Groovy DSL |
| metadata file | `src/main/resources/META-INF/neoforge.mods.toml` |
| mixin config | `src/main/resources/levanilla_tacztps.mixins.json` |
| config file name | `tac-leawindtps.toml` |
| local TaCZ jar path | `libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar` |
| local Leawind jar path | `libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar` |
| local Architectury jar path | `libs/architectury-api-13.0.8-neoforge.jar` |
| recoil 参数顺序 | `LeawindBridge.turnCamera(dYRot, dXRot)` |
| crosshair target | `com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat#showCrosshair()Z` |
| aiming target | `TickAnimationEvent#tickAnimation(ClientTickEvent.Pre)` |
| draw target | `LocalPlayerDraw#draw(ItemStack)` |
| MixinExtras | 禁止使用 |
| memory layer schema | `feature_list.json` 使用 `units`，`sprint_plan.json` 使用 `activeSprint` |
| memory layer 操作 | 追加/合并 NeoForge 迁移单元，不覆盖历史 |

---

## 3. 证据锁定表

| 证据 ID | 来源 | 锁定事实 | 实装后果 |
|---|---|---|---|
| E-WIN-AGENTS | `AGENTS.win.md` | 当前仓库机器上下文使用 Windows PowerShell 与 `.\gradlew.bat`。 | 本文所有命令必须是 PowerShell 命令。 |
| E-WIN-TESTPATH | Microsoft PowerShell `Test-Path` 文档 | `Test-Path` 返回路径存在状态。 | 文件存在检查统一使用 `Test-Path`。 |
| E-WIN-REMOVE | Microsoft PowerShell `Remove-Item` 文档 | `Remove-Item -LiteralPath` 删除指定文件或目录。 | 删除操作统一使用 `Remove-Item -LiteralPath ... -Force -ErrorAction SilentlyContinue`。 |
| E-WIN-HASH | Microsoft PowerShell `Get-FileHash` 文档 | `Get-FileHash -Algorithm SHA256` 计算文件 SHA-256。 | 本地 jar 哈希记录统一使用 `Get-FileHash -Algorithm SHA256`。 |
| E-GRADLE-WRAPPER | Gradle Wrapper 官方文档 | Gradle Wrapper 包含 Windows batch 脚本 `gradlew.bat`。 | Gradle 命令统一使用 `.\gradlew.bat`。 |
| E-NF-MODFILE | `https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/` | `neoforge.mods.toml` 位于 `src/main/resources/META-INF/neoforge.mods.toml`；metadata 支持 `[[mixins]]` 与 `[[dependencies.<modid>]]`。 | 必须删除旧 `mods.toml` 并创建 `neoforge.mods.toml`。 |
| E-NF-CONFIG | `https://docs.neoforged.net/docs/1.21.1/misc/config` | `ModConfigSpec` 在 mod constructor 中通过 `ModContainer#registerConfig` 注册。 | 主类必须使用 NeoForge `ModContainer` 注册 client config。 |
| E-MDG | `https://docs.neoforged.net/toolchain/docs/plugins/mdg/` | ModDevGradle 支持 Gradle `8.8`；使用 `neoForge { version = ... runs { client { client() } } mods { ... } }`。 | Gradle wrapper 锁定 `8.8-bin`，build 使用 ModDevGradle。 |
| E-MDG-PLUGIN | `https://plugins.gradle.org/plugin/net.neoforged.moddev` | `net.neoforged.moddev` 锁定为 `2.0.141`。 | `build.gradle` 必须声明 `id 'net.neoforged.moddev' version '2.0.141'`。 |
| E-MDG-RUNTIME | `https://docs.neoforged.net/toolchain/docs/dependencies/nonmclibs/` | Minecraft `1.21.8` 及以下开发环境运行时必须使用 `additionalRuntimeClasspath` 加入外部依赖。 | 本地依赖 jar 必须使用 `compileOnly files(...)` 与 `additionalRuntimeClasspath files(...)`。 |
| E-TACZ-GRADLE | `https://raw.githubusercontent.com/MUKSC/TACZ-1.21.1/neoforge/1.21.1/gradle.properties` | TaCZ `mod_id=tacz`，`mod_version=1.1.7-hotfix-r5`，`archives_base_name=tacz-neoforge-1.21.1`。 | TaCZ 依赖 modId 与本地 jar 文件名锁定。 |
| E-TACZ-RECOIL | `https://raw.githubusercontent.com/MUKSC/TACZ-1.21.1/neoforge/1.21.1/src/main/java/com/tacz/guns/client/event/CameraSetupEvent.java` | `CameraSetupEvent#applyCameraRecoil(ViewportEvent.ComputeCameraAngles)` 存在，非 ShoulderSurfing 分支直接改 player rotation。 | Recoil Mixin 必须 target 该方法，第三人称下取消原生 player mutation。 |
| E-TACZ-CROSSHAIR | `https://raw.githubusercontent.com/MUKSC/TACZ-1.21.1/neoforge/1.21.1/src/main/java/com/tacz/guns/client/event/RenderCrosshairEvent.java` | TaCZ crosshair 流程调用 `ShoulderSurfingCompat.showCrosshair()`。 | 准星 Mixin 必须 hook `ShoulderSurfingCompat#showCrosshair()Z`。 |
| E-TACZ-SHOULDER | `https://raw.githubusercontent.com/MUKSC/TACZ-1.21.1/neoforge/1.21.1/src/main/java/com/tacz/guns/compat/shouldersurfing/ShoulderSurfingCompat.java` | `showCrosshair()` 是 public static boolean wrapper。 | 使用 `@Inject(method = "showCrosshair()Z", at = @At("HEAD"), cancellable = true)`。 |
| E-TACZ-TICK | `https://raw.githubusercontent.com/MUKSC/TACZ-1.21.1/neoforge/1.21.1/src/main/java/com/tacz/guns/client/event/TickAnimationEvent.java` | `TickAnimationEvent` 有 `tickAnimation(ClientTickEvent.Pre)` 与 `tickAnimation(RenderFrameEvent.Post)` 两个 overload。 | 必须写完整 descriptor，禁止裸方法名。 |
| E-TACZ-DRAW | `https://raw.githubusercontent.com/MUKSC/TACZ-1.21.1/neoforge/1.21.1/src/main/java/com/tacz/guns/client/gameplay/LocalPlayerDraw.java` | `LocalPlayerDraw` 目标方法为 `draw(ItemStack lastItem)`。 | Draw Mixin descriptor 锁定为 `draw(Lnet/minecraft/world/item/ItemStack;)V`。 |
| E-LW-GRADLE | `https://raw.githubusercontent.com/Leawind/Third-Person/1.21/gradle.properties` | Leawind 1.21 分支使用 Java 21、Minecraft 1.21.1、modId `leawind_third_person`、version `2.3.0`、Architectury API `13.0.8`、NeoForge `21.1.145`。 | 目标 Leawind / Architectury / NeoForge 版本锁定。 |
| E-LW-STATUS | `https://raw.githubusercontent.com/Leawind/Third-Person/1.21/common/src/main/java/com/github/leawind/thirdperson/ThirdPersonStatus.java` | Leawind 暴露 `isRenderingInThirdPerson()`、`shouldRenderThirdPersonCrosshair()`、`isPerspectiveInverted`。 | `LeawindBridge` 必须封装这些 API。 |
| E-LW-CAMERA | `https://raw.githubusercontent.com/Leawind/Third-Person/1.21/common/src/main/java/com/github/leawind/thirdperson/core/CameraAgent.java` | `CameraAgent#turnCamera(double dYRot, double dXRot)` 参数顺序锁定为 yaw delta 后 pitch delta。 | recoil 差值传入顺序固定为 `turnCamera(dYRot, dXRot)`。 |

本地 jar descriptor gate 必须锁定：

| 类 / 方法 | descriptor |
|---|---|
| `CameraSetupEvent#applyCameraRecoil` | `applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V` |
| `ShoulderSurfingCompat#showCrosshair` | `showCrosshair()Z` |
| `TickAnimationEvent#tickAnimation` | `tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V` |
| `LocalPlayerDraw#draw` | `draw(Lnet/minecraft/world/item/ItemStack;)V` |
| `CameraAgent#turnCamera` | `turnCamera(DD)V` |

Descriptor gate 必须成对验证 javap 方法声明行与紧邻 `descriptor:` 行；只搜索方法名不得作为通过条件。descriptor 不匹配必须触发 `B-API-001`，不得进入实现切片。

---

## 4. 未决阻塞项

| Blocker ID | 阻塞内容 | 解除条件 | 阻塞期间禁止事项 |
|---|---|---|---|
| B-ENV-001 | PowerShell 工具链缺失：`git`、`python3`、`rg`、`javap`、`jar`、`.\gradlew.bat` 任一不可用 | 安装或恢复工具后重新执行 S00 门禁 | 禁止进入 S02 |
| B-WF-001 | `.tacz-leawindtps-builder/` 三文件缺失或 JSON 无效 | 完成 S00 memory append | 禁止进入 S02 |
| B-WF-SCHEMA | `feature_list.json` 不含 `units`，或 `sprint_plan.json` 不含 `activeSprint` | 停止执行，先修订本文档的 memory layer 迁移规则 | 禁止追加新 work units |
| B-DEP-001 | 三个本地依赖 jar 未放置或 SHA-256 未记录 | 完成 S02 jar gate | 禁止写 Java 代码 |
| B-API-001 | `javap` descriptor 与本文档不一致 | 停止执行，修订证据锁定表与对应切片代码，再重新通过 descriptor gate | 禁止实现相关 Mixin |
| B-GRADLE-001 | S02 Gradle 配置门禁失败 | 执行 S02R-GRADLE，只修改 S02 allowlist | 禁止进入 S03 |
| B-STATIC-001 | S03–S06 任一静态门禁失败 | 回到当前切片，只修改当前切片 allowlist | 禁止进入下一切片 |
| B-BUILD-001 | S07 或 S09 build 失败 | 进入 S07R-DIAG，按错误路径执行对应 S07R-* remediation | 禁止写 `passes=true` |
| B-RUNTIME-001 | S09 `runClient` 无法进入主菜单或世界 | 进入 S09R-DIAG，按日志分类执行对应 S09R-* remediation | 禁止完成 S09 |
| B-MIXIN-001 | runtime 出现 Mixin 注入错误 | 进入 `S09R-DIAG`，由诊断器按日志分派到 `S09R-S03` / `S09R-S04` / `S09R-S05` / `S09R-S06` / `S09R-S07` | 禁止完成 S09 |
| B-GAMEPLAY-001 | recoil、crosshair、aim perspective、draw guard 任一 gameplay 判据失败 | 进入对应 S09R-* remediation | 禁止写相关 unit `passes=true` |

---

## 5. 切片总览

| 阶段 | 切片 | 类型 | 阶段目标 | 通过门禁 |
|---|---|---|---|---|
| Phase 0 | S00–S01 | workflow / 证据 | 保留并扩展 memory layer，冻结证据、阻塞项和消歧结果 | `.tacz-leawindtps-builder/` 三文件存在，`units` / `activeSprint` schema 通过 |
| Phase 1 | S02 | 工程迁移 | 重建 NeoForge / ModDevGradle / 本地依赖工程 | 三个 local jar 存在，SHA-256 已记录，descriptor gate 通过，`verifyLocalModJars` 通过 |
| Phase 2 | S03 | metadata / config | 替换 NeoForge metadata、Mixin metadata、主类、client config | `processResources` 通过，metadata 静态检查通过 |
| Phase 3 | S04 | recoil | 实现 Leawind bridge、recoil guard、TaCZ recoil 手动转接 | recoil Mixin target 与参数顺序静态检查通过 |
| Phase 4 | S05 | crosshair | 实现 TaCZ crosshair 对 Leawind 状态的 wrapper hook | 旧 lambda Mixin 删除，新 `ShoulderSurfingCompat` Mixin 静态检查通过 |
| Phase 5 | S06 | aiming/scoping | 实现配置驱动的瞄准/开镜第一人称过渡 | `TickAnimationEvent` overload descriptor 静态检查通过 |
| Phase 6 | S07 | draw guard / build gate | 实现 `LocalPlayerDraw` 防崩，删除旧反射 Mixin，执行首次完整 build | build 通过，Forge/MixinExtras/旧 Mixin 残留清零 |
| Phase 7 | S08–S09 | docs / validation | 更新 README，执行全量 gameplay 与 close-out 验证 | 所有 unit `passes=true` 且 evidence 完整 |

---

## 6. 执行规则

### 6.1 PowerShell 全局命令规则

每个命令块都必须在仓库根目录的 Windows PowerShell 中执行。

统一规则：

- Gradle：`.\gradlew.bat`
- 文件存在：`Test-Path -LiteralPath ...`
- 删除文件：`Remove-Item -LiteralPath ... -Force -ErrorAction SilentlyContinue`
- 创建目录：`New-Item -ItemType Directory -Path ... -Force | Out-Null`
- 创建空文件：`New-Item -ItemType File -Path ... -Force | Out-Null`
- 哈希：`Get-FileHash -Algorithm SHA256`
- 文本搜索：`rg`
- JSON 修改：PowerShell here-string 传入 `python3 -`
- 命令失败：`throw`，不得继续下一切片

禁止使用 Unix/Bash 专用写法、Unix 路径检查写法、Unix 删除写法、Unix 哈希写法、Unix 文本搜索写法、Unix Gradle wrapper 写法，以及 shell 短路控制流写法。检测到此类写法必须停止并修正文档命令块。

### 6.2 PowerShell helper prelude

每个新的 PowerShell 会话必须先执行以下 helper。该 helper 不写入仓库文件。

```powershell
$ErrorActionPreference = 'Stop'

function Assert-PathExists {
    param([Parameter(Mandatory=$true)][string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Required path missing: $Path"
    }
}

function Assert-PathAbsent {
    param([Parameter(Mandatory=$true)][string]$Path)
    if (Test-Path -LiteralPath $Path) {
        throw "Forbidden path exists: $Path"
    }
}

function Assert-RgFound {
    param(
        [Parameter(Mandatory=$true)][string]$Pattern,
        [Parameter(Mandatory=$true)][string[]]$Paths
    )
    & rg --fixed-strings --line-number -- $Pattern @Paths
    if ($LASTEXITCODE -ne 0) {
        throw "Required text not found: $Pattern"
    }
}

function Assert-RgAbsent {
    param(
        [Parameter(Mandatory=$true)][string]$Pattern,
        [Parameter(Mandatory=$true)][string[]]$Paths
    )
    & rg --line-number -- $Pattern @Paths
    if ($LASTEXITCODE -eq 0) {
        throw "Forbidden text found: $Pattern"
    }
    if ($LASTEXITCODE -gt 1) {
        throw "rg failed while checking: $Pattern"
    }
}

function Assert-ObsoleteMixinCameraRemoved {
    $obsoletePath = 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java'
    if (Test-Path -LiteralPath $obsoletePath) {
        throw "Forbidden obsolete MixinCamera file exists: $obsoletePath"
    }
    $mixinJsonPath = 'src/main/resources/levanilla_tacztps.mixins.json'
    Assert-PathExists $mixinJsonPath
    $mixinJson = Get-Content -LiteralPath $mixinJsonPath -Raw
    if ($mixinJson -match '"client\.MixinCamera"') {
        throw 'Forbidden exact mixin entry remains in levanilla_tacztps.mixins.json: "client.MixinCamera"'
    }
}

function Add-ProgressLine {
    param([Parameter(Mandatory=$true)][string]$Text)
    Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Value $Text -Encoding UTF8
}
```

### 6.3 Memory layer 状态记录

本仓库已有 `.tacz-leawindtps-builder/feature_list.json`，根字段为 `units`；已有 `.tacz-leawindtps-builder/sprint_plan.json`，根字段为 `activeSprint`。S00 只能追加 NeoForge 迁移数据，不得覆盖已有文件。

每个切片开始必须执行：

```powershell
$env:SLICE_ID = 'SXX'
Add-ProgressLine ""
Add-ProgressLine "- START $env:SLICE_ID $((Get-Date).ToString('o'))"
$script = @'
import json, os
from pathlib import Path
slice_id = os.environ['SLICE_ID']
p = Path('.tacz-leawindtps-builder/sprint_plan.json')
d = json.loads(p.read_text(encoding='utf-8'))
if 'activeSprint' not in d or not isinstance(d['activeSprint'], dict):
    raise SystemExit('B-WF-SCHEMA: sprint_plan.json missing activeSprint object')
mig = d['activeSprint'].setdefault('neoforge1211Migration', {})
slices = mig.setdefault('slices', [])
for s in slices:
    if s.get('id') == slice_id:
        s['status'] = 'in_progress'
        s['started'] = True
        s['blocker'] = None
        break
else:
    raise SystemExit(f'Slice not registered in activeSprint.neoforge1211Migration: {slice_id}')
p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
'@
$script | python3 -
```

每个切片阻塞必须执行：

```powershell
$env:SLICE_ID = 'SXX'
$env:BLOCKER_ID = 'B-XXXX'
Add-ProgressLine ""
Add-ProgressLine "- BLOCKED $env:SLICE_ID $env:BLOCKER_ID $((Get-Date).ToString('o'))"
$script = @'
import json, os
from pathlib import Path
slice_id = os.environ['SLICE_ID']
blocker_id = os.environ['BLOCKER_ID']
p = Path('.tacz-leawindtps-builder/sprint_plan.json')
d = json.loads(p.read_text(encoding='utf-8'))
mig = d['activeSprint']['neoforge1211Migration']
for s in mig['slices']:
    if s.get('id') == slice_id:
        s['status'] = 'blocked'
        s['blocker'] = blocker_id
        break
else:
    raise SystemExit(f'Slice not registered: {slice_id}')
p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
'@
$script | python3 -
```

每个切片完成必须执行：

```powershell
$env:SLICE_ID = 'SXX'
Add-ProgressLine ""
Add-ProgressLine "- DONE $env:SLICE_ID $((Get-Date).ToString('o'))"
$script = @'
import json, os
from pathlib import Path
slice_id = os.environ['SLICE_ID']
p = Path('.tacz-leawindtps-builder/sprint_plan.json')
d = json.loads(p.read_text(encoding='utf-8'))
mig = d['activeSprint']['neoforge1211Migration']
for s in mig['slices']:
    if s.get('id') == slice_id:
        s['status'] = 'done'
        s['completed'] = True
        s['blocker'] = None
        break
else:
    raise SystemExit(f'Slice not registered: {slice_id}')
p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
'@
$script | python3 -
```

实现完成但未验证时，更新对应 unit 的 `implemented=true`，`passes` 必须保持 `false`：

```powershell
$env:UNIT_ID = 'NF1211-BUILD'
$script = @'
import json, os
from pathlib import Path
unit_id = os.environ['UNIT_ID']
p = Path('.tacz-leawindtps-builder/feature_list.json')
d = json.loads(p.read_text(encoding='utf-8'))
if 'units' not in d or not isinstance(d['units'], list):
    raise SystemExit('B-WF-SCHEMA: feature_list.json missing units list')
for u in d['units']:
    if u.get('id') == unit_id:
        u['implemented'] = True
        u.setdefault('passes', False)
        break
else:
    raise SystemExit(f'Unit not registered: {unit_id}')
p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
'@
$script | python3 -
```

真实验证通过后，才允许写 `passes=true`：

```powershell
$env:UNIT_ID = 'NF1211-BUILD'
$env:EVIDENCE_TEXT = 'actual validation evidence here'
$script = @'
import json, os
from datetime import datetime, timezone
from pathlib import Path
unit_id = os.environ['UNIT_ID']
evidence = os.environ['EVIDENCE_TEXT']
p = Path('.tacz-leawindtps-builder/feature_list.json')
d = json.loads(p.read_text(encoding='utf-8'))
for u in d['units']:
    if u.get('id') == unit_id:
        u['passes'] = True
        u['validated_at'] = datetime.now(timezone.utc).isoformat()
        u.setdefault('evidence', []).append(evidence)
        break
else:
    raise SystemExit(f'Unit not registered: {unit_id}')
p.write_text(json.dumps(d, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
'@
$script | python3 -
```

### 6.4 代码切片 close-out

S02–S07 每个代码切片收尾必须执行：

```powershell
python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'graphify manual-closeout failed' }
```

### 6.5 失败回修规则

S07 build 失败必须执行 `S07R-DIAG`，按第一条匹配路径进入固定 remediation：

| 失败输出包含 | 回修切片 | 允许修改文件 |
|---|---|---|
| `build.gradle`、`settings.gradle`、`gradle.properties`、`gradle-wrapper.properties`、`verifyLocalModJars` | `S07R-S02` | S02 allowlist |
| `ThirdPersonTacz.java`、`ClientConfig.java`、`neoforge.mods.toml`、`levanilla_tacztps.mixins.json`、`processResources` | `S07R-S03` | S03 allowlist |
| `LeawindBridge.java`、`RecoilCallGuard.java`、`MixinCameraSetupEvent.java`、`ModEventSubscriber.java` | `S07R-S04` | S04 allowlist |
| `MixinShoulderSurfingCompat.java`、`MixinRenderCrosshairEvent.java` | `S07R-S05` | S05 allowlist |
| `MixinTickAnimationEvent.java` | `S07R-S06` | S06 allowlist |
| `MixinLocalPlayerDraw.java`、`MixinCamera.java` | `S07R-S07` | S07 allowlist |

同一次 build 输出命中多条规则时，必须按表格顺序先执行最靠上的 remediation。完成 remediation 后必须重新执行 S07 的 build gate。

S09 runtime/gameplay 失败必须执行 `S09R-DIAG`，按第一条匹配项进入固定 remediation：

| 失败类型 | 回修切片 | 允许修改文件 | 返回点 |
|---|---|---|---|
| `ModLoadingException`、config 未生成、metadata 不加载 | `S09R-S03` | S03 allowlist | 从 V02 重新执行 |
| `CameraSetupEvent` Mixin 错误、recoil 行为失败 | `S09R-S04` | S04 allowlist | 从 V03 重新执行 |
| `ShoulderSurfingCompat` Mixin 错误、crosshair 行为失败 | `S09R-S05` | S05 allowlist | 从 V04 重新执行 |
| `TickAnimationEvent` Mixin 错误、aim/scoping 行为失败 | `S09R-S06` | S06 allowlist | 从 V05 重新执行 |
| `LocalPlayerDraw` Mixin 错误、`hitResult` NPE、draw guard 行为失败 | `S09R-S07` | S07 allowlist | 从 V06 重新执行 |
| README 检查失败 | `S09R-S08` | S08 allowlist | 从 V07 重新执行 |
| build 失败 | `S07R-DIAG` | 由 S07R-DIAG 决定 | 从 V01 重新执行 |

Remediation 必须只把 owning slice 的目标文件恢复为本文档 final content，再执行 owning slice 的静态门禁、`.\gradlew.bat clean build`、相关 runtime 验证、graphify close-out。Descriptor 与本文档不一致时，不进入 remediation，必须标记 `B-API-001`。

### 6.6 禁止事项

- 不得提交 `libs/*.jar`。
- 不得保留 `net.minecraftforge.*` import。
- 不得保留 `net.minecraftforge.gradle`。
- 不得保留 `org.spongepowered.mixin` Gradle plugin。
- 不得声明 `jarJar`。
- 不得声明 MixinExtras。
- 不得使用 `@ModifyExpressionValue`。
- 不得使用 `require = 0` 掩盖未命中。
- 不得在 S09 对应验证项通过前把任何 unit `passes` 写为 `true`。
- 不得覆盖 `.tacz-leawindtps-builder/progress.md`。
- 不得完全替换 `.tacz-leawindtps-builder/feature_list.json` 或 `.tacz-leawindtps-builder/sprint_plan.json`。
- 不得在共享文档写机器私有绝对路径。

---

## 7. 详细切片

# Phase 0：Workflow 与证据门禁

## S00：Workflow 初始化与分支门禁

### 切片 ID

`S00`

### 目标

保留现有 `.tacz-leawindtps-builder/` memory layer，确认分支、PowerShell 工具链、graphify 脚本，并在现有 `units` / `activeSprint` 下追加 NeoForge 1.21.1 迁移工作单元。

### 前置条件

- 当前工作区为 fork 后本地仓库。
- 当前执行分支必须为 `codex/neoforge-1.21.1`。
- 目标发布分支必须在 memory layer 中记录为 `neoforge-1.21.1`。
- `.tacz-leawindtps-builder/progress.md` 已存在。
- `.tacz-leawindtps-builder/feature_list.json` 已存在且根字段包含 `units`。
- `.tacz-leawindtps-builder/sprint_plan.json` 已存在且根字段包含 `activeSprint`。

### 输入证据

- E-WIN-AGENTS
- 用户强制约束：当前执行分支为 `codex/neoforge-1.21.1`，目标发布分支为 `neoforge-1.21.1`。
- 用户强制约束：切片状态写 progress。
- 用户强制约束：同步 feature list 与 sprint plan。
- 用户强制约束：保留已有 WF0 bootstrap 历史，不得静默重置。

### 允许修改文件

```text
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/
build.gradle
settings.gradle
gradle.properties
gradle/
README.md
LICENSE
```

### 文件动作

- 不得删除、清空或完全替换任何 builder 文件。
- 只能向 `progress.md` 追加记录。
- 只能向 `feature_list.json` 的 `units` 追加或合并 `NF1211-*` units。
- 只能向 `sprint_plan.json` 的 `activeSprint` 添加或更新 `neoforge1211Migration` 对象。
- 现有 WF0 / INV1 / VER1 相关内容必须原样保留。

### 具体编辑要求

`feature_list.json` 必须追加或合并以下 unit ID：

```text
NF1211-WORKFLOW
NF1211-BUILD
NF1211-METADATA
NF1211-RECOIL
NF1211-CROSSHAIR
NF1211-AIM-PERSPECTIVE
NF1211-DRAW-GUARD
NF1211-DOCS
```

`sprint_plan.json` 的 `activeSprint.neoforge1211Migration.slices` 必须包含 S00–S09 以及 remediation slices：

```text
S00 S01 S02 S03 S04 S05 S06 S07 S08 S09
S02R-GRADLE
S07R-DIAG S07R-S02 S07R-S03 S07R-S04 S07R-S05 S07R-S06 S07R-S07
S09R-DIAG S09R-S03 S09R-S04 S09R-S05 S09R-S06 S09R-S07 S09R-S08
```

### 必须执行的命令

```powershell
$ErrorActionPreference = 'Stop'

$branch = (git branch --show-current).Trim()
if ($branch -ne 'codex/neoforge-1.21.1') { throw "Wrong execution branch: $branch; expected codex/neoforge-1.21.1" }
Add-ProgressLine "- EXECUTION_BRANCH codex/neoforge-1.21.1 TARGET_RELEASE_BRANCH neoforge-1.21.1 $((Get-Date).ToString('o'))"

Assert-PathExists 'AGENTS.win.md'
Assert-PathExists '.\gradlew.bat'
Assert-PathExists 'scripts/dev/setup_graphify_local.py'
Assert-PathExists '.tacz-leawindtps-builder/progress.md'
Assert-PathExists '.tacz-leawindtps-builder/feature_list.json'
Assert-PathExists '.tacz-leawindtps-builder/sprint_plan.json'

foreach ($cmd in @('git','python3','rg','javap','jar')) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) { throw "Missing command: $cmd" }
}

python3 -m json.tool .tacz-leawindtps-builder/feature_list.json | Out-Null
python3 -m json.tool .tacz-leawindtps-builder/sprint_plan.json | Out-Null

$script = @'
import json
from pathlib import Path
fl = Path('.tacz-leawindtps-builder/feature_list.json')
sp = Path('.tacz-leawindtps-builder/sprint_plan.json')
feature = json.loads(fl.read_text(encoding='utf-8'))
sprint = json.loads(sp.read_text(encoding='utf-8'))
if 'units' not in feature or not isinstance(feature['units'], list):
    raise SystemExit('B-WF-SCHEMA: feature_list.json missing units list')
if 'activeSprint' not in sprint or not isinstance(sprint['activeSprint'], dict):
    raise SystemExit('B-WF-SCHEMA: sprint_plan.json missing activeSprint object')
units = [
    ('NF1211-WORKFLOW', 'NeoForge migration workflow preservation'),
    ('NF1211-BUILD', 'NeoForge 1.21.1 Gradle build'),
    ('NF1211-METADATA', 'NeoForge metadata and client config'),
    ('NF1211-RECOIL', 'TaCZ recoil redirected to Leawind camera'),
    ('NF1211-CROSSHAIR', 'TaCZ crosshair respects Leawind third-person crosshair state'),
    ('NF1211-AIM-PERSPECTIVE', 'Aiming and scoping invert Leawind perspective according to config'),
    ('NF1211-DRAW-GUARD', 'LocalPlayerDraw null hitResult guard'),
    ('NF1211-DOCS', 'README and release notes aligned with GPL and target versions'),
]
by_id = {u.get('id'): u for u in feature['units'] if isinstance(u, dict)}
for uid, title in units:
    if uid not in by_id:
        feature['units'].append({
            'id': uid,
            'title': title,
            'phase': 'NeoForge 1.21.1 migration',
            'status': 'pending',
            'implemented': False,
            'passes': False,
            'validated_at': None,
            'evidence': []
        })
    else:
        existing = by_id[uid]
        existing.setdefault('title', title)
        existing.setdefault('phase', 'NeoForge 1.21.1 migration')
        existing.setdefault('status', 'pending')
        existing.setdefault('implemented', False)
        existing.setdefault('passes', False)
        existing.setdefault('validated_at', None)
        existing.setdefault('evidence', [])
slice_defs = [
    ('S00', 'Workflow 初始化与分支门禁'),
    ('S01', '证据归档与 blocker 建立'),
    ('S02', 'Gradle / NeoForge / 本地依赖迁移'),
    ('S03', 'NeoForge metadata、主类、配置迁移'),
    ('S04', 'Leawind bridge、recoil guard、recoil pipeline'),
    ('S05', 'Crosshair pipeline'),
    ('S06', 'Aiming / scoping perspective pipeline'),
    ('S07', 'LocalPlayerDraw 防崩与首次 build gate'),
    ('S08', 'README 与发布元数据'),
    ('S09', '全量验证与 close-out'),
    ('S02R-GRADLE', 'S02 Gradle remediation'),
    ('S07R-DIAG', 'S07 build failure classifier'),
    ('S07R-S02', 'S07 remediation for S02 write set'),
    ('S07R-S03', 'S07 remediation for S03 write set'),
    ('S07R-S04', 'S07 remediation for S04 write set'),
    ('S07R-S05', 'S07 remediation for S05 write set'),
    ('S07R-S06', 'S07 remediation for S06 write set'),
    ('S07R-S07', 'S07 remediation for S07 write set'),
    ('S09R-DIAG', 'S09 runtime failure classifier'),
    ('S09R-S03', 'S09 remediation for metadata/config'),
    ('S09R-S04', 'S09 remediation for recoil'),
    ('S09R-S05', 'S09 remediation for crosshair'),
    ('S09R-S06', 'S09 remediation for aiming/scoping'),
    ('S09R-S07', 'S09 remediation for draw guard'),
    ('S09R-S08', 'S09 remediation for README')
]
mig = sprint['activeSprint'].setdefault('neoforge1211Migration', {})
mig['executionBranch'] = 'codex/neoforge-1.21.1'
mig['targetReleaseBranch'] = 'neoforge-1.21.1'
mig.setdefault('status', 'pending')
slices = mig.setdefault('slices', [])
by_slice = {s.get('id'): s for s in slices if isinstance(s, dict)}
for sid, name in slice_defs:
    if sid not in by_slice:
        slices.append({'id': sid, 'name': name, 'status': 'pending', 'started': False, 'completed': False, 'blocker': None})
    else:
        s = by_slice[sid]
        s.setdefault('name', name)
        s.setdefault('status', 'pending')
        s.setdefault('started', False)
        s.setdefault('completed', False)
        s.setdefault('blocker', None)
fl.write_text(json.dumps(feature, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
sp.write_text(json.dumps(sprint, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
'@
$script | python3 -

Add-ProgressLine ""
Add-ProgressLine "## NeoForge 1.21.1 Migration Append"
Add-ProgressLine "- Existing memory layer preserved. WF0 / INV1 / VER1 history remains untouched."
Add-ProgressLine "- Execution branch recorded as codex/neoforge-1.21.1."
Add-ProgressLine "- Target release branch recorded as neoforge-1.21.1."
Add-ProgressLine "- Added or merged NF1211 units under feature_list.json units."
Add-ProgressLine "- Added or merged activeSprint.neoforge1211Migration slices."
```

执行 S00 start/done 模板，将 `SLICE_ID` 设为 `S00`。

### 预期产物

```text
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 验证步骤

```powershell
python3 -m json.tool .tacz-leawindtps-builder/feature_list.json | Out-Null
python3 -m json.tool .tacz-leawindtps-builder/sprint_plan.json | Out-Null

$script = @'
import json
from pathlib import Path
feature = json.loads(Path('.tacz-leawindtps-builder/feature_list.json').read_text(encoding='utf-8'))
sprint = json.loads(Path('.tacz-leawindtps-builder/sprint_plan.json').read_text(encoding='utf-8'))
required_units = {'NF1211-WORKFLOW','NF1211-BUILD','NF1211-METADATA','NF1211-RECOIL','NF1211-CROSSHAIR','NF1211-AIM-PERSPECTIVE','NF1211-DRAW-GUARD','NF1211-DOCS'}
actual_units = {u.get('id') for u in feature['units'] if isinstance(u, dict)}
missing_units = sorted(required_units - actual_units)
if missing_units:
    raise SystemExit('Missing units: ' + ', '.join(missing_units))
if 'activeSprint' not in sprint or 'neoforge1211Migration' not in sprint['activeSprint']:
    raise SystemExit('Missing activeSprint.neoforge1211Migration')
mig = sprint['activeSprint']['neoforge1211Migration']
if mig.get('executionBranch') != 'codex/neoforge-1.21.1':
    raise SystemExit('Missing or wrong executionBranch: codex/neoforge-1.21.1')
if mig.get('targetReleaseBranch') != 'neoforge-1.21.1':
    raise SystemExit('Missing or wrong targetReleaseBranch: neoforge-1.21.1')
required_slices = {'S00','S01','S02','S03','S04','S05','S06','S07','S08','S09','S07R-DIAG','S09R-DIAG'}
actual_slices = {s.get('id') for s in mig['slices']}
missing_slices = sorted(required_slices - actual_slices)
if missing_slices:
    raise SystemExit('Missing slices: ' + ', '.join(missing_slices))
'@
$script | python3 -

Assert-RgFound 'NeoForge 1.21.1 Migration Append' @('.tacz-leawindtps-builder/progress.md')
```

Pass 判据：builder JSON 有效，`units` 与 `activeSprint.neoforge1211Migration` 存在，`executionBranch=codex/neoforge-1.21.1`，`targetReleaseBranch=neoforge-1.21.1`，progress 只有追加记录。  
Fail 判据：任一文件缺失、JSON 无效、schema 不匹配、分支错误、工具缺失、历史被覆盖。

### 失败停止条件

- 当前执行分支不是 `codex/neoforge-1.21.1`。
- `AGENTS.win.md` 不存在。
- `.\gradlew.bat` 不存在。
- `scripts/dev/setup_graphify_local.py` 不存在。
- 任一工具缺失。
- `feature_list.json` 不含 `units`。
- `sprint_plan.json` 不含 `activeSprint`。

### 完成后同步

- `NF1211-WORKFLOW.implemented=true`
- `NF1211-WORKFLOW.passes=false`
- `S00.status=done`

### 完成定义

S00 仅当 memory layer 保留、NF1211 units 追加、activeSprint migration slices 追加、progress 追加记录、执行分支和工具链门禁通过后完成。

---

## S01：证据归档与 blocker 建立

### 切片 ID

`S01`

### 目标

把证据锁定摘要与 blocker 规则追加到 progress，不修改代码。

### 前置条件

- S00 完成。

### 输入证据

- 本文档第 2、3、4 章。

### 允许修改文件

```text
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/
build.gradle
settings.gradle
gradle.properties
gradle/
README.md
LICENSE
```

### 文件动作

向 `.tacz-leawindtps-builder/progress.md` 追加证据锁定摘要。

### 具体编辑要求

追加内容：

```md
## Evidence Lock Summary - NeoForge 1.21.1

- Execution shell locked to Windows PowerShell.
- Gradle command locked to `.\gradlew.bat`.
- Memory layer update mode locked to append/merge, not replace.
- Existing WF0 / INV1 / VER1 history must remain untouched.
- NeoForge locked to 21.1.145.
- ModDevGradle locked to 2.0.141.
- Gradle wrapper locked to 8.8-bin.
- Java locked to 21.
- TaCZ locked to modId `tacz`, version `1.1.7-hotfix-r5`.
- Leawind locked to modId `leawind_third_person`, version `2.3.0`.
- Architectury API locked to `13.0.8`.
- MixinExtras removed from this compat mod.
- RenderCrosshair lambda target removed from plan.
- Recoil sign and argument order locked to `turnCamera(dYRot, dXRot)`.
- S07 and S09 failures must use documented remediation slices.
```

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S01'
# 执行 6.3 start 模板

Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value @'

## Evidence Lock Summary - NeoForge 1.21.1

- Execution shell locked to Windows PowerShell.
- Gradle command locked to `.\gradlew.bat`.
- Memory layer update mode locked to append/merge, not replace.
- Existing WF0 / INV1 / VER1 history must remain untouched.
- NeoForge locked to 21.1.145.
- ModDevGradle locked to 2.0.141.
- Gradle wrapper locked to 8.8-bin.
- Java locked to 21.
- TaCZ locked to modId `tacz`, version `1.1.7-hotfix-r5`.
- Leawind locked to modId `leawind_third_person`, version `2.3.0`.
- Architectury API locked to `13.0.8`.
- MixinExtras removed from this compat mod.
- RenderCrosshair lambda target removed from plan.
- Recoil sign and argument order locked to `turnCamera(dYRot, dXRot)`.
- S07 and S09 failures must use documented remediation slices.
'@

Assert-RgFound 'Execution shell locked to Windows PowerShell.' @('.tacz-leawindtps-builder/progress.md')
Assert-RgFound 'S07 and S09 failures must use documented remediation slices.' @('.tacz-leawindtps-builder/progress.md')

# 执行 6.3 done 模板
```

### 预期产物

```text
.tacz-leawindtps-builder/progress.md
```

### 验证步骤

Pass 判据：progress 中存在 evidence lock 摘要。  
Fail 判据：摘要缺失。

### 失败停止条件

- `.tacz-leawindtps-builder/progress.md` 不存在。
- `.tacz-leawindtps-builder/sprint_plan.json` JSON 无效。

### 完成后同步

- `S01.status=done`

### 完成定义

S01 仅当证据锁定摘要追加到 progress 后完成。

---

# Phase 1：NeoForge 工程迁移

## S02：Gradle / NeoForge / 本地依赖迁移

### 切片 ID

`S02`

### 目标

把工程文件迁移为 NeoForge 1.21.1 + ModDevGradle 2.0.141，并锁定本地依赖 jar。S02 执行 Gradle 配置门禁与 `verifyLocalModJars`，不执行 `clean build`。

### 前置条件

- S01 完成。
- 三个依赖 jar 已放入：

```text
libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar
libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar
libs/architectury-api-13.0.8-neoforge.jar
```

### 输入证据

- E-MDG
- E-MDG-PLUGIN
- E-MDG-RUNTIME
- E-TACZ-GRADLE
- E-LW-GRADLE
- E-GRADLE-WRAPPER

### 允许修改文件

```text
settings.gradle
gradle.properties
build.gradle
gradle/wrapper/gradle-wrapper.properties
.gitignore
libs/.gitkeep
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/main/java/
src/main/resources/
README.md
LICENSE
libs/*.jar
```

### 文件动作

- 完全替换 `settings.gradle`。
- 完全替换 `gradle.properties`。
- 完全替换 `build.gradle`。
- 完全替换 `gradle/wrapper/gradle-wrapper.properties`。
- 创建 `libs/.gitkeep`。
- 更新 `.gitignore`。

### 具体编辑要求

`settings.gradle` final content：

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = 'NeoForge'
            url = uri('https://maven.neoforged.net/releases')
        }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

rootProject.name = 'TACZ-LeawindTPS-Compat'
```

`gradle.properties` final content：

```properties
org.gradle.jvmargs=-Xmx4G
org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=false

minecraft_version=1.21.1
minecraft_version_range=[1.21.1,1.21.2)
neo_version=21.1.145
neo_version_range=[21.1.145,21.2)
loader_version_range=[1,)

parchment_minecraft_version=1.21.1
parchment_mappings_version=2024.11.17

mod_id=levanilla_tacztps
mod_name=TaCZ: Leawind TPS Compat
mod_license=GPL-3.0-only
mod_version=3.0.0-mc1.21.1-neoforge
mod_group_id=com.levanilla
mod_authors=levanilla, airoucat
mod_description=Client-side third-person shooting compatibility layer for MUKSC TaCZ 1.21.1 NeoForge and Leawind's Third Person.

tacz_jar=libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar
leawind_jar=libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar
architectury_jar=libs/architectury-api-13.0.8-neoforge.jar
```

`build.gradle` final content：

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.141'
    id 'idea'
}

version = mod_version
group = mod_group_id

base {
    archivesName = mod_id
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        name = 'NeoForge'
        url = uri('https://maven.neoforged.net/releases')
    }
}

def localModJars = [
        file(project.tacz_jar),
        file(project.leawind_jar),
        file(project.architectury_jar)
]

tasks.register('verifyLocalModJars') {
    group = 'verification'
    description = 'Verify required local mod jars for the NeoForge 1.21.1 compat workspace.'

    inputs.files(localModJars)

    doLast {
        localModJars.each { jarFile ->
            if (!jarFile.exists()) {
                throw new GradleException("Missing required local dependency jar: ${jarFile}")
            }
            if (!jarFile.name.endsWith('.jar')) {
                throw new GradleException("Required local dependency is not a jar: ${jarFile}")
            }
        }
    }
}

dependencies {
    compileOnly files(localModJars)
    additionalRuntimeClasspath files(localModJars)
}

neoForge {
    version = project.neo_version

    parchment {
        minecraftVersion = project.parchment_minecraft_version
        mappingsVersion = project.parchment_mappings_version
    }

    runs {
        client {
            client()
            gameDirectory = project.file('run/client')
            systemProperty 'forge.logging.console.level', 'debug'
            systemProperty 'mixin.debug.export', 'true'
            systemProperty 'mixin.debug.verbose', 'true'
        }

        configureEach {
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        "${mod_id}" {
            sourceSet sourceSets.main
        }
    }
}

tasks.named('compileJava').configure {
    dependsOn tasks.named('verifyLocalModJars')
}

tasks.named('processResources', ProcessResources).configure {
    def replaceProperties = [
            minecraft_version      : minecraft_version,
            minecraft_version_range: minecraft_version_range,
            neo_version            : neo_version,
            neo_version_range      : neo_version_range,
            loader_version_range   : loader_version_range,
            mod_id                 : mod_id,
            mod_name               : mod_name,
            mod_license            : mod_license,
            mod_version            : mod_version,
            mod_authors            : mod_authors,
            mod_description        : mod_description
    ]

    inputs.properties replaceProperties

    filesMatching('META-INF/neoforge.mods.toml') {
        expand replaceProperties
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release.set(21)
}

tasks.named('jar', Jar).configure {
    manifest {
        attributes([
                'Specification-Title'   : mod_name,
                'Specification-Vendor'  : mod_authors,
                'Specification-Version' : mod_version,
                'Implementation-Title'  : mod_name,
                'Implementation-Version': mod_version,
                'Implementation-Vendor' : mod_authors
        ])
    }
}

idea {
    module {
        downloadSources = true
        downloadJavadoc = true
    }
}

publishing {
    publications {
        register('mavenJava', MavenPublication) {
            from components.java
        }
    }
}
```

`gradle/wrapper/gradle-wrapper.properties` final content：

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.8-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

`.gitignore` 必须包含：

```gitignore
# Local mod dependencies
/libs/*.jar
!/libs/.gitkeep
```

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S02'
# 执行 6.3 start 模板

New-Item -ItemType Directory -Path 'libs' -Force | Out-Null
New-Item -ItemType File -Path 'libs/.gitkeep' -Force | Out-Null

Assert-PathExists 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar'
Assert-PathExists 'libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar'
Assert-PathExists 'libs/architectury-api-13.0.8-neoforge.jar'

Get-FileHash -Algorithm SHA256 -LiteralPath 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' | Format-List | Out-String | Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8
Get-FileHash -Algorithm SHA256 -LiteralPath 'libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar' | Format-List | Out-String | Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8
Get-FileHash -Algorithm SHA256 -LiteralPath 'libs/architectury-api-13.0.8-neoforge.jar' | Format-List | Out-String | Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8

function Assert-JavapMethodDescriptor {
    param(
        [Parameter(Mandatory=$true)][string]$Jar,
        [Parameter(Mandatory=$true)][string]$ClassName,
        [Parameter(Mandatory=$true)][string]$MethodRegex,
        [Parameter(Mandatory=$true)][string]$JvmDescriptor,
        [Parameter(Mandatory=$true)][string]$LockedMethodDescriptor,
        [Parameter(Mandatory=$true)][string]$GateName
    )

    $output = & javap -classpath $Jar -s -p $ClassName | Out-String
    if ($LASTEXITCODE -ne 0) {
        Add-ProgressLine "- BLOCKED S02 B-API-001 $((Get-Date).ToString('o')) javap failed: $GateName"
        throw "B-API-001: javap failed for $GateName"
    }

    $descriptor = [regex]::Escape($JvmDescriptor)
    $pattern = '(?ms)^\s*.*' + $MethodRegex + '\s*\r?\n\s*descriptor:\s*' + $descriptor + '\s*$'

    if (-not [regex]::IsMatch($output, $pattern)) {
        Add-ProgressLine "- BLOCKED S02 B-API-001 $((Get-Date).ToString('o')) descriptor mismatch: $GateName expected $LockedMethodDescriptor / descriptor: $JvmDescriptor"
        Add-ProgressLine "- JAVAP OUTPUT $GateName"
        Add-ProgressLine $output
        throw "B-API-001: descriptor gate failed for $GateName; expected $LockedMethodDescriptor with javap descriptor $JvmDescriptor"
    }

    Add-ProgressLine "- DESCRIPTOR PASS S02 $GateName $LockedMethodDescriptor"
}

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.client.event.CameraSetupEvent' `
    -MethodRegex 'applyCameraRecoil\(net\.neoforged\.neoforge\.client\.event\.ViewportEvent\$ComputeCameraAngles\);' `
    -JvmDescriptor '(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V' `
    -LockedMethodDescriptor 'applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V' `
    -GateName 'CameraSetupEvent.applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V'

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat' `
    -MethodRegex 'showCrosshair\(\);' `
    -JvmDescriptor '()Z' `
    -LockedMethodDescriptor 'showCrosshair()Z' `
    -GateName 'ShoulderSurfingCompat.showCrosshair()Z'

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.client.event.TickAnimationEvent' `
    -MethodRegex 'tickAnimation\(net\.neoforged\.neoforge\.client\.event\.ClientTickEvent\$Pre\);' `
    -JvmDescriptor '(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V' `
    -LockedMethodDescriptor 'tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V' `
    -GateName 'TickAnimationEvent.tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V'

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.client.gameplay.LocalPlayerDraw' `
    -MethodRegex 'draw\(net\.minecraft\.world\.item\.ItemStack\);' `
    -JvmDescriptor '(Lnet/minecraft/world/item/ItemStack;)V' `
    -LockedMethodDescriptor 'draw(Lnet/minecraft/world/item/ItemStack;)V' `
    -GateName 'LocalPlayerDraw.draw(Lnet/minecraft/world/item/ItemStack;)V'

Assert-JavapMethodDescriptor `
    -Jar 'libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar' `
    -ClassName 'com.github.leawind.thirdperson.core.CameraAgent' `
    -MethodRegex 'turnCamera\(double, double\);' `
    -JvmDescriptor '(DD)V' `
    -LockedMethodDescriptor 'turnCamera(DD)V' `
    -GateName 'CameraAgent.turnCamera(DD)V'

.\gradlew.bat --version
if ($LASTEXITCODE -ne 0) { throw 'Gradle wrapper version check failed' }

.\gradlew.bat verifyLocalModJars
if ($LASTEXITCODE -ne 0) { throw 'verifyLocalModJars failed' }

python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'graphify manual-closeout failed' }

$env:UNIT_ID = 'NF1211-BUILD'
# 执行 6.3 implemented=true 模板
# 执行 6.3 done 模板
```

### 预期产物

```text
settings.gradle
gradle.properties
build.gradle
gradle/wrapper/gradle-wrapper.properties
libs/.gitkeep
```

### 验证步骤

```powershell
$trackedJars = git ls-files -- 'libs/*.jar'
if ($trackedJars) { throw "Local dependency jars are tracked by Git: $trackedJars" }

.\gradlew.bat verifyLocalModJars
if ($LASTEXITCODE -ne 0) { throw 'verifyLocalModJars failed' }

Assert-RgFound 'net.neoforged.moddev' @('build.gradle')
Assert-RgFound 'additionalRuntimeClasspath files(localModJars)' @('build.gradle')
Assert-RgFound 'distributionUrl=https\://services.gradle.org/distributions/gradle-8.8-bin.zip' @('gradle/wrapper/gradle-wrapper.properties')
```

Pass 判据：三 jar 存在，SHA-256 已记录，descriptor gate 通过，jar 未被 Git tracking，`verifyLocalModJars` 通过。  
Fail 判据：任一 jar 缺失、descriptor 不匹配、jar 被 Git tracking、Gradle 配置门禁失败。

### 失败停止条件

- 依赖 jar 缺失：`B-DEP-001`。
- descriptor 不匹配：`B-API-001`。
- Gradle 配置门禁失败：`B-GRADLE-001`，进入 `S02R-GRADLE`。
- graphify 失败：`B-GRADLE-001`。

### 完成后同步

- `NF1211-BUILD.implemented=true`
- `NF1211-BUILD.passes=false`
- `S02.status=done`

### 完成定义

S02 仅当本地依赖、SHA-256、descriptor gate、Gradle 配置门禁、graphify 全部通过后完成。

---

## S02R-GRADLE：S02 Gradle remediation

### 切片 ID

`S02R-GRADLE`

### 目标

修复 S02 中 Gradle 配置门禁失败。只允许恢复 S02 final content 和本地 jar gate，不得修改 Java 或 resources。

### 前置条件

- S02 处于 blocked。
- blocker 为 `B-GRADLE-001`。

### 允许修改文件

同 S02 allowlist。

### 禁止修改文件

同 S02 denylist。

### 文件动作

- 将 `settings.gradle`、`gradle.properties`、`build.gradle`、`gradle/wrapper/gradle-wrapper.properties` 恢复为 S02 final content。
- 重新检查 `.gitignore` local jar block。
- 重新执行 S02 必须执行的命令。

### 完成定义

`S02R-GRADLE` 仅当 `.\gradlew.bat verifyLocalModJars` 与 graphify 通过后完成；随后返回 S02 完成步骤。

---

# Phase 2：Metadata / Entry Point / Config

## S03：NeoForge metadata、主类、配置迁移

### 切片 ID

`S03`

### 目标

替换 NeoForge metadata、Mixin metadata、mod entrypoint、client config。S03 执行 `processResources` 门禁，不执行 `clean build`。

### 前置条件

- S02 完成。

### 输入证据

- E-NF-MODFILE
- E-NF-CONFIG

### 允许修改文件

```text
src/main/resources/META-INF/mods.toml
src/main/resources/META-INF/neoforge.mods.toml
src/main/resources/levanilla_tacztps.mixins.json
src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java
src/main/java/com/levanilla/TacZtps/ClientConfig.java
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/
src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java
build.gradle
gradle.properties
README.md
LICENSE
```

### 文件动作

- 删除 `src/main/resources/META-INF/mods.toml`。
- 创建或完全替换 `src/main/resources/META-INF/neoforge.mods.toml`。
- 完全替换 `src/main/resources/levanilla_tacztps.mixins.json`。
- 完全替换 `src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java`。
- 完全替换 `src/main/java/com/levanilla/TacZtps/ClientConfig.java`。

### 具体编辑要求

`src/main/resources/META-INF/neoforge.mods.toml` final content：

```toml
modLoader="javafml"
loaderVersion="${loader_version_range}"
license="${mod_license}"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
authors="${mod_authors}"
description='''${mod_description}'''

[[mixins]]
config="${mod_id}.mixins.json"

[[dependencies.${mod_id}]]
modId="neoforge"
type="required"
versionRange="${neo_version_range}"
ordering="NONE"
side="CLIENT"

[[dependencies.${mod_id}]]
modId="minecraft"
type="required"
versionRange="${minecraft_version_range}"
ordering="NONE"
side="CLIENT"

[[dependencies.${mod_id}]]
modId="tacz"
type="required"
ordering="AFTER"
side="CLIENT"

[[dependencies.${mod_id}]]
modId="leawind_third_person"
type="required"
versionRange="[2.3.0,2.4.0)"
ordering="AFTER"
side="CLIENT"

[[dependencies.${mod_id}]]
modId="architectury"
type="required"
versionRange="[13.0.8,14.0.0)"
ordering="AFTER"
side="CLIENT"
```

`src/main/resources/levanilla_tacztps.mixins.json` final content：

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.levanilla.TacZtps.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [],
  "client": [
    "client.MixinCameraSetupEvent",
    "client.MixinShoulderSurfingCompat",
    "client.MixinTickAnimationEvent",
    "client.MixinLocalPlayerDraw"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

`src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java` final content：

```java
package com.levanilla.TacZtps;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(value = ThirdPersonTacz.MOD_ID, dist = Dist.CLIENT)
public final class ThirdPersonTacz {
    public static final String MOD_ID = "levanilla_tacztps";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ThirdPersonTacz(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "tac-leawindtps.toml");
    }
}
```

`src/main/java/com/levanilla/TacZtps/ClientConfig.java` final content：

```java
package com.levanilla.TacZtps;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue SWITCH_FIRST_PERSON_AIMING;
    public static final ModConfigSpec.BooleanValue SWITCH_FIRST_PERSON_SCOPING;

    static {
        BUILDER.push("perspective");

        SWITCH_FIRST_PERSON_AIMING = BUILDER
                .comment(
                        "When true, aiming any TaCZ gun forces Leawind's Third Person into first-person transition.",
                        "This option takes precedence over switch_first_person_scoping."
                )
                .define("switch_first_person_aiming", false);

        SWITCH_FIRST_PERSON_SCOPING = BUILDER
                .comment(
                        "When true, aiming a TaCZ gun with an external or built-in scope forces Leawind's Third Person into first-person transition."
                )
                .define("switch_first_person_scoping", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private ClientConfig() {
    }
}
```

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S03'
# 执行 6.3 start 模板

New-Item -ItemType Directory -Path 'src/main/resources/META-INF' -Force | Out-Null
Remove-Item -LiteralPath 'src/main/resources/META-INF/mods.toml' -Force -ErrorAction SilentlyContinue

Assert-PathExists 'src/main/resources/META-INF/neoforge.mods.toml'
Assert-PathExists 'src/main/resources/levanilla_tacztps.mixins.json'
Assert-PathAbsent 'src/main/resources/META-INF/mods.toml'

Assert-RgAbsent 'requiredMods' @('src/main/resources')
Assert-RgAbsent 'mixinextras' @('src/main/resources','build.gradle','gradle.properties')

.\gradlew.bat verifyLocalModJars processResources
if ($LASTEXITCODE -ne 0) { throw 'processResources gate failed' }

python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'graphify manual-closeout failed' }

$env:UNIT_ID = 'NF1211-METADATA'
# 执行 6.3 implemented=true 模板
# 执行 6.3 done 模板
```

### 验证步骤

```powershell
Assert-PathExists 'src/main/resources/META-INF/neoforge.mods.toml'
Assert-PathAbsent 'src/main/resources/META-INF/mods.toml'
Assert-RgFound 'container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "tac-leawindtps.toml");' @('src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java')
Assert-RgFound 'net.neoforged.neoforge.common.ModConfigSpec' @('src/main/java/com/levanilla/TacZtps/ClientConfig.java')
Assert-RgAbsent 'net.minecraftforge' @('src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java','src/main/java/com/levanilla/TacZtps/ClientConfig.java')
```

Pass 判据：metadata 文件存在；旧 `mods.toml` 被删除；`processResources` 通过；`requiredMods` 与 MixinExtras 无残留。  
Fail 判据：任一检查失败。

### 完成后同步

- `NF1211-METADATA.implemented=true`
- `NF1211-METADATA.passes=false`
- `S03.status=done`

### 完成定义

S03 仅当 metadata、entrypoint、config 文件替换完成，resources 门禁与静态检查通过后完成。

---

# Phase 3：Recoil pipeline

## S04：Leawind bridge、recoil guard、recoil pipeline

### 切片 ID

`S04`

### 目标

实现 Leawind bridge、recoil guard、TaCZ recoil 取消与手动转接 pipeline。S04 执行静态门禁，不执行 `clean build`。

### 前置条件

- S03 完成。
- descriptor gate 已确认 recoil 目标。

### 输入证据

- E-TACZ-RECOIL
- E-LW-STATUS
- E-LW-CAMERA

### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/compat/LeawindBridge.java
src/main/java/com/levanilla/TacZtps/compat/RecoilCallGuard.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java
src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java
src/main/resources/
build.gradle
gradle.properties
README.md
LICENSE
```

### 文件动作

- 创建 `src/main/java/com/levanilla/TacZtps/compat/`。
- 完全替换目标 Java 文件。

### 具体编辑要求

`src/main/java/com/levanilla/TacZtps/compat/LeawindBridge.java` final content：

```java
package com.levanilla.TacZtps.compat;

import com.github.leawind.thirdperson.ThirdPerson;
import com.github.leawind.thirdperson.ThirdPersonStatus;

public final class LeawindBridge {
    private LeawindBridge() {
    }

    public static boolean isThirdPersonRendering() {
        return ThirdPerson.isAvailable() && ThirdPersonStatus.isRenderingInThirdPerson();
    }

    public static boolean shouldRenderCrosshair() {
        return ThirdPersonStatus.shouldRenderThirdPersonCrosshair();
    }

    public static void turnCamera(float dYRot, float dXRot) {
        ThirdPerson.CAMERA_AGENT.turnCamera(dYRot, dXRot);
    }

    public static void invertPerspective() {
        ThirdPersonStatus.isPerspectiveInverted = true;
    }
}
```

`src/main/java/com/levanilla/TacZtps/compat/RecoilCallGuard.java` final content：

```java
package com.levanilla.TacZtps.compat;

public final class RecoilCallGuard {
    private static final ThreadLocal<Boolean> MANUAL_CALL = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private RecoilCallGuard() {
    }

    public static boolean isManualCall() {
        return MANUAL_CALL.get();
    }

    public static void runManual(Runnable runnable) {
        MANUAL_CALL.set(Boolean.TRUE);
        try {
            runnable.run();
        } finally {
            MANUAL_CALL.set(Boolean.FALSE);
        }
    }
}
```

`src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java` final content：

```java
package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.levanilla.TacZtps.compat.RecoilCallGuard;
import com.tacz.guns.client.event.CameraSetupEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CameraSetupEvent.class, remap = false)
public final class MixinCameraSetupEvent {
    @Inject(
            method = "applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void levanilla$cancelNativeRecoilForLeawind(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        if (!RecoilCallGuard.isManualCall() && LeawindBridge.isThirdPersonRendering()) {
            ci.cancel();
        }
    }
}
```

Mixin contract：

| 项 | 值 |
|---|---|
| owner class | `com.tacz.guns.client.event.CameraSetupEvent` |
| target method descriptor | `applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V` |
| injection point | `HEAD` |
| cancellable | `true` |
| remap | `false` |
| 选择原因 | TaCZ 原生 recoil 在该方法内直接修改 player rotation；HEAD cancel 阻止 event path 的直接 player mutation。 |
| 命中验证 | S09 runtime 中检查 `.mixin.out` 与日志。 |

`src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java` final content：

```java
package com.levanilla.TacZtps;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.levanilla.TacZtps.compat.RecoilCallGuard;
import com.tacz.guns.client.event.CameraSetupEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = ThirdPersonTacz.MOD_ID)
public final class ModEventSubscriber {
    private ModEventSubscriber() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCameraRotateThirdPerson(ViewportEvent.ComputeCameraAngles event) {
        if (!LeawindBridge.isThirdPersonRendering()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        float oldXRot = player.getXRot();
        float oldYRot = player.getYRot();

        RecoilCallGuard.runManual(() -> CameraSetupEvent.applyCameraRecoil(event));

        float dXRot = player.getXRot() - oldXRot;
        float dYRot = player.getYRot() - oldYRot;

        player.setXRot(oldXRot);
        player.setYRot(oldYRot);

        LeawindBridge.turnCamera(dYRot, dXRot);
    }
}
```

不变量：`LeawindBridge.turnCamera(dYRot, dXRot)` 的参数顺序不得修改。

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S04'
# 执行 6.3 start 模板

New-Item -ItemType Directory -Path 'src/main/java/com/levanilla/TacZtps/compat' -Force | Out-Null

Assert-RgFound 'LeawindBridge.turnCamera(dYRot, dXRot);' @('src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java')
Assert-RgFound 'method = "applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V"' @('src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java')
Assert-RgAbsent 'turnCamera(dXRot, dYRot)' @('src/main')
Assert-RgAbsent 'EventPriority.HIGH|EventPriority.NORMAL' @('src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java')

.\gradlew.bat verifyLocalModJars processResources
if ($LASTEXITCODE -ne 0) { throw 'S04 resources/static Gradle gate failed' }

python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'graphify manual-closeout failed' }

$env:UNIT_ID = 'NF1211-RECOIL'
# 执行 6.3 implemented=true 模板
# 执行 6.3 done 模板
```

### 完成后同步

- `NF1211-RECOIL.implemented=true`
- `NF1211-RECOIL.passes=false`
- `S04.status=done`

### 完成定义

S04 仅当目标文件内容锁定、静态检查通过、resources/static Gradle gate 通过、graphify 通过后完成。

---

# Phase 4：Crosshair pipeline

## S05：Crosshair pipeline

### 切片 ID

`S05`

### 目标

删除旧 `MixinRenderCrosshairEvent.java`，新增 `MixinShoulderSurfingCompat.java`。

### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java
src/main/resources/levanilla_tacztps.mixins.json
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/main/java/com/levanilla/TacZtps/compat/
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java
src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java
build.gradle
gradle.properties
README.md
LICENSE
```

### 文件动作

- 删除 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java`。
- 完全替换 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java`。

### 具体编辑要求

`src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java` final content：

```java
package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShoulderSurfingCompat.class, remap = false)
public final class MixinShoulderSurfingCompat {
    @Inject(
            method = "showCrosshair()Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void levanilla$forceCrosshairForLeawind(CallbackInfoReturnable<Boolean> cir) {
        if (LeawindBridge.shouldRenderCrosshair()) {
            cir.setReturnValue(true);
        }
    }
}
```

Mixin contract：

| 项 | 值 |
|---|---|
| owner class | `com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat` |
| target method descriptor | `showCrosshair()Z` |
| injection point | `HEAD` |
| cancellable | `true` |
| remap | `false` |
| 选择原因 | TaCZ crosshair 逻辑复用 ShoulderSurfing wrapper；hook wrapper 避免不可锁定的 lambda 名。 |
| 命中验证 | S09 runtime 中检查 `.mixin.out` 与日志。 |

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S05'
# 执行 6.3 start 模板

Remove-Item -LiteralPath 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java' -Force -ErrorAction SilentlyContinue

Assert-RgFound 'method = "showCrosshair()Z"' @('src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java')
Assert-RgAbsent 'MixinRenderCrosshairEvent' @('src/main')
Assert-RgAbsent 'lambda\$onRenderCrosshair' @('src/main')
Assert-RgAbsent 'com.llamalad7.mixinextras' @('src/main','build.gradle','gradle.properties')

.\gradlew.bat verifyLocalModJars processResources
if ($LASTEXITCODE -ne 0) { throw 'S05 resources/static Gradle gate failed' }

python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'graphify manual-closeout failed' }

$env:UNIT_ID = 'NF1211-CROSSHAIR'
# 执行 6.3 implemented=true 模板
# 执行 6.3 done 模板
```

### 完成后同步

- `NF1211-CROSSHAIR.implemented=true`
- `NF1211-CROSSHAIR.passes=false`
- `S05.status=done`

### 完成定义

S05 仅当旧 crosshair Mixin 删除、新 wrapper Mixin 内容锁定、静态检查通过、resources/static Gradle gate 通过、graphify 通过后完成。

---

# Phase 5：Aim / Scope perspective

## S06：Aiming / scoping perspective pipeline

### 切片 ID

`S06`

### 目标

替换 `MixinTickAnimationEvent.java`，按 config 控制 Leawind 第一人称过渡。

### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/main/java/com/levanilla/TacZtps/ClientConfig.java
src/main/java/com/levanilla/TacZtps/compat/
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java
src/main/resources/
build.gradle
gradle.properties
README.md
LICENSE
```

### 具体编辑要求

`src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java` final content：

```java
package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.ClientConfig;
import com.levanilla.TacZtps.compat.LeawindBridge;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.event.TickAnimationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TickAnimationEvent.class, remap = false)
public final class MixinTickAnimationEvent {
    @Inject(
            method = "tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V",
            at = @At("HEAD"),
            remap = false
    )
    private static void levanilla$switchFirstPersonWhenAiming(ClientTickEvent.Pre event, CallbackInfo ci) {
        if (!LeawindBridge.isThirdPersonRendering()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return;
        }

        IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
        if (!operator.isAim()) {
            return;
        }

        ResourceLocation scopeId = iGun.getAttachmentId(mainHandItem, AttachmentType.SCOPE);
        if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = iGun.getBuiltInAttachmentId(mainHandItem, AttachmentType.SCOPE);
        }

        boolean hasScope = !scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID);
        boolean shouldInvert = ClientConfig.SWITCH_FIRST_PERSON_AIMING.get()
                || (hasScope && ClientConfig.SWITCH_FIRST_PERSON_SCOPING.get());

        if (shouldInvert) {
            LeawindBridge.invertPerspective();
        }
    }
}
```

Mixin contract：

| 项 | 值 |
|---|---|
| owner class | `com.tacz.guns.client.event.TickAnimationEvent` |
| target method descriptor | `tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V` |
| injection point | `HEAD` |
| cancellable | `false` |
| remap | `false` |
| 选择原因 | `ClientTickEvent.Pre` overload 是 client tick 状态入口；不得 target `RenderFrameEvent.Post`。 |
| 命中验证 | S09 runtime 中检查 `.mixin.out` 与 ambiguous target 日志。 |

不变量：

- target method 必须写完整 descriptor。
- 必须先检查 `LeawindBridge.isThirdPersonRendering()`。
- 必须通过 `IClientPlayerGunOperator.fromLocalPlayer(player).isAim()` 判断瞄准状态。
- 必须先调用 `getAttachmentId`，为空再调用 `getBuiltInAttachmentId`。

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S06'
# 执行 6.3 start 模板

Assert-RgFound 'tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V' @('src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java')
Assert-RgFound 'getBuiltInAttachmentId' @('src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java')
Assert-RgFound 'LeawindBridge.invertPerspective();' @('src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java')
Assert-RgAbsent 'method = "tickAnimation"' @('src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java')

.\gradlew.bat verifyLocalModJars processResources
if ($LASTEXITCODE -ne 0) { throw 'S06 resources/static Gradle gate failed' }

python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'graphify manual-closeout failed' }

$env:UNIT_ID = 'NF1211-AIM-PERSPECTIVE'
# 执行 6.3 implemented=true 模板
# 执行 6.3 done 模板
```

### 完成后同步

- `NF1211-AIM-PERSPECTIVE.implemented=true`
- `NF1211-AIM-PERSPECTIVE.passes=false`
- `S06.status=done`

### 完成定义

S06 仅当 tick Mixin 内容锁定、静态检查通过、resources/static Gradle gate 通过、graphify 通过后完成。

---

# Phase 6：Draw guard 与旧代码清理

## S07：LocalPlayerDraw 防崩与首次 build gate

### 切片 ID

`S07`

### 目标

替换 `MixinLocalPlayerDraw.java`，删除 `MixinCamera.java`，完成首次 `.\gradlew.bat clean build`。

### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java
src/main/resources/levanilla_tacztps.mixins.json
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/main/java/com/levanilla/TacZtps/compat/
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java
src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java
build.gradle
gradle.properties
README.md
LICENSE
```

### 具体编辑要求

`src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java` final content：

```java
package com.levanilla.TacZtps.mixin.client;

import com.tacz.guns.client.gameplay.LocalPlayerDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LocalPlayerDraw.class, remap = false)
public final class MixinLocalPlayerDraw {
    @Inject(
            method = "draw(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void levanilla$preventNullHitResultCrash(ItemStack lastItem, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hitResult == null && mc.player != null) {
            mc.hitResult = BlockHitResult.miss(
                    mc.player.position(),
                    Direction.UP,
                    mc.player.blockPosition()
            );
        }
    }
}
```

Mixin contract：

| 项 | 值 |
|---|---|
| owner class | `com.tacz.guns.client.gameplay.LocalPlayerDraw` |
| target method descriptor | `draw(Lnet/minecraft/world/item/ItemStack;)V` |
| injection point | `HEAD` |
| cancellable | `false` |
| remap | `false` |
| 选择原因 | 在 TaCZ draw 逻辑读取 `Minecraft#hitResult` 前补齐 miss hitResult。 |
| 命中验证 | S09 runtime 中检查 `.mixin.out` 与 hitResult NPE 日志。 |

删除文件：

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java
```

删除原因：旧反射 recoil 路线不可锁定，且会与 S04 recoil pipeline 重复。

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S07'
# 执行 6.3 start 模板

Remove-Item -LiteralPath 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java' -Force -ErrorAction SilentlyContinue

Assert-RgFound 'method = "draw(Lnet/minecraft/world/item/ItemStack;)V"' @('src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java')
Assert-PathAbsent 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java'
$mixinsJson = Get-Content -LiteralPath 'src/main/resources/levanilla_tacztps.mixins.json' -Raw
if ($mixinsJson -match '"client\.MixinCamera"') { throw 'Forbidden exact mixin entry remains: client.MixinCamera' }
Assert-RgAbsent 'net.minecraftforge' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'com.llamalad7.mixinextras' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'requiredMods' @('src/main','src/main/resources')

.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) {
    Add-ProgressLine "- BUILD FAILED S07 $((Get-Date).ToString('o'))"
    throw 'S07 build failed; execute S07R-DIAG before editing additional files'
}

$jarPath = 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar'
Assert-PathExists $jarPath
jar tf $jarPath | rg --fixed-strings 'META-INF/neoforge.mods.toml'
if ($LASTEXITCODE -ne 0) { throw 'Jar metadata missing: neoforge.mods.toml' }
jar tf $jarPath | rg --fixed-strings 'levanilla_tacztps.mixins.json'
if ($LASTEXITCODE -ne 0) { throw 'Jar metadata missing: mixins json' }
jar tf $jarPath | rg 'tacz-neoforge|leawind-third-person|architectury'
if ($LASTEXITCODE -eq 0) { throw 'Dependency jar was bundled into compat jar' }
if ($LASTEXITCODE -gt 1) { throw 'jar dependency scan failed' }

python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'graphify manual-closeout failed' }

$env:UNIT_ID = 'NF1211-DRAW-GUARD'
# 执行 6.3 implemented=true 模板
# 执行 6.3 done 模板
```

### 完成后同步

- `NF1211-DRAW-GUARD.implemented=true`
- `NF1211-DRAW-GUARD.passes=false`
- `S07.status=done`

### 完成定义

S07 仅当 draw guard 替换、旧反射 Mixin 删除、首次 build gate 通过、graphify 通过后完成。

---

## S07R-DIAG：S07 build failure classifier

### 切片 ID

`S07R-DIAG`

### 目标

对 S07 build 失败进行路径归因，分派到固定 remediation 切片。

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S07R-DIAG'
# 执行 6.3 start 模板

.\gradlew.bat clean build *>&1 | Tee-Object -FilePath '.tacz-leawindtps-builder/s07-build-failure.log'

$log = Get-Content -LiteralPath '.tacz-leawindtps-builder/s07-build-failure.log' -Raw
$target = $null
if ($log -match 'build\\.gradle|settings\\.gradle|gradle\\.properties|gradle-wrapper\\.properties|verifyLocalModJars') { $target = 'S07R-S02' }
elseif ($log -match 'ThirdPersonTacz\\.java|ClientConfig\\.java|neoforge\\.mods\\.toml|levanilla_tacztps\\.mixins\\.json|processResources') { $target = 'S07R-S03' }
elseif ($log -match 'LeawindBridge\\.java|RecoilCallGuard\\.java|MixinCameraSetupEvent\\.java|ModEventSubscriber\\.java') { $target = 'S07R-S04' }
elseif ($log -match 'MixinShoulderSurfingCompat\\.java|MixinRenderCrosshairEvent\\.java') { $target = 'S07R-S05' }
elseif ($log -match 'MixinTickAnimationEvent\\.java') { $target = 'S07R-S06' }
elseif ($log -match 'MixinLocalPlayerDraw\\.java|MixinCamera\\.java') { $target = 'S07R-S07' }
else { throw 'S07R-DIAG could not classify build failure; mark B-API-001 and revise this plan' }

Add-ProgressLine "- S07R-DIAG TARGET $target $((Get-Date).ToString('o'))"
# 执行 6.3 done 模板
```

### 完成定义

S07R-DIAG 仅当 progress 写入明确 target 后完成。执行者必须进入该 target remediation。

---

## S07R-S02 / S07R-S03 / S07R-S04 / S07R-S05 / S07R-S06 / S07R-S07

### 目标

按 S07R-DIAG 指定的 owning slice 回修 build 失败。

### 允许修改文件

- `S07R-S02`：仅 S02 allowlist。
- `S07R-S03`：仅 S03 allowlist。
- `S07R-S04`：仅 S04 allowlist。
- `S07R-S05`：仅 S05 allowlist。
- `S07R-S06`：仅 S06 allowlist。
- `S07R-S07`：仅 S07 allowlist。

### 文件动作

- 将 owning slice 涉及的文件恢复为本文档对应 final content。
- 重新执行 owning slice 的静态门禁。
- 执行 `.\gradlew.bat clean build`。
- 执行 graphify close-out。
- 返回 S07 必须执行命令，从 build gate 开始重跑。

### 失败停止条件

- descriptor 与本文档不一致：标记 `B-API-001`。
- owning slice final content 恢复后仍 build 失败且错误路径仍指向同一文件：标记 `B-API-001`，停止执行。

---

# Phase 7：文档、全量验证与 close-out

## S08：README 与发布元数据

### 切片 ID

`S08`

### 目标

替换 README，使目标版本、非官方适配范围、本地 jar 约束、GPLv3 继承义务明确。

### 允许修改文件

```text
README.md
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/
build.gradle
settings.gradle
gradle.properties
gradle/
LICENSE
libs/
```

### 具体编辑要求

`README.md` final content：

````md
# TaCZ: Leawind TPS Compat - NeoForge 1.21.1

This branch ports the original Forge 1.20.1 compatibility mod to NeoForge 1.21.1.

## Target

- Minecraft: `1.21.1`
- NeoForge: `21.1.145`
- Java: `21`
- TaCZ: `MUKSC/TACZ-1.21.1`, `1.1.7-hotfix-r5`
- Leawind's Third Person: `2.3.0`, NeoForge
- Architectury API: `13.0.8`
- modid: `levanilla_tacztps`

## Scope

This project is a client-side compatibility layer.

It does not modify TaCZ source code.
It does not modify Leawind source code.
It does not bundle TaCZ, Leawind's Third Person, or Architectury API inside the compat jar.

## Features

- Redirects TaCZ camera recoil to Leawind's third-person camera.
- Prevents TaCZ recoil from directly mutating the player rotation while Leawind third-person rendering is active.
- Allows TaCZ crosshair rendering to respect Leawind's third-person crosshair state.
- Supports config-driven first-person transition while aiming or scoping.
- Guards TaCZ draw logic against a null `Minecraft#hitResult`.

## Required runtime mods

Install these separately:

- TaCZ `1.1.7-hotfix-r5` for NeoForge 1.21.1
- Leawind's Third Person `2.3.0` for NeoForge 1.21.1
- Architectury API `13.0.8` for NeoForge 1.21.1

## Development local jars

Local development requires these repo-relative files:

```text
libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar
libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar
libs/architectury-api-13.0.8-neoforge.jar
```

These jars are ignored by Git and must not be committed.

## Config

Config file:

```text
config/tac-leawindtps.toml
```

Options:

```toml
switch_first_person_aiming = false
switch_first_person_scoping = true
```

Behavior:

- `switch_first_person_aiming=true` forces Leawind first-person transition while aiming any TaCZ gun.
- `switch_first_person_scoping=true` forces Leawind first-person transition while aiming a TaCZ gun with an external or built-in scope.
- `switch_first_person_aiming` takes precedence over `switch_first_person_scoping`.

## License

This project is a fork of `leva-nilla/TACZ-LeawindTPS-Compat`.

The original project is licensed under the GNU General Public License v3.0.
This fork remains licensed under `GPL-3.0-only`.

When distributing a compiled jar, the corresponding source code for that jar must also be available.
Do not add extra restrictions such as prohibiting modification, redistribution, or commercial use.
````

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S08'
# 执行 6.3 start 模板

Assert-RgFound 'Minecraft: `1.21.1`' @('README.md')
Assert-RgFound 'NeoForge: `21.1.145`' @('README.md')
Assert-RgFound 'modid: `levanilla_tacztps`' @('README.md')
Assert-RgFound 'GPL-3.0-only' @('README.md')
Assert-RgFound 'These jars are ignored by Git and must not be committed.' @('README.md')

$script = @'
from pathlib import Path
text = Path('README.md').read_text(encoding='utf-8')
markers = ['/' + 'Users' + '/', '/' + 'home' + '/', 'C:' + '\\']
for marker in markers:
    if marker in text:
        raise SystemExit('Private absolute path marker found in README')
'@
$script | python3 -

$env:UNIT_ID = 'NF1211-DOCS'
# 执行 6.3 implemented=true 模板
# 执行 6.3 done 模板
```

### 完成后同步

- `NF1211-DOCS.implemented=true`
- `NF1211-DOCS.passes=false`
- `S08.status=done`

### 完成定义

S08 仅当 README 完全替换且验证通过后完成。

---

## S09：全量验证与 close-out

### 切片 ID

`S09`

### 目标

执行完整验证 runbook。所有 `passes=true` 必须在本切片中由真实验证触发。Runtime/gameplay 失败必须进入 `S09R-*` remediation。

### 允许修改文件

```text
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

### 禁止修改文件

```text
src/
build.gradle
settings.gradle
gradle.properties
gradle/
README.md
LICENSE
libs/
```

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S09'
# 执行 6.3 start 模板

$script = @'
import json
from pathlib import Path
sprint = json.loads(Path('.tacz-leawindtps-builder/sprint_plan.json').read_text(encoding='utf-8'))
slices = sprint['activeSprint']['neoforge1211Migration']['slices']
missing = [s['id'] for s in slices if s['id'] in {'S00','S01','S02','S03','S04','S05','S06','S07','S08'} and s.get('status') != 'done']
if missing:
    raise SystemExit('Previous slices not done: ' + ', '.join(missing))
'@
$script | python3 -
```

### 失败停止条件

- 任一验证项 fail。
- 任一日志出现 Mixin 错误。
- 任一 unit 未真实验证却被写为 `passes=true`。

### 完成定义

S09 仅当全量验证 runbook 全部 pass、unit passes 均有 evidence、progress 写入 `FINAL CLOSE-OUT PASS` 后完成。

---

## S09R-DIAG：S09 runtime/gameplay failure classifier

### 切片 ID

`S09R-DIAG`

### 目标

对 S09 runtime/gameplay 失败进行固定归因，并分派到 `S09R-*` remediation。

### 必须执行的命令

```powershell
$env:SLICE_ID = 'S09R-DIAG'
# 执行 6.3 start 模板

$logPath = 'run/client/logs/latest.log'
Assert-PathExists $logPath
$log = Get-Content -LiteralPath $logPath -Raw
$target = $null
if ($log -match 'ModLoadingException|neoforge\\.mods\\.toml|tac-leawindtps\\.toml') { $target = 'S09R-S03' }
elseif ($log -match 'CameraSetupEvent|MixinCameraSetupEvent') { $target = 'S09R-S04' }
elseif ($log -match 'ShoulderSurfingCompat|MixinShoulderSurfingCompat') { $target = 'S09R-S05' }
elseif ($log -match 'TickAnimationEvent|MixinTickAnimationEvent|ambiguous') { $target = 'S09R-S06' }
elseif ($log -match 'LocalPlayerDraw|MixinLocalPlayerDraw|hitResult|NullPointerException') { $target = 'S09R-S07' }
else { throw 'S09R-DIAG could not classify runtime failure; mark B-RUNTIME-001 and revise this plan' }

Add-ProgressLine "- S09R-DIAG TARGET $target $((Get-Date).ToString('o'))"
# 执行 6.3 done 模板
```

### 完成定义

S09R-DIAG 仅当 progress 写入明确 target 后完成。执行者必须进入该 target remediation。

---

## S09R-S03 / S09R-S04 / S09R-S05 / S09R-S06 / S09R-S07 / S09R-S08

### 目标

按 S09R-DIAG 或验证项 failure 指定的 owning slice 回修 runtime/gameplay failure。

### 允许修改文件

- `S09R-S03`：仅 S03 allowlist。
- `S09R-S04`：仅 S04 allowlist。
- `S09R-S05`：仅 S05 allowlist。
- `S09R-S06`：仅 S06 allowlist。
- `S09R-S07`：仅 S07 allowlist。
- `S09R-S08`：仅 S08 allowlist。

### 文件动作

- 将 owning slice 涉及的文件恢复为本文档对应 final content。
- 重新执行 owning slice 的静态门禁。
- 执行 `.\gradlew.bat clean build`。
- 执行 owning runtime 验证。
- 执行 graphify close-out。
- 返回 S09，从失败的验证项重新执行。

### 失败停止条件

- descriptor 与本文档不一致：标记 `B-API-001`。
- owning slice final content 恢复后仍 runtime fail 且日志仍指向同一 owner：标记 `B-RUNTIME-001`。

---

## 8. 验证 Runbook

### V01 Build

运行命令：

```powershell
.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) { throw 'V01 build failed; execute S07R-DIAG' }

$jarPath = 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar'
Assert-PathExists $jarPath
jar tf $jarPath | rg --fixed-strings 'META-INF/neoforge.mods.toml'
if ($LASTEXITCODE -ne 0) { throw 'Jar metadata missing: neoforge.mods.toml' }
jar tf $jarPath | rg --fixed-strings 'levanilla_tacztps.mixins.json'
if ($LASTEXITCODE -ne 0) { throw 'Jar metadata missing: mixins json' }
jar tf $jarPath | rg 'tacz-neoforge|leawind-third-person|architectury'
if ($LASTEXITCODE -eq 0) { throw 'Dependency jar bundled into compat jar' }
if ($LASTEXITCODE -gt 1) { throw 'jar scan failed' }
```

mod 组合：

```text
NeoForge 21.1.145
TaCZ local jar
Leawind local jar
Architectury local jar
compat source
```

Pass 判据：build 成功；compat jar 存在；metadata 存在；依赖 jar 未打入 compat jar。  
Fail 判据：任一检查失败。  
记录位置：`.tacz-leawindtps-builder/progress.md`。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-BUILD'
$env:EVIDENCE_TEXT = 'V01 build passed; jar metadata present; dependency jars not bundled'
# 执行 6.3 passes=true 模板
```

### V02 Metadata / Config

运行命令：

```powershell
.\gradlew.bat runClient
if ($LASTEXITCODE -ne 0) { throw 'runClient failed before metadata/config validation; execute S09R-DIAG' }
```

操作步骤：

1. 进入主菜单。
2. 进入单人世界。
3. 退出客户端。
4. 执行：

```powershell
Assert-PathExists 'run/client/config/tac-leawindtps.toml'
Assert-RgFound 'switch_first_person_aiming' @('run/client/config/tac-leawindtps.toml')
Assert-RgFound 'switch_first_person_scoping' @('run/client/config/tac-leawindtps.toml')
Assert-RgAbsent 'InvalidInjectionException|MixinApplyError|ClassNotFoundException|ModLoadingException' @('run/client/logs/latest.log')
```

Pass 判据：mod 加载，config 生成，日志无 mod loading 或 Mixin 错误。  
Fail 判据：任一检查失败。失败后必须进入 `S09R-DIAG`，不得在 V02 内直接修改文件。  
记录位置：`run/client/config/tac-leawindtps.toml`、`run/client/logs/latest.log`、progress。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-METADATA'
$env:EVIDENCE_TEXT = 'V02 metadata/config passed; config generated and no mod loading or Mixin errors'
# 执行 6.3 passes=true 模板
```

### V03 Recoil

运行命令：

```powershell
.\gradlew.bat runClient
if ($LASTEXITCODE -ne 0) { throw 'runClient failed before recoil validation; execute S09R-DIAG' }
```

mod 组合：

```text
NeoForge 21.1.145
TaCZ 1.1.7-hotfix-r5
Leawind 2.3.0
Architectury API 13.0.8
compat jar/classes
```

配置前提：Leawind 第三人称启用，测试世界有 TaCZ 枪械。

操作步骤：

1. 开启 Leawind 第三人称。
2. 手持 TaCZ 枪械。
3. 连续射击 10 发。
4. 停止射击，观察 recoil 回落。
5. 切回第一人称。
6. 射击 5 发。
7. 退出客户端。
8. 执行：

```powershell
$cameraMixin = Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -like '*CameraSetupEvent*' }
if (-not $cameraMixin) { throw 'CameraSetupEvent mixin export missing; execute S09R-S04' }
Assert-RgAbsent 'InvalidInjectionException|MixinApplyError|ClassNotFoundException' @('run/client/logs/latest.log')
```

Pass 判据：第三人称 recoil 作用于 Leawind camera；玩家实体朝向不被 recoil 直接扭动；第一人称 TaCZ recoil 正常；Mixin 命中。  
Fail 判据：任一行为不符合。失败后必须进入 `S09R-DIAG`，不得在 V03 内直接修改文件。  
记录位置：progress、`run/client/logs/latest.log`、`run/client/.mixin.out/`。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-RECOIL'
$env:EVIDENCE_TEXT = 'V03 recoil passed; third-person recoil redirected and first-person recoil intact'
# 执行 6.3 passes=true 模板
```

### V04 Crosshair

运行命令：

```powershell
.\gradlew.bat runClient
if ($LASTEXITCODE -ne 0) { throw 'runClient failed before crosshair validation; execute S09R-DIAG' }
```

配置前提：Leawind 第三人称准星显示开启，测试世界有 TaCZ 枪械。

操作步骤：

1. 开启 Leawind 第三人称。
2. 手持 TaCZ 枪械，不瞄准，观察准星。
3. 按住瞄准键，观察准星。
4. 执行换弹，观察准星隐藏逻辑。
5. 打开 TaCZ 改装界面，观察准星隐藏逻辑。
6. 退出客户端。
7. 执行：

```powershell
$crosshairMixin = Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -like '*ShoulderSurfingCompat*' }
if (-not $crosshairMixin) { throw 'ShoulderSurfingCompat mixin export missing; execute S09R-S05' }
Assert-RgAbsent 'InvalidInjectionException|MixinApplyError|ClassNotFoundException' @('run/client/logs/latest.log')
```

Pass 判据：Leawind 允许准星时，TaCZ 第三人称持枪/瞄准不隐藏准星；换弹和改装界面隐藏逻辑不被破坏；Mixin 命中。  
Fail 判据：任一行为不符合。失败后必须进入 `S09R-DIAG`，不得在 V04 内直接修改文件。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-CROSSHAIR'
$env:EVIDENCE_TEXT = 'V04 crosshair passed; Leawind third-person crosshair state respected without breaking reload/refit hiding'
# 执行 6.3 passes=true 模板
```

### V05 Aim Perspective

运行命令：

```powershell
.\gradlew.bat runClient
if ($LASTEXITCODE -ne 0) { throw 'runClient failed before aim perspective validation; execute S09R-DIAG' }
```

测试 1 config：

```toml
switch_first_person_aiming = false
switch_first_person_scoping = true
```

Pass 判据：无 scope 枪械瞄准不得切第一人称过渡；外置 scope 与内置 scope 瞄准必须切第一人称过渡。

测试 2 config：

```toml
switch_first_person_aiming = true
switch_first_person_scoping = true
```

Pass 判据：无 scope 枪械瞄准也必须切第一人称过渡。

测试 3 config：

```toml
switch_first_person_aiming = false
switch_first_person_scoping = false
```

Pass 判据：有 scope 枪械瞄准不得切第一人称过渡。

最终检查：

```powershell
$tickMixin = Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -like '*TickAnimationEvent*' }
if (-not $tickMixin) { throw 'TickAnimationEvent mixin export missing; execute S09R-S06' }
Assert-RgAbsent 'InvalidInjectionException|MixinApplyError|ambiguous|ClassNotFoundException' @('run/client/logs/latest.log')
```

Fail 判据：任一 config 行为不符合；松开瞄准后视角不恢复；日志出现 ambiguous target 或 Mixin 错误。失败后必须进入 `S09R-DIAG`，不得在 V05 内直接修改文件。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-AIM-PERSPECTIVE'
$env:EVIDENCE_TEXT = 'V05 aim perspective passed across no-scope, external-scope, built-in-scope, and disabled-config cases'
# 执行 6.3 passes=true 模板
```

### V06 Draw Guard

运行命令：

```powershell
.\gradlew.bat runClient
if ($LASTEXITCODE -ne 0) { throw 'runClient failed before draw guard validation; execute S09R-DIAG' }
```

操作步骤：

1. 进入单人世界。
2. 枪械与非枪物品快速切换 30 次。
3. 第一人称与 Leawind 第三人称切换 10 次。
4. 持枪、收枪、拔枪、换枪各执行 5 次。
5. 退出客户端。
6. 执行：

```powershell
$drawMixin = Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue | Where-Object { $_.Name -like '*LocalPlayerDraw*' }
if (-not $drawMixin) { throw 'LocalPlayerDraw mixin export missing; execute S09R-S07' }
Assert-RgAbsent 'NullPointerException|hitResult|InvalidInjectionException|MixinApplyError|ClassNotFoundException' @('run/client/logs/latest.log')
Assert-PathAbsent 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java'
$mixinsJson = Get-Content -LiteralPath 'src/main/resources/levanilla_tacztps.mixins.json' -Raw
if ($mixinsJson -match '"client\.MixinCamera"') { throw 'Forbidden exact mixin entry remains: client.MixinCamera' }
```

Pass 判据：无崩溃；无 hitResult NPE；`LocalPlayerDraw` Mixin 命中；`src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java` 不存在，且 mixin json 不包含精确条目 `"client.MixinCamera"`。
Fail 判据：任一操作崩溃、hitResult NPE、Mixin 未命中，或旧 `MixinCamera.java` 精确删除检查失败。失败后必须进入 `S09R-DIAG`，不得在 V06 内直接修改文件。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-DRAW-GUARD'
$env:EVIDENCE_TEXT = 'V06 draw guard passed; rapid item switching and perspective switching produced no hitResult NPE'
# 执行 6.3 passes=true 模板
```

### V07 Docs

运行命令：

```powershell
Assert-RgFound 'Minecraft: `1.21.1`' @('README.md')
Assert-RgFound 'NeoForge: `21.1.145`' @('README.md')
Assert-RgFound 'GPL-3.0-only' @('README.md')
Assert-RgFound 'These jars are ignored by Git and must not be committed.' @('README.md')

$script = @'
from pathlib import Path
text = Path('README.md').read_text(encoding='utf-8')
markers = ['/' + 'Users' + '/', '/' + 'home' + '/', 'C:' + '\\']
for marker in markers:
    if marker in text:
        raise SystemExit('Private absolute path marker found in README')
'@
$script | python3 -
```

Pass 判据：README 目标版本、license、本地 jar 规则完整，无机器私有绝对路径。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-DOCS'
$env:EVIDENCE_TEXT = 'V07 docs passed; README target versions, GPL notice, local jar rules, and path hygiene verified'
# 执行 6.3 passes=true 模板
```

### V08 Workflow

运行命令：

```powershell
python3 -m json.tool .tacz-leawindtps-builder/feature_list.json | Out-Null
python3 -m json.tool .tacz-leawindtps-builder/sprint_plan.json | Out-Null

$script = @'
import json
from pathlib import Path
feature = json.loads(Path('.tacz-leawindtps-builder/feature_list.json').read_text(encoding='utf-8'))
required = {'NF1211-WORKFLOW','NF1211-BUILD','NF1211-METADATA','NF1211-RECOIL','NF1211-CROSSHAIR','NF1211-AIM-PERSPECTIVE','NF1211-DRAW-GUARD','NF1211-DOCS'}
units = [u for u in feature['units'] if u.get('id') in required]
bad = []
for u in units:
    if u.get('passes') and (not u.get('validated_at') or not u.get('evidence')):
        bad.append(u.get('id'))
missing = required - {u.get('id') for u in units}
if missing:
    raise SystemExit('Missing NF1211 units: ' + ', '.join(sorted(missing)))
if bad:
    raise SystemExit('passes=true without evidence: ' + ', '.join(sorted(bad)))
'@
$script | python3 -
```

Pass 判据：builder JSON 有效；每个 `passes=true` 都有 evidence。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-WORKFLOW'
$env:EVIDENCE_TEXT = 'V08 workflow passed; memory layer preserved and NF1211 units validated'
# 执行 6.3 passes=true 模板
```

---

## 9. 仓库同步与 Close-out

### Close-out 必须执行

```powershell
.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) { throw 'Close-out build failed; execute S07R-DIAG' }

python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
if ($LASTEXITCODE -ne 0) { throw 'Close-out graphify failed' }

python3 -m json.tool .tacz-leawindtps-builder/feature_list.json | Out-Null
python3 -m json.tool .tacz-leawindtps-builder/sprint_plan.json | Out-Null

Assert-RgAbsent 'net.minecraftforge' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'com.llamalad7.mixinextras' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'requiredMods' @('src/main','src/main/resources')
Assert-PathAbsent 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java'
$mixinsJson = Get-Content -LiteralPath 'src/main/resources/levanilla_tacztps.mixins.json' -Raw
if ($mixinsJson -match '"client\.MixinCamera"') { throw 'Forbidden exact mixin entry remains: client.MixinCamera' }

$trackedJars = git ls-files -- 'libs/*.jar'
if ($trackedJars) { throw "Local dependency jars are tracked by Git: $trackedJars" }

$jarPath = 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar'
jar tf $jarPath | rg 'tacz-neoforge|leawind-third-person|architectury'
if ($LASTEXITCODE -eq 0) { throw 'Dependency jar bundled into compat jar' }
if ($LASTEXITCODE -gt 1) { throw 'jar scan failed' }

Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value @'

## Close-out Summary - NeoForge 1.21.1

- Gradle build passed.
- runClient validation passed.
- Recoil validation passed.
- Crosshair validation passed.
- Aim/scoping perspective validation passed.
- LocalPlayerDraw guard validation passed.
- README validation passed.
- Dependency jars not bundled.
- Local dependency jars not tracked by Git.
- Forge imports removed.
- MixinExtras removed.
- Obsolete `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java` removed and exact mixin entry `"client.MixinCamera"` absent.

- FINAL CLOSE-OUT PASS
'@

$env:SLICE_ID = 'S09'
# 执行 6.3 done 模板
```

### 失败停止条件

- 任一验证项 fail。
- 任一日志出现 Mixin 错误。
- 任一 unit 未真实验证却被写为 `passes=true`。

### 完成后同步

- 所有 NF1211 units `implemented=true`。
- 所有 NF1211 units `passes=true`。
- `S09.status=done`。

### 完成定义

S09 仅当全量验证 runbook 全部 pass、unit passes 均有 evidence、progress 写入 `FINAL CLOSE-OUT PASS` 后完成。

---

## 10. 最终完成标准

执行完成后，当前执行分支 `codex/neoforge-1.21.1` 必须保留全部验证证据；发布前必须将已验证改动合并或发布到目标发布分支 `neoforge-1.21.1`。该合并/发布动作不属于本文执行切片，不得在 S00–S09 期间要求直接在发布分支上开发。

当前执行分支 `codex/neoforge-1.21.1` 仅当以下全部条件满足时完成；完成后才能合并或发布到目标发布分支 `neoforge-1.21.1`：

- `.tacz-leawindtps-builder/sprint_plan.json` 保留 `activeSprint`，并包含 `activeSprint.neoforge1211Migration`。
- `activeSprint.neoforge1211Migration.executionBranch` 为 `codex/neoforge-1.21.1`。
- `activeSprint.neoforge1211Migration.targetReleaseBranch` 为 `neoforge-1.21.1`。
- `.tacz-leawindtps-builder/feature_list.json` 保留 `units`，并包含全部 `NF1211-*` units。
- 既有 WF0 / INV1 / VER1 / bootstrap 历史未被删除。
- `.tacz-leawindtps-builder/progress.md` 包含 `FINAL CLOSE-OUT PASS`。
- 全部 `NF1211-*` units 的 `implemented=true`。
- 全部 `NF1211-*` units 的 `passes=true`，且每个 `passes=true` 都有 `validated_at` 与 `evidence`。
- `.\gradlew.bat clean build` 输出 `BUILD SUCCESSFUL`。
- `.\gradlew.bat runClient` 能进入世界。
- TaCZ + Leawind + Architectury + compat 同时加载，无 Mixin 错误。
- Leawind 第三人称下 TaCZ recoil 作用于 Leawind camera。
- Leawind 第三人称下玩家实体 rotation 不被 TaCZ recoil 直接扭动。
- TaCZ 第一人称 recoil 不被破坏。
- TaCZ crosshair 尊重 Leawind 第三人称准星配置。
- 换弹与改装界面的 TaCZ 隐藏准星逻辑不被破坏。
- `switch_first_person_aiming` 与 `switch_first_person_scoping` 行为符合 V05。
- 快速切枪不会触发 `Minecraft#hitResult` NPE。
- `src/main/resources/META-INF/mods.toml` 不存在。
- `src/main/resources/META-INF/neoforge.mods.toml` 存在。
- `src/main/resources/levanilla_tacztps.mixins.json` 不包含精确条目 `"client.MixinCamera"`。
- `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java` 不存在。
- `src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java` 不存在。
- `rg --line-number -- "net.minecraftforge" src/main build.gradle settings.gradle gradle.properties` 无结果。
- `rg --line-number -- "com.llamalad7.mixinextras" src/main build.gradle settings.gradle gradle.properties` 无结果。
- `rg --line-number -- "requiredMods" src/main src/main/resources` 无结果。
- `jar tf build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar` 不包含 TaCZ、Leawind、Architectury jar。
- `git ls-files -- 'libs/*.jar'` 无输出。
- README 明确写明目标版本、非官方 scope、本地 jar 不提交、GPLv3 继承义务。
- 所有共享文档不包含机器私有绝对路径。
