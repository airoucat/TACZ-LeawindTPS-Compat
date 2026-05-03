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
- 执行阶段必须在 `codex/neoforge-1.21.1` 上跑通。
- 发布前必须把已验证改动合并或发布到 `neoforge-1.21.1`。
- 不修改 TaCZ 源码。
- 不修改 Leawind 源码。
- 不把 TaCZ、Leawind、Architectury API 打进 compat jar。
- 只通过 compat 层、事件和 Mixin 完成迁移。
- 沿用 `compat bridge + recoil guard + mixin` 路线。
- 删除旧反射 recoil 路线，即删除 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java`。
- 删除旧 `RenderCrosshairEvent` lambda target 路线。
- 不使用 MixinExtras。
- 不重置 `.tacz-leawindtps-builder/feature_list.json`、`.tacz-leawindtps-builder/sprint_plan.json`、`.tacz-leawindtps-builder/progress.md`。
- 保留已有 WF0 / INV1 / VER1 / bootstrap 历史，并在现有 `units` / `activeSprint` schema 下追加 NeoForge 迁移工作单元。

阶段设计：

| 阶段 | 切片 | 目的 | 关键门禁 |
|---|---|---|---|
| Phase 0 | S00–S01 | 保留 memory layer 并追加迁移工作单元，冻结证据和 blocker | `units` / `activeSprint` 存在，当前执行分支正确，PowerShell 工具链存在 |
| Phase 1 | S02 | 迁移 Gradle 与本地依赖 | 本地 jar 存在，SHA-256 已记录，完整 descriptor gate 通过，Gradle 配置门禁通过 |
| Phase 2 | S03 | 迁移 NeoForge metadata、主类、config | resources 处理通过，metadata 静态门禁通过 |
| Phase 3 | S04 | 实现 recoil bridge 与 guard | recoil 文件内容锁定，静态门禁通过 |
| Phase 4 | S05 | 实现 crosshair bridge | 删除旧 crosshair Mixin，新增 wrapper Mixin，静态门禁通过 |
| Phase 5 | S06 | 实现 aiming/scoping perspective | `TickAnimationEvent` overload descriptor 锁定，静态门禁通过 |
| Phase 6 | S07 | 实现 draw guard，删除旧反射 Mixin，执行首次完整 build | build 通过，Forge/MixinExtras/旧反射 Mixin 残留清零 |
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
| E-NF-MODFILE | NeoForge `1.21.1` mod files 文档 | `neoforge.mods.toml` 位于 `src/main/resources/META-INF/neoforge.mods.toml`；metadata 支持 `[[mixins]]` 与 `[[dependencies.<modid>]]`。 | 必须删除旧 `mods.toml` 并创建 `neoforge.mods.toml`。 |
| E-NF-CONFIG | NeoForge `1.21.1` config 文档 | `ModConfigSpec` 在 mod constructor 中通过 `ModContainer#registerConfig` 注册。 | 主类必须使用 NeoForge `ModContainer` 注册 client config。 |
| E-MDG | ModDevGradle 文档 | ModDevGradle 支持 Gradle `8.8`；使用 `neoForge { version = ... runs { client { client() } } mods { ... } }`。 | Gradle wrapper 锁定 `8.8-bin`，build 使用 `net.neoforged.moddev`。 |
| E-TACZ-PROPS | `MUKSC/TACZ-1.21.1` `gradle.properties` | `mod_id=tacz`、`mod_version=1.1.7-hotfix-r5`、`archives_base_name=tacz-neoforge-1.21.1`。 | TaCZ 依赖锁定本地 jar 路径。 |
| E-TACZ-RECOIL | TaCZ `CameraSetupEvent` | 存在 `applyCameraRecoil(ViewportEvent.ComputeCameraAngles)`；descriptor 必须为 `(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V`。 | MixinCameraSetupEvent target 固定。 |
| E-TACZ-CROSSHAIR | TaCZ `ShoulderSurfingCompat` | 存在 `showCrosshair()`；descriptor 必须为 `()Z`。 | Crosshair Mixin owner 固定为 `ShoulderSurfingCompat`。 |
| E-TACZ-TICK | TaCZ `TickAnimationEvent` | `tickAnimation` 存在 overload；目标 descriptor 必须为 `(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V`。 | Mixin 必须写完整 descriptor，不得使用裸方法名。 |
| E-TACZ-DRAW | TaCZ `LocalPlayerDraw` | `draw(ItemStack)` descriptor 必须为 `(Lnet/minecraft/world/item/ItemStack;)V`。 | Draw guard Mixin target 固定。 |
| E-LW-CAMERA | Leawind `CameraAgent` | `turnCamera(double, double)` descriptor 必须为 `(DD)V`，语义为 `(dYRot, dXRot)`。 | recoil 差值传参固定为 `turnCamera(dYRot, dXRot)`。 |
| E-LW-STATUS | Leawind `ThirdPersonStatus` | 存在 `isRenderingInThirdPerson()`、`shouldRenderThirdPersonCrosshair()`、`isPerspectiveInverted`。 | `LeawindBridge` API 固定。 |

---

## 4. 未决阻塞项

| Blocker ID | 阻塞内容 | 解除条件 | 阻塞期间禁止事项 |
|---|---|---|---|
| B-WF-001 | 本仓库 memory layer 不符合 `units` / `activeSprint` schema，或当前执行分支不是 `codex/neoforge-1.21.1`。 | 修复 memory layer 读取逻辑或切回 `codex/neoforge-1.21.1`；不得覆盖历史。 | 禁止进入 S01。 |
| B-DEP-001 | 三个本地依赖 jar 未放置或 SHA-256 未记录。 | 三个 jar 路径存在，`Get-FileHash -Algorithm SHA256` 输出写入 progress。 | 禁止进入 S03。 |
| B-API-001 | `javap -s -p` 输出未同时匹配方法声明行与紧邻 `descriptor:` 行。 | descriptor gate 脚本全部通过；方法声明与 descriptor 成对匹配锁定值。 | 禁止写 Java 代码，禁止进入 S03。 |
| B-GRADLE-001 | Gradle 配置门禁失败。 | 当前切片 allowed files 内修复；重新执行对应门禁并通过。 | 禁止进入下一切片。 |
| B-BUILD-001 | S07 或 close-out build 失败。 | 执行 `S07R-DIAG`，分派到对应 `S07R-*` 回修切片，重新执行 S07。 | 禁止在 S07 临时修改不在 allowlist 的文件。 |
| B-RUNTIME-001 | S09 runtime/gameplay 验证失败。 | 执行 `S09R-DIAG`，分派到对应 `S09R-*` 回修切片，重新执行受影响验证项。 | 禁止在 S09 临时修改代码。 |
| B-MIXIN-001 | runClient 日志出现 `InvalidInjectionException`、`MixinApplyError`、`ClassNotFoundException` 或 Mixin target 未命中。 | 执行 `S09R-DIAG`，由诊断流程分派到 `S09R-S03` / `S09R-S04` / `S09R-S05` / `S09R-S06` / `S09R-S07`。 | 禁止完成 S09。 |

---

## 5. 执行规则

### 5.1 PowerShell helper

S00 完成后，后续切片必须使用下列 PowerShell helper。helper 只存在于当前 shell 会话，重新打开 PowerShell 后必须重新执行本节。

```powershell
function Assert-PathExists {
    param([Parameter(Mandatory=$true)][string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Missing required path: $Path"
    }
}

function Assert-PathAbsent {
    param([Parameter(Mandatory=$true)][string]$Path)
    if (Test-Path -LiteralPath $Path) {
        throw "Forbidden path exists: $Path"
    }
}

function Assert-RgAbsent {
    param(
        [Parameter(Mandatory=$true)][string]$Pattern,
        [Parameter(Mandatory=$true)][string[]]$Paths
    )
    & rg --line-number --fixed-strings -- $Pattern @Paths
    if ($LASTEXITCODE -eq 0) {
        throw "Forbidden text found: $Pattern"
    }
    if ($LASTEXITCODE -gt 1) {
        throw "rg failed while searching: $Pattern"
    }
}

function Assert-ExactMixinCameraRemoval {
    Assert-PathAbsent 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java'
    $mixinsJsonPath = 'src/main/resources/levanilla_tacztps.mixins.json'
    Assert-PathExists $mixinsJsonPath
    $mixinsJson = Get-Content -LiteralPath $mixinsJsonPath -Raw
    if ($mixinsJson -match '"client\.MixinCamera"') {
        throw 'Forbidden exact mixin entry remains: "client.MixinCamera"'
    }
}

function Invoke-GraphifyCloseout {
    python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout
    if ($LASTEXITCODE -ne 0) {
        throw 'graphify rebuild failed'
    }
}
```

### 5.2 Memory layer update templates

所有状态写入必须追加，不得覆盖历史。

切片开始模板：

```powershell
$env:SLICE_ID = 'SXX'
Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value "`n- START $env:SLICE_ID $(Get-Date -Format o)"

$script = @'
import json, os
from pathlib import Path

slice_id = os.environ["SLICE_ID"]
path = Path(".tacz-leawindtps-builder/sprint_plan.json")
data = json.loads(path.read_text(encoding="utf-8"))
mig = data.setdefault("activeSprint", {}).setdefault("neoforge1211Migration", {})
mig["executionBranch"] = "codex/neoforge-1.21.1"
mig["targetReleaseBranch"] = "neoforge-1.21.1"
items = mig.setdefault("slices", [])
for item in items:
    if item.get("id") == slice_id:
        item["status"] = "in_progress"
        item["started"] = True
        break
else:
    items.append({"id": slice_id, "status": "in_progress", "started": True, "completed": False, "blocker": None})
path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
'@
$script | python3 -
```

切片阻塞模板：

```powershell
$env:SLICE_ID = 'SXX'
$env:BLOCKER_ID = 'B-XXXX'
Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value "`n- BLOCKED $env:SLICE_ID $env:BLOCKER_ID $(Get-Date -Format o)"

$script = @'
import json, os
from pathlib import Path

slice_id = os.environ["SLICE_ID"]
blocker_id = os.environ["BLOCKER_ID"]
path = Path(".tacz-leawindtps-builder/sprint_plan.json")
data = json.loads(path.read_text(encoding="utf-8"))
mig = data.setdefault("activeSprint", {}).setdefault("neoforge1211Migration", {})
items = mig.setdefault("slices", [])
for item in items:
    if item.get("id") == slice_id:
        item["status"] = "blocked"
        item["blocker"] = blocker_id
        break
else:
    items.append({"id": slice_id, "status": "blocked", "started": True, "completed": False, "blocker": blocker_id})
path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
'@
$script | python3 -
```

切片完成模板：

```powershell
$env:SLICE_ID = 'SXX'
Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value "`n- DONE $env:SLICE_ID $(Get-Date -Format o)"

$script = @'
import json, os
from pathlib import Path

slice_id = os.environ["SLICE_ID"]
path = Path(".tacz-leawindtps-builder/sprint_plan.json")
data = json.loads(path.read_text(encoding="utf-8"))
mig = data.setdefault("activeSprint", {}).setdefault("neoforge1211Migration", {})
items = mig.setdefault("slices", [])
for item in items:
    if item.get("id") == slice_id:
        item["status"] = "done"
        item["completed"] = True
        item["blocker"] = None
        break
else:
    items.append({"id": slice_id, "status": "done", "started": True, "completed": True, "blocker": None})
path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
'@
$script | python3 -
```

Unit implemented 模板：

```powershell
$env:UNIT_ID = 'NF1211-BUILD'

$script = @'
import json, os
from pathlib import Path

unit_id = os.environ["UNIT_ID"]
path = Path(".tacz-leawindtps-builder/feature_list.json")
data = json.loads(path.read_text(encoding="utf-8"))
units = data.setdefault("units", [])
for unit in units:
    if unit.get("id") == unit_id:
        unit["implemented"] = True
        break
else:
    units.append({"id": unit_id, "implemented": True, "passes": False, "validated_at": None, "evidence": []})
path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
'@
$script | python3 -
```

Unit pass 模板只允许在真实验证通过后执行：

```powershell
$env:UNIT_ID = 'NF1211-BUILD'
$env:EVIDENCE_TEXT = 'actual validation evidence here'

$script = @'
import json, os
from datetime import datetime, timezone
from pathlib import Path

unit_id = os.environ["UNIT_ID"]
evidence = os.environ["EVIDENCE_TEXT"]
path = Path(".tacz-leawindtps-builder/feature_list.json")
data = json.loads(path.read_text(encoding="utf-8"))
for unit in data.get("units", []):
    if unit.get("id") == unit_id:
        unit["passes"] = True
        unit["validated_at"] = datetime.now(timezone.utc).isoformat()
        unit.setdefault("evidence", []).append(evidence)
        break
else:
    raise SystemExit(f"unit not found: {unit_id}")
path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
'@
$script | python3 -
```

### 5.3 Close-out rule for code slices

每个代码改动切片收尾必须执行：

```powershell
Invoke-GraphifyCloseout
```

该命令失败时，当前切片必须写入 blocker，blocker 使用 `B-GRADLE-001` 或 `B-BUILD-001`。

### 5.4 `passes=true` 写入规则

`passes=true` 只能在验证真实执行且 pass/fail 判据满足后写入。实现完成但未验证时，必须保持 `passes=false`。S02–S08 只能写 `implemented=true`，不得写 `passes=true`。

---

## 6. 切片总览

| 切片 ID | 阶段 | 名称 | 是否代码改动 | 主要产物 |
|---|---|---|---:|---|
| S00 | Phase 0 | Workflow 追加初始化与分支门禁 | 否 | memory layer 追加 NF1211 units，保留历史 |
| S01 | Phase 0 | 证据归档与 blocker 建立 | 否 | progress 证据摘要 |
| S02 | Phase 1 | Gradle / NeoForge / 本地依赖迁移 | 是 | Gradle 文件、本地 jar gate、完整 descriptor gate |
| S03 | Phase 2 | NeoForge metadata、主类、配置迁移 | 是 | `neoforge.mods.toml`、mixin json、entrypoint、config |
| S04 | Phase 3 | Leawind bridge、recoil guard、recoil pipeline | 是 | `LeawindBridge`、`RecoilCallGuard`、recoil Mixin、event subscriber |
| S05 | Phase 4 | Crosshair pipeline | 是 | `MixinShoulderSurfingCompat`，删除旧 crosshair Mixin |
| S06 | Phase 5 | Aiming / scoping perspective pipeline | 是 | `MixinTickAnimationEvent` |
| S07 | Phase 6 | LocalPlayerDraw 防崩与首次 build gate | 是 | draw guard、删除旧反射 Mixin、首次完整 build |
| S08 | Phase 7 | README 与发布元数据 | 否 | README |
| S09 | Phase 7 | 全量验证与 close-out | 否 | 验证 evidence、passes、close-out |
| S07R-DIAG | Remediation | S07 build 失败诊断 | 否 | 回修分派 |
| S07R-S02 | Remediation | 修复 Gradle / dependency 写集 | 是 | S02 写集 |
| S07R-S03 | Remediation | 修复 metadata / config 写集 | 是 | S03 写集 |
| S07R-S04 | Remediation | 修复 recoil 写集 | 是 | S04 写集 |
| S07R-S05 | Remediation | 修复 crosshair 写集 | 是 | S05 写集 |
| S07R-S06 | Remediation | 修复 perspective 写集 | 是 | S06 写集 |
| S07R-S07 | Remediation | 修复 draw guard / cleanup 写集 | 是 | S07 写集 |
| S09R-DIAG | Remediation | runtime/gameplay 失败诊断 | 否 | 回修分派 |
| S09R-S03 | Remediation | 修复 runtime metadata/config | 是 | S03 写集 |
| S09R-S04 | Remediation | 修复 runtime recoil | 是 | S04 写集 |
| S09R-S05 | Remediation | 修复 runtime crosshair | 是 | S05 写集 |
| S09R-S06 | Remediation | 修复 runtime aiming/scoping | 是 | S06 写集 |
| S09R-S07 | Remediation | 修复 runtime draw guard | 是 | S07 写集 |
| S09R-S08 | Remediation | 修复 README / docs | 否 | S08 写集 |

---

## 7. 详细切片

### S00：Workflow 追加初始化与分支门禁

#### 目标

确认当前执行分支为 `codex/neoforge-1.21.1`，保留 `.tacz-leawindtps-builder/` 既有历史，并在现有 `units` / `activeSprint` schema 下追加 NeoForge 迁移工作单元。

#### 前置条件

- 当前工作区为 fork 后本地仓库。
- 当前执行分支为 `codex/neoforge-1.21.1`。
- `scripts/dev/setup_graphify_local.py` 存在。
- `.tacz-leawindtps-builder/feature_list.json` 存在并含 `units`。
- `.tacz-leawindtps-builder/sprint_plan.json` 存在并含 `activeSprint`。
- `.tacz-leawindtps-builder/progress.md` 存在。

#### 允许修改文件

```text
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

#### 禁止修改文件

```text
src/
build.gradle
settings.gradle
gradle.properties
gradle/
README.md
LICENSE
```

#### 文件动作

- 不得覆盖三个 builder 文件。
- 必须向 `progress.md` 追加记录。
- 必须向 `feature_list.json.units` 合并追加 `NF1211-*` units。
- 必须向 `sprint_plan.json.activeSprint` 合并追加 `neoforge1211Migration`。
- 必须记录 `executionBranch = codex/neoforge-1.21.1`。
- 必须记录 `targetReleaseBranch = neoforge-1.21.1`。

#### 必须执行的命令

```powershell
$currentBranch = git branch --show-current
if ($currentBranch -ne 'codex/neoforge-1.21.1') {
    throw "Wrong execution branch: $currentBranch. Expected codex/neoforge-1.21.1"
}

Assert-PathExists 'scripts/dev/setup_graphify_local.py'
Assert-PathExists '.tacz-leawindtps-builder/progress.md'
Assert-PathExists '.tacz-leawindtps-builder/feature_list.json'
Assert-PathExists '.tacz-leawindtps-builder/sprint_plan.json'

python3 -m json.tool .tacz-leawindtps-builder/feature_list.json | Out-Null
python3 -m json.tool .tacz-leawindtps-builder/sprint_plan.json | Out-Null

$script = @'
import json
from pathlib import Path

feature_path = Path(".tacz-leawindtps-builder/feature_list.json")
sprint_path = Path(".tacz-leawindtps-builder/sprint_plan.json")

feature_data = json.loads(feature_path.read_text(encoding="utf-8"))
sprint_data = json.loads(sprint_path.read_text(encoding="utf-8"))

if "units" not in feature_data or not isinstance(feature_data["units"], list):
    raise SystemExit("feature_list.json must contain units[]")
if "activeSprint" not in sprint_data or not isinstance(sprint_data["activeSprint"], dict):
    raise SystemExit("sprint_plan.json must contain activeSprint{}")

required_units = [
    ("NF1211-WORKFLOW", "NeoForge 1.21.1 workflow memory layer"),
    ("NF1211-BUILD", "NeoForge 1.21.1 Gradle build and local dependency gate"),
    ("NF1211-METADATA", "NeoForge metadata and client config"),
    ("NF1211-RECOIL", "TaCZ recoil redirected to Leawind camera"),
    ("NF1211-CROSSHAIR", "TaCZ crosshair respects Leawind third-person crosshair state"),
    ("NF1211-AIM-PERSPECTIVE", "aiming and scoping invert Leawind perspective according to config"),
    ("NF1211-DRAW-GUARD", "LocalPlayerDraw null hitResult guard"),
    ("NF1211-DOCS", "README and release notes aligned with GPL and target versions"),
]

existing = {unit.get("id"): unit for unit in feature_data["units"]}
for unit_id, name in required_units:
    if unit_id not in existing:
        feature_data["units"].append({
            "id": unit_id,
            "name": name,
            "implemented": False,
            "passes": False,
            "validated_at": None,
            "evidence": []
        })
    else:
        unit = existing[unit_id]
        unit.setdefault("name", name)
        unit.setdefault("implemented", False)
        unit.setdefault("passes", False)
        unit.setdefault("validated_at", None)
        unit.setdefault("evidence", [])

mig = sprint_data["activeSprint"].setdefault("neoforge1211Migration", {})
mig["executionBranch"] = "codex/neoforge-1.21.1"
mig["targetReleaseBranch"] = "neoforge-1.21.1"
mig.setdefault("slices", [])
existing_slices = {item.get("id"): item for item in mig["slices"]}
for sid in ["S00","S01","S02","S03","S04","S05","S06","S07","S08","S09"]:
    if sid not in existing_slices:
        mig["slices"].append({"id": sid, "status": "pending", "started": False, "completed": False, "blocker": None})

feature_path.write_text(json.dumps(feature_data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
sprint_path.write_text(json.dumps(sprint_data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
'@
$script | python3 -

Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value "`n## NF1211 migration initialized $(Get-Date -Format o)`n- executionBranch: codex/neoforge-1.21.1`n- targetReleaseBranch: neoforge-1.21.1"
```

#### 验证步骤

```powershell
$script = @'
import json
from pathlib import Path

feature = json.loads(Path(".tacz-leawindtps-builder/feature_list.json").read_text(encoding="utf-8"))
sprint = json.loads(Path(".tacz-leawindtps-builder/sprint_plan.json").read_text(encoding="utf-8"))

ids = {unit.get("id") for unit in feature["units"]}
missing = [x for x in ["NF1211-WORKFLOW","NF1211-BUILD","NF1211-METADATA","NF1211-RECOIL","NF1211-CROSSHAIR","NF1211-AIM-PERSPECTIVE","NF1211-DRAW-GUARD","NF1211-DOCS"] if x not in ids]
if missing:
    raise SystemExit("Missing NF1211 units: " + ", ".join(missing))

mig = sprint["activeSprint"]["neoforge1211Migration"]
if mig["executionBranch"] != "codex/neoforge-1.21.1":
    raise SystemExit("Wrong executionBranch")
if mig["targetReleaseBranch"] != "neoforge-1.21.1":
    raise SystemExit("Wrong targetReleaseBranch")
'@
$script | python3 -
```

Pass 判据：branch 正确；builder JSON 有效；`units` 与 `activeSprint.neoforge1211Migration` 存在；历史未覆盖。

#### 完成后同步

- `NF1211-WORKFLOW.implemented=true`
- `S00.status=done`
- 不写 `passes=true`

---

### S01：证据归档与 blocker 建立

#### 目标

把证据锁定摘要追加到 progress，并确认 blocker 表使用 `S09R-DIAG`，不存在未定义 remediation 引用。

#### 允许修改文件

```text
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/sprint_plan.json
```

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S01'
# 执行 5.2 切片开始模板

Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value @'

## NF1211 Evidence Lock Summary

- Current execution branch locked to `codex/neoforge-1.21.1`.
- Target release branch locked to `neoforge-1.21.1`.
- Windows PowerShell command surface locked.
- Gradle wrapper command locked to `.\gradlew.bat`.
- Descriptor gate requires paired javap method declaration line and adjacent descriptor line.
- Old reflection recoil file must be absent by exact path.
- Mixins JSON must not contain exact entry `"client.MixinCamera"`.
- Runtime Mixin failures route to `S09R-DIAG`.
'@

$env:SLICE_ID = 'S01'
# 执行 5.2 切片完成模板
```

#### 验证步骤

```powershell
rg --line-number --fixed-strings -- 'S09R-DIAG' '.tacz-leawindtps-builder/progress.md'
rg --line-number --fixed-strings -- 'codex/neoforge-1.21.1' '.tacz-leawindtps-builder/progress.md'
```

#### 完成后同步

- `S01.status=done`

---

### S02：Gradle / NeoForge / 本地依赖迁移

#### 目标

把 ForgeGradle 工程迁移为 NeoForge 1.21.1 + ModDevGradle 工程，锁定本地依赖 jar，并执行完整 descriptor gate。

#### 允许修改文件

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

#### 禁止修改文件

```text
src/main/java/
src/main/resources/
README.md
LICENSE
libs/*.jar
```

#### 文件动作

- 完全替换 `settings.gradle`。
- 完全替换 `gradle.properties`。
- 完全替换 `build.gradle`。
- 完全替换 `gradle/wrapper/gradle-wrapper.properties`。
- 创建 `libs/.gitkeep`。
- 更新 `.gitignore`，禁止提交 `libs/*.jar`。
- 执行完整 descriptor gate。

#### final content：`settings.gradle`

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

#### final content：`gradle.properties`

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

#### final content：`build.gradle`

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

sourceSets.main.resources {
    srcDir('src/generated/resources')
}

configurations {
    localRuntime
    runtimeClasspath.extendsFrom localRuntime
}

dependencies {
    compileOnly files(localModJars)
    localRuntime files(localModJars)
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
                'Specification-Title'     : mod_name,
                'Specification-Vendor'    : mod_authors,
                'Specification-Version'   : mod_version,
                'Implementation-Title'    : mod_name,
                'Implementation-Version'  : mod_version,
                'Implementation-Vendor'   : mod_authors
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

#### final content：`gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.8-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

#### `.gitignore` 必须包含

```gitignore
# Local mod dependencies
/libs/*.jar
!/libs/.gitkeep
```

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S02'
# 执行 5.2 切片开始模板

New-Item -ItemType Directory -Force -Path 'libs' | Out-Null
if (-not (Test-Path -LiteralPath 'libs/.gitkeep')) {
    New-Item -ItemType File -Path 'libs/.gitkeep' | Out-Null
}

Assert-PathExists 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar'
Assert-PathExists 'libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar'
Assert-PathExists 'libs/architectury-api-13.0.8-neoforge.jar'

Get-FileHash -Algorithm SHA256 -LiteralPath 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' | Format-List | Out-String | Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8
Get-FileHash -Algorithm SHA256 -LiteralPath 'libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar' | Format-List | Out-String | Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8
Get-FileHash -Algorithm SHA256 -LiteralPath 'libs/architectury-api-13.0.8-neoforge.jar' | Format-List | Out-String | Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8
```

#### 完整 descriptor gate

必须执行以下 PowerShell。该 gate 同时验证方法声明行和紧邻 `descriptor:` 行。任何不匹配必须触发 `B-API-001`。

```powershell
function Assert-JavapMethodDescriptor {
    param(
        [Parameter(Mandatory=$true)][string]$Jar,
        [Parameter(Mandatory=$true)][string]$ClassName,
        [Parameter(Mandatory=$true)][string]$MethodLineRegex,
        [Parameter(Mandatory=$true)][string]$DescriptorRegex,
        [Parameter(Mandatory=$true)][string]$Label
    )

    $text = & javap -classpath $Jar -s -p $ClassName | Out-String
    if ($LASTEXITCODE -ne 0) {
        throw "javap failed for $Label"
    }

    $lines = $text -split "`r?`n"
    for ($i = 0; $i -lt $lines.Length; $i++) {
        if ($lines[$i] -match $MethodLineRegex) {
            $j = $i + 1
            while ($j -lt $lines.Length -and $lines[$j].Trim().Length -eq 0) {
                $j++
            }

            if ($j -ge $lines.Length -or $lines[$j] -notmatch '^\s*descriptor:\s*(.+?)\s*$') {
                throw "Method found but adjacent descriptor line missing for $Label"
            }

            $descriptor = $matches[1]
            if ($descriptor -match $DescriptorRegex) {
                Add-Content -LiteralPath '.tacz-leawindtps-builder/progress.md' -Encoding UTF8 -Value "- DESCRIPTOR PASS $Label $descriptor"
                return
            }

            throw "Descriptor mismatch for ${Label}: ${descriptor}"
        }
    }

    throw "Method declaration not found for $Label"
}

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.client.event.CameraSetupEvent' `
    -MethodLineRegex 'public static void applyCameraRecoil\(net\.neoforged\.neoforge\.client\.event\.ViewportEvent\$ComputeCameraAngles\);' `
    -DescriptorRegex '^\(Lnet/neoforged/neoforge/client/event/ViewportEvent\$ComputeCameraAngles;\)V$' `
    -Label 'CameraSetupEvent.applyCameraRecoil'

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.compat.shouldersurfing.ShoulderSurfingCompat' `
    -MethodLineRegex 'public static boolean showCrosshair\(\);' `
    -DescriptorRegex '^\(\)Z$' `
    -Label 'ShoulderSurfingCompat.showCrosshair'

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.client.event.TickAnimationEvent' `
    -MethodLineRegex 'public static void tickAnimation\(net\.neoforged\.neoforge\.client\.event\.ClientTickEvent\$Pre\);' `
    -DescriptorRegex '^\(Lnet/neoforged/neoforge/client/event/ClientTickEvent\$Pre;\)V$' `
    -Label 'TickAnimationEvent.tickAnimation.ClientTickEvent.Pre'

Assert-JavapMethodDescriptor `
    -Jar 'libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar' `
    -ClassName 'com.tacz.guns.client.gameplay.LocalPlayerDraw' `
    -MethodLineRegex 'public void draw\(net\.minecraft\.world\.item\.ItemStack\);' `
    -DescriptorRegex '^\(Lnet/minecraft/world/item/ItemStack;\)V$' `
    -Label 'LocalPlayerDraw.draw'

Assert-JavapMethodDescriptor `
    -Jar 'libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar' `
    -ClassName 'com.github.leawind.thirdperson.core.CameraAgent' `
    -MethodLineRegex 'public void turnCamera\(double, double\);' `
    -DescriptorRegex '^\(DD\)V$' `
    -Label 'CameraAgent.turnCamera'
```

#### Gradle 门禁

```powershell
.\gradlew.bat --version
if ($LASTEXITCODE -ne 0) { throw 'Gradle wrapper failed' }

.\gradlew.bat verifyLocalModJars
if ($LASTEXITCODE -ne 0) { throw 'verifyLocalModJars failed' }

Invoke-GraphifyCloseout
```

#### 验证步骤

```powershell
$trackedJars = git ls-files -- 'libs/*.jar'
if ($trackedJars) {
    throw "Local dependency jars are tracked by Git: $trackedJars"
}
```

Pass 判据：三 jar 存在；SHA-256 已记录；完整 descriptor gate 通过；`verifyLocalModJars` 通过；本地 jar 未被 Git 跟踪。

#### 失败停止条件

- jar 缺失：写 `B-DEP-001`。
- descriptor 不匹配：写 `B-API-001`。
- Gradle wrapper 或 verify task 失败：写 `B-GRADLE-001`。

#### 完成后同步

- `NF1211-BUILD.implemented=true`
- `S02.status=done`
- 不写 `passes=true`

---

### S03：NeoForge metadata、主类、配置迁移

#### 目标

迁移 NeoForge metadata、Mixin metadata、mod entrypoint 和 client config。

#### 允许修改文件

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

#### 禁止修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/
src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java
src/main/java/com/levanilla/TacZtps/compat/
build.gradle
gradle.properties
settings.gradle
README.md
LICENSE
```

#### 文件动作

- 删除 `src/main/resources/META-INF/mods.toml`。
- 创建或完全替换 `src/main/resources/META-INF/neoforge.mods.toml`。
- 完全替换 `src/main/resources/levanilla_tacztps.mixins.json`。
- 完全替换 `src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java`。
- 完全替换 `src/main/java/com/levanilla/TacZtps/ClientConfig.java`。

#### final content：`src/main/resources/META-INF/neoforge.mods.toml`

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

#### final content：`src/main/resources/levanilla_tacztps.mixins.json`

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

#### final content：`src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java`

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

#### final content：`src/main/java/com/levanilla/TacZtps/ClientConfig.java`

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

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S03'
# 执行 5.2 切片开始模板

Remove-Item -LiteralPath 'src/main/resources/META-INF/mods.toml' -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path 'src/main/resources/META-INF' | Out-Null

Assert-PathAbsent 'src/main/resources/META-INF/mods.toml'
Assert-PathExists 'src/main/resources/META-INF/neoforge.mods.toml'
Assert-PathExists 'src/main/resources/levanilla_tacztps.mixins.json'

Assert-RgAbsent 'net.minecraftforge' @('src/main/java','src/main/resources')
Assert-RgAbsent 'requiredMods' @('src/main/resources')
Assert-RgAbsent 'com.llamalad7.mixinextras' @('src/main/java','src/main/resources','build.gradle','gradle.properties')

.\gradlew.bat processResources
if ($LASTEXITCODE -ne 0) { throw 'processResources failed' }

Invoke-GraphifyCloseout

$env:UNIT_ID = 'NF1211-METADATA'
# 执行 5.2 Unit implemented 模板

$env:SLICE_ID = 'S03'
# 执行 5.2 切片完成模板
```

#### 验证步骤

```powershell
Assert-PathExists 'build/resources/main/META-INF/neoforge.mods.toml'
Assert-PathExists 'build/resources/main/levanilla_tacztps.mixins.json'
```

Pass 判据：旧 `mods.toml` 不存在；NeoForge metadata 存在；mixin json 不包含 `"client.MixinCamera"`；`processResources` 通过。

#### 失败停止条件

- `processResources` 失败：写 `B-GRADLE-001`。
- metadata 静态检查失败：写 `B-GRADLE-001`。

---

### S04：Leawind bridge、recoil guard、recoil pipeline

#### 目标

实现 Leawind bridge、recoil guard、TaCZ recoil 取消与手动转接 pipeline。

#### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/compat/LeawindBridge.java
src/main/java/com/levanilla/TacZtps/compat/RecoilCallGuard.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java
src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

#### final content：`src/main/java/com/levanilla/TacZtps/compat/LeawindBridge.java`

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

#### final content：`src/main/java/com/levanilla/TacZtps/compat/RecoilCallGuard.java`

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

#### final content：`src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java`

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
| 选择原因 | TaCZ 原生 recoil 在该方法直接修改 player rotation；HEAD cancel 阻止原生 event path 对 player 生效；`RecoilCallGuard` 保证手动调用不被取消。 |
| 命中验证 | S09 V03 必须验证 `.mixin.out` 导出与日志无 Mixin 错误。 |

#### final content：`src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java`

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

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S04'
# 执行 5.2 切片开始模板

New-Item -ItemType Directory -Force -Path 'src/main/java/com/levanilla/TacZtps/compat' | Out-Null

rg --line-number --fixed-strings -- 'LeawindBridge.turnCamera(dYRot, dXRot);' 'src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java'
if ($LASTEXITCODE -ne 0) { throw 'Recoil argument order is wrong' }

rg --line-number --fixed-strings -- 'method = "applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V"' 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java'
if ($LASTEXITCODE -ne 0) { throw 'CameraSetupEvent descriptor missing' }

Invoke-GraphifyCloseout

$env:UNIT_ID = 'NF1211-RECOIL'
# 执行 5.2 Unit implemented 模板

$env:SLICE_ID = 'S04'
# 执行 5.2 切片完成模板
```

#### 完成定义

参数顺序、descriptor 字符串、graphify closeout 全部通过。runtime recoil 验证在 S09 V03 执行。

---

### S05：Crosshair pipeline

#### 目标

实现 TaCZ crosshair 对 Leawind 第三人称准星状态的兼容，删除旧 `RenderCrosshairEvent` lambda Mixin。

#### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java
src/main/resources/levanilla_tacztps.mixins.json
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

#### 文件动作

- 删除 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java`。
- 创建或完全替换 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java`。
- `src/main/resources/levanilla_tacztps.mixins.json` 必须包含 `client.MixinShoulderSurfingCompat`。
- `src/main/resources/levanilla_tacztps.mixins.json` 不得包含精确条目 `"client.MixinCamera"`。

#### final content：`src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java`

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
| 选择原因 | TaCZ crosshair 流程中两个 force-show 判断都调用该 wrapper；hook 该方法覆盖 RenderCrosshairEvent 与 renderCrosshair 两处调用，避开不可锁定的 lambda 名称。 |
| 命中验证 | S09 V04 必须验证 `.mixin.out` 导出与日志无 Mixin 错误。 |

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S05'
# 执行 5.2 切片开始模板

Remove-Item -LiteralPath 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java' -Force -ErrorAction SilentlyContinue

Assert-PathAbsent 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java'
Assert-PathExists 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinShoulderSurfingCompat.java'
Assert-ExactMixinCameraRemoval
Assert-RgAbsent 'com.llamalad7.mixinextras' @('src/main','build.gradle','gradle.properties')

Invoke-GraphifyCloseout

$env:UNIT_ID = 'NF1211-CROSSHAIR'
# 执行 5.2 Unit implemented 模板

$env:SLICE_ID = 'S05'
# 执行 5.2 切片完成模板
```

#### 完成定义

旧 crosshair Mixin 删除，新 wrapper Mixin 存在，精确旧反射 Mixin 检查通过，graphify closeout 通过。runtime crosshair 验证在 S09 V04 执行。

---

### S06：Aiming / scoping perspective pipeline

#### 目标

实现 TaCZ 瞄准与开镜时按配置触发 Leawind 第一人称过渡。

#### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

#### final content：`src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java`

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
| 选择原因 | 该 overload 在 client tick 上处理 TaCZ 动画状态；`RenderFrameEvent.Post` overload 不作为 perspective config 入口。 |
| 命中验证 | S09 V05 必须验证 `.mixin.out` 导出与日志无 ambiguous target 或 Mixin 错误。 |

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S06'
# 执行 5.2 切片开始模板

rg --line-number --fixed-strings -- 'method = "tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V"' 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java'
if ($LASTEXITCODE -ne 0) { throw 'TickAnimationEvent ClientTickEvent.Pre descriptor missing' }

Invoke-GraphifyCloseout

$env:UNIT_ID = 'NF1211-AIM-PERSPECTIVE'
# 执行 5.2 Unit implemented 模板

$env:SLICE_ID = 'S06'
# 执行 5.2 切片完成模板
```

#### 完成定义

descriptor 字符串静态检查通过，graphify closeout 通过。runtime aim/scoping 验证在 S09 V05 执行。

---

### S07：LocalPlayerDraw 防崩与首次 build gate

#### 目标

替换 `MixinLocalPlayerDraw.java`，删除 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java`，完成首次 `.\gradlew.bat clean build`。

#### 允许修改文件

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java
src/main/resources/levanilla_tacztps.mixins.json
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

#### 文件动作

- 完全替换 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java`。
- 删除 `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java`。
- `src/main/resources/levanilla_tacztps.mixins.json` 不得包含精确条目 `"client.MixinCamera"`。

#### final content：`src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java`

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
| 选择原因 | `draw(ItemStack)` 开始前补齐 `Minecraft#hitResult`，避免 TaCZ draw 逻辑读取空 hitResult。 |
| 命中验证 | S09 V06 必须验证 `.mixin.out` 导出与快速切枪无 NPE。 |

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S07'
# 执行 5.2 切片开始模板

Remove-Item -LiteralPath 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java' -Force -ErrorAction SilentlyContinue
Assert-ExactMixinCameraRemoval

Assert-RgAbsent 'net.minecraftforge' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'com.llamalad7.mixinextras' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'requiredMods' @('src/main','src/main/resources')

.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) {
    $env:SLICE_ID = 'S07'
    $env:BLOCKER_ID = 'B-BUILD-001'
    # 执行 5.2 切片阻塞模板
    throw 'S07 build failed; execute S07R-DIAG'
}

Invoke-GraphifyCloseout

$env:UNIT_ID = 'NF1211-DRAW-GUARD'
# 执行 5.2 Unit implemented 模板

$env:SLICE_ID = 'S07'
# 执行 5.2 切片完成模板
```

#### 验证步骤

```powershell
Assert-ExactMixinCameraRemoval
Assert-PathAbsent 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java'
Assert-PathExists 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar'
```

Pass 判据：首次完整 build 通过；精确旧反射 Mixin 检查通过；旧 crosshair Mixin 不存在；Forge/MixinExtras/requiredMods 无残留。

#### 失败停止条件

- build 失败：写 `B-BUILD-001`，进入 `S07R-DIAG`。
- 精确旧反射 Mixin 检查失败：按 S07 写集修复后重跑 S07。
- graphify closeout 失败：写 `B-BUILD-001`。

---

### S08：README 与发布元数据

#### 目标

替换 README，使目标版本、非官方范围、依赖安装、GPLv3 义务、源码发布要求与本分支一致。

#### 允许修改文件

```text
README.md
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

#### final content：`README.md`

```md
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
```

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S08'
# 执行 5.2 切片开始模板

rg --line-number --fixed-strings -- 'Minecraft: `1.21.1`' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing Minecraft target' }

rg --line-number --fixed-strings -- 'NeoForge: `21.1.145`' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing NeoForge target' }

rg --line-number --fixed-strings -- 'GPL-3.0-only' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing GPL target' }

rg --line-number --fixed-strings -- 'These jars are ignored by Git and must not be committed.' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing local jar rule' }

& rg --line-number -- '/Users/|C:\\|/home/' README.md
if ($LASTEXITCODE -eq 0) { throw 'README contains machine-private absolute path' }
if ($LASTEXITCODE -gt 1) { throw 'README path scan failed' }

$env:UNIT_ID = 'NF1211-DOCS'
# 执行 5.2 Unit implemented 模板

$env:SLICE_ID = 'S08'
# 执行 5.2 切片完成模板
```

#### 完成定义

README 目标版本、license、本地 jar 规则、路径卫生全部通过。

---

### S09：全量验证与 close-out

#### 目标

执行全量验证 runbook，逐项写入 `passes=true` 与 evidence，完成 close-out。

#### 允许修改文件

```text
.tacz-leawindtps-builder/progress.md
.tacz-leawindtps-builder/feature_list.json
.tacz-leawindtps-builder/sprint_plan.json
```

#### 禁止修改文件

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

#### 必须执行的命令

```powershell
$env:SLICE_ID = 'S09'
# 执行 5.2 切片开始模板

$script = @'
import json
from pathlib import Path

sprint = json.loads(Path(".tacz-leawindtps-builder/sprint_plan.json").read_text(encoding="utf-8"))
items = sprint["activeSprint"]["neoforge1211Migration"]["slices"]
missing = [item["id"] for item in items if item["id"] in [f"S{i:02d}" for i in range(0,9)] and item.get("status") != "done"]
if missing:
    raise SystemExit("Previous slices not done: " + ", ".join(missing))
'@
$script | python3 -
```

然后执行第 8 章所有验证项。验证失败时必须进入 `S09R-DIAG`，不得在 S09 临时修改代码。

#### 完成后同步

- 所有 `NF1211-*` units `passes=true`，且每个有 `validated_at` 与 `evidence`。
- `S09.status=done`。
- progress 包含 `FINAL CLOSE-OUT PASS`。

---

## 8. 验证 Runbook

### V01 Build 验证

```powershell
.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) {
    throw 'V01 build failed; execute S07R-DIAG'
}

Assert-PathExists 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar'

jar tf 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar' | rg --fixed-strings -- 'META-INF/neoforge.mods.toml'
if ($LASTEXITCODE -ne 0) { throw 'jar missing neoforge.mods.toml' }

jar tf 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar' | rg --fixed-strings -- 'levanilla_tacztps.mixins.json'
if ($LASTEXITCODE -ne 0) { throw 'jar missing mixin json' }

jar tf 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar' | rg -- 'tacz-neoforge|leawind-third-person|architectury'
if ($LASTEXITCODE -eq 0) { throw 'Dependency jar bundled into compat jar' }
if ($LASTEXITCODE -gt 1) { throw 'jar scan failed' }

$env:UNIT_ID = 'NF1211-BUILD'
$env:EVIDENCE_TEXT = 'V01 build passed; jar metadata present; dependency jars not bundled'
# 执行 5.2 Unit pass 模板
```

### V02 Metadata / Config 验证

```powershell
.\gradlew.bat runClient
if ($LASTEXITCODE -ne 0) {
    throw 'V02 runClient failed; execute S09R-DIAG'
}

Assert-PathExists 'run/client/config/tac-leawindtps.toml'
rg --line-number --fixed-strings -- 'switch_first_person_aiming' 'run/client/config/tac-leawindtps.toml'
if ($LASTEXITCODE -ne 0) { throw 'config missing switch_first_person_aiming' }

rg --line-number --fixed-strings -- 'switch_first_person_scoping' 'run/client/config/tac-leawindtps.toml'
if ($LASTEXITCODE -ne 0) { throw 'config missing switch_first_person_scoping' }

& rg --line-number -- 'InvalidInjectionException|MixinApplyError|ClassNotFoundException|ModLoadingException' 'run/client/logs/latest.log'
if ($LASTEXITCODE -eq 0) { throw 'V02 log contains loading or Mixin error; execute S09R-DIAG' }
if ($LASTEXITCODE -gt 1) { throw 'V02 log scan failed' }

$env:UNIT_ID = 'NF1211-METADATA'
$env:EVIDENCE_TEXT = 'V02 runClient loaded mod and generated tac-leawindtps.toml'
# 执行 5.2 Unit pass 模板
```

### V03 Recoil 验证

mod 组合：

```text
NeoForge 21.1.145
TaCZ 1.1.7-hotfix-r5
Leawind 2.3.0
Architectury API 13.0.8
compat S04+ code
```

配置前提：

```text
Leawind enabled
TaCZ gun available
Third-person rendering active
```

操作步骤：

1. 进入单人世界。
2. 开启 Leawind 第三人称。
3. 手持 TaCZ 枪械。
4. 连续射击 10 发。
5. 停止射击，观察 recoil 回落。
6. 切回第一人称。
7. 射击 5 发。
8. 退出客户端。

验证命令：

```powershell
Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -like '*CameraSetupEvent*' } |
    Select-Object -First 1 |
    ForEach-Object { $_.FullName } |
    Tee-Object -Variable cameraMixinHit

if (-not $cameraMixinHit) { throw 'CameraSetupEvent mixin export missing; execute S09R-DIAG' }

& rg --line-number -- 'InvalidInjectionException|MixinApplyError|ClassNotFoundException' 'run/client/logs/latest.log'
if ($LASTEXITCODE -eq 0) { throw 'V03 log contains Mixin error; execute S09R-DIAG' }
if ($LASTEXITCODE -gt 1) { throw 'V03 log scan failed' }
```

Pass 判据：

- 第三人称 recoil 作用于 Leawind camera。
- 第三人称玩家实体朝向不被 recoil 直接扭动。
- 第一人称 TaCZ recoil 正常。
- `.mixin.out` 导出 `CameraSetupEvent`。
- 日志无 Mixin 错误。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-RECOIL'
$env:EVIDENCE_TEXT = 'V03 third-person recoil redirected to Leawind camera; first-person TaCZ recoil intact'
# 执行 5.2 Unit pass 模板
```

### V04 Crosshair 验证

配置前提：

```text
Leawind third-person crosshair enabled while aiming
Leawind third-person crosshair enabled while not aiming
```

操作步骤：

1. 进入单人世界。
2. 开启 Leawind 第三人称。
3. 手持 TaCZ 枪械，不瞄准。
4. 观察准星。
5. 按住瞄准键。
6. 观察准星。
7. 换弹。
8. 打开 TaCZ 改装界面。
9. 退出客户端。

验证命令：

```powershell
Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -like '*ShoulderSurfingCompat*' } |
    Select-Object -First 1 |
    ForEach-Object { $_.FullName } |
    Tee-Object -Variable shoulderMixinHit

if (-not $shoulderMixinHit) { throw 'ShoulderSurfingCompat mixin export missing; execute S09R-DIAG' }

& rg --line-number -- 'InvalidInjectionException|MixinApplyError|ClassNotFoundException' 'run/client/logs/latest.log'
if ($LASTEXITCODE -eq 0) { throw 'V04 log contains Mixin error; execute S09R-DIAG' }
if ($LASTEXITCODE -gt 1) { throw 'V04 log scan failed' }
```

Pass 判据：

- 第三人称不瞄准时，Leawind 允许显示则显示准星。
- 第三人称瞄准时，Leawind 允许显示则显示准星。
- 换弹隐藏逻辑不被破坏。
- 改装界面隐藏逻辑不被破坏。
- `.mixin.out` 导出 `ShoulderSurfingCompat`。
- 日志无 Mixin 错误。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-CROSSHAIR'
$env:EVIDENCE_TEXT = 'V04 crosshair passed; Leawind third-person crosshair state respected'
# 执行 5.2 Unit pass 模板
```

### V05 Aim Perspective 验证

测试 1 config：

```toml
switch_first_person_aiming = false
switch_first_person_scoping = true
```

操作与 pass 判据：

- 无 scope 枪械瞄准：不切第一人称过渡。
- 外置 scope 枪械瞄准：切第一人称过渡。
- 内置 scope 枪械瞄准：切第一人称过渡。

测试 2 config：

```toml
switch_first_person_aiming = true
switch_first_person_scoping = true
```

操作与 pass 判据：

- 无 scope 枪械瞄准：切第一人称过渡。

测试 3 config：

```toml
switch_first_person_aiming = false
switch_first_person_scoping = false
```

操作与 pass 判据：

- 有 scope 枪械瞄准：不切第一人称过渡。

验证命令：

```powershell
Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -like '*TickAnimationEvent*' } |
    Select-Object -First 1 |
    ForEach-Object { $_.FullName } |
    Tee-Object -Variable tickMixinHit

if (-not $tickMixinHit) { throw 'TickAnimationEvent mixin export missing; execute S09R-DIAG' }

& rg --line-number -- 'InvalidInjectionException|MixinApplyError|ambiguous|ClassNotFoundException' 'run/client/logs/latest.log'
if ($LASTEXITCODE -eq 0) { throw 'V05 log contains Mixin or ambiguous target error; execute S09R-DIAG' }
if ($LASTEXITCODE -gt 1) { throw 'V05 log scan failed' }
```

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-AIM-PERSPECTIVE'
$env:EVIDENCE_TEXT = 'V05 aim/scoping perspective passed across no-scope, external-scope, built-in-scope cases'
# 执行 5.2 Unit pass 模板
```

### V06 Draw Guard 验证

操作步骤：

1. 进入单人世界。
2. 枪械与非枪物品快速切换 30 次。
3. 第一人称与 Leawind 第三人称切换 10 次。
4. 持枪、收枪、拔枪、换枪各执行 5 次。
5. 退出客户端。

验证命令：

```powershell
Get-ChildItem -Path 'run/client/.mixin.out' -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -like '*LocalPlayerDraw*' } |
    Select-Object -First 1 |
    ForEach-Object { $_.FullName } |
    Tee-Object -Variable drawMixinHit

if (-not $drawMixinHit) { throw 'LocalPlayerDraw mixin export missing; execute S09R-DIAG' }

& rg --line-number -- 'NullPointerException|hitResult|InvalidInjectionException|MixinApplyError|ClassNotFoundException' 'run/client/logs/latest.log'
if ($LASTEXITCODE -eq 0) { throw 'V06 log contains NPE or Mixin error; execute S09R-DIAG' }
if ($LASTEXITCODE -gt 1) { throw 'V06 log scan failed' }

Assert-ExactMixinCameraRemoval
```

Pass 判据：

- 无崩溃。
- 日志无 hitResult NPE。
- `.mixin.out` 导出 `LocalPlayerDraw`。
- `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java` 不存在。
- `src/main/resources/levanilla_tacztps.mixins.json` 不包含精确条目 `"client.MixinCamera"`。

通过后写：

```powershell
$env:UNIT_ID = 'NF1211-DRAW-GUARD'
$env:EVIDENCE_TEXT = 'V06 draw guard passed; exact old reflection mixin removal verified'
# 执行 5.2 Unit pass 模板
```

### V07 Docs 验证

```powershell
rg --line-number --fixed-strings -- 'Minecraft: `1.21.1`' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing Minecraft target' }

rg --line-number --fixed-strings -- 'NeoForge: `21.1.145`' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing NeoForge target' }

rg --line-number --fixed-strings -- 'GPL-3.0-only' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing GPL target' }

rg --line-number --fixed-strings -- 'These jars are ignored by Git and must not be committed.' README.md
if ($LASTEXITCODE -ne 0) { throw 'README missing local jar rule' }

& rg --line-number -- '/Users/|C:\\|/home/' README.md
if ($LASTEXITCODE -eq 0) { throw 'README contains machine-private absolute path' }
if ($LASTEXITCODE -gt 1) { throw 'README path scan failed' }

$env:UNIT_ID = 'NF1211-DOCS'
$env:EVIDENCE_TEXT = 'V07 README target versions, GPL notice, local jar rules, and path hygiene passed'
# 执行 5.2 Unit pass 模板
```

### V08 Workflow 验证

```powershell
python3 -m json.tool .tacz-leawindtps-builder/feature_list.json | Out-Null
python3 -m json.tool .tacz-leawindtps-builder/sprint_plan.json | Out-Null

$script = @'
import json
from pathlib import Path

feature = json.loads(Path(".tacz-leawindtps-builder/feature_list.json").read_text(encoding="utf-8"))
sprint = json.loads(Path(".tacz-leawindtps-builder/sprint_plan.json").read_text(encoding="utf-8"))

required = {
    "NF1211-WORKFLOW",
    "NF1211-BUILD",
    "NF1211-METADATA",
    "NF1211-RECOIL",
    "NF1211-CROSSHAIR",
    "NF1211-AIM-PERSPECTIVE",
    "NF1211-DRAW-GUARD",
    "NF1211-DOCS",
}
units = {unit.get("id"): unit for unit in feature.get("units", [])}
missing = required - set(units)

bad = []
for unit in units.values():
    if unit.get("id") in required and unit.get("passes") and (not unit.get("validated_at") or not unit.get("evidence")):
        bad.append(unit.get("id"))

mig = sprint.get("activeSprint", {}).get("neoforge1211Migration")
if not mig:
    raise SystemExit("missing activeSprint.neoforge1211Migration")
if mig.get("executionBranch") != "codex/neoforge-1.21.1":
    raise SystemExit("wrong executionBranch")
if mig.get("targetReleaseBranch") != "neoforge-1.21.1":
    raise SystemExit("wrong targetReleaseBranch")
if missing:
    raise SystemExit("Missing NF1211 units: " + ", ".join(sorted(missing)))
if bad:
    raise SystemExit("passes=true without evidence: " + ", ".join(sorted(bad)))
'@
$script | python3 -

$env:UNIT_ID = 'NF1211-WORKFLOW'
$env:EVIDENCE_TEXT = 'V08 workflow passed; memory layer preserved and NF1211 units validated'
# 执行 5.2 Unit pass 模板
```

---

## 9. Remediation 流程

### S07R-DIAG

S07 build 失败后必须执行本诊断，按错误类型进入对应回修切片。

```powershell
& rg --line-number -- 'build.gradle|settings.gradle|gradle.properties|verifyLocalModJars|ModDevGradle' 'build/reports' 'run/client/logs' 2>$null
& rg --line-number -- 'neoforge.mods.toml|mods.toml|levanilla_tacztps.mixins.json|ModLoadingException' 'build' 'run/client/logs' 2>$null
& rg --line-number -- 'CameraSetupEvent|LeawindBridge|RecoilCallGuard|ModEventSubscriber' 'build' 'run/client/logs' 2>$null
& rg --line-number -- 'ShoulderSurfingCompat|showCrosshair' 'build' 'run/client/logs' 2>$null
& rg --line-number -- 'TickAnimationEvent|ClientTickEvent' 'build' 'run/client/logs' 2>$null
& rg --line-number -- 'LocalPlayerDraw|ItemStack|hitResult' 'build' 'run/client/logs' 2>$null
```

分派规则：

| 诊断命中 | 进入切片 |
|---|---|
| Gradle、dependency、local jar、verifyLocalModJars | `S07R-S02` |
| metadata、mods.toml、mixin json、entrypoint、config | `S07R-S03` |
| recoil bridge、CameraSetupEvent、LeawindBridge、event subscriber | `S07R-S04` |
| ShoulderSurfingCompat、showCrosshair | `S07R-S05` |
| TickAnimationEvent、ClientTickEvent、scope config | `S07R-S06` |
| LocalPlayerDraw、ItemStack、hitResult、旧反射 Mixin path | `S07R-S07` |

`S07R-S02` 到 `S07R-S07` 只能修改对应原切片的 allowed files。回修完成后必须重跑原切片门禁，然后回到 S07。

### S09R-DIAG

S09 runtime/gameplay 验证失败后必须执行本诊断，按失败验证项进入对应回修切片。

分派规则：

| 失败项 | 进入切片 |
|---|---|
| V02 metadata/config/loading | `S09R-S03` |
| V03 recoil | `S09R-S04` |
| V04 crosshair | `S09R-S05` |
| V05 aim/scoping perspective | `S09R-S06` |
| V06 draw guard | `S09R-S07` |
| V07 README/docs | `S09R-S08` |

`S09R-S03` 到 `S09R-S08` 只能修改对应原切片的 allowed files。回修完成后必须重新执行受影响验证项，并重新执行 V01 与 V08。

---

## 10. 仓库同步与 Close-out

### Close-out 必须执行

```powershell
.\gradlew.bat clean build
if ($LASTEXITCODE -ne 0) { throw 'Close-out build failed; execute S07R-DIAG' }

Invoke-GraphifyCloseout

python3 -m json.tool .tacz-leawindtps-builder/feature_list.json | Out-Null
python3 -m json.tool .tacz-leawindtps-builder/sprint_plan.json | Out-Null

Assert-RgAbsent 'net.minecraftforge' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'com.llamalad7.mixinextras' @('src/main','build.gradle','settings.gradle','gradle.properties')
Assert-RgAbsent 'requiredMods' @('src/main','src/main/resources')
Assert-ExactMixinCameraRemoval
Assert-PathAbsent 'src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java'

$trackedJars = git ls-files -- 'libs/*.jar'
if ($trackedJars) { throw "Local dependency jars are tracked by Git: $trackedJars" }

$jarPath = 'build/libs/levanilla_tacztps-3.0.0-mc1.21.1-neoforge.jar'
jar tf $jarPath | rg -- 'tacz-neoforge|leawind-third-person|architectury'
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
# 执行 5.2 切片完成模板
```

### 失败停止条件

- 任一验证项 fail。
- 任一日志出现 Mixin 错误。
- 任一 unit 未真实验证却被写为 `passes=true`。

### 完成后同步

- 所有 NF1211 units `implemented=true`。
- 所有 NF1211 units `passes=true`。
- `S09.status=done`。

---

## 11. 最终完成标准

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
