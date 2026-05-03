# TACZ-LeawindTPS-Compat NeoForge 1.21.1 移植开发计划

> 目标：在你已经 fork 并创建好的分支上，把 `TACZ-LeawindTPS-Compat` 从旧的 Forge 1.20.1 兼容层，迁移成面向 `MUKSC/TACZ-1.21.1` + `Leawind's Third Person` 的 NeoForge 1.21.1 客户端兼容 mod。

---

## 0. 总体判断

这次不是“小改版本号”，而是一次 **半重写式移植**。

旧项目真正值得保留的是兼容思路：

1. 拦截 TaCZ 原本直接改玩家视角的 recoil。
2. 在自己的事件里手动调用 TaCZ recoil。
3. 把 recoil 造成的 yaw/pitch 差值转交给 Leawind 第三人称相机。
4. 根据配置，在瞄准或开镜时让 Leawind 切入第一人称过渡状态。
5. 保留 `mc.hitResult == null` 的防崩兜底。

但下面这些内容应该重做：

- Gradle 工程；
- ForgeGradle 依赖；
- `mods.toml`；
- Forge imports；
- 事件注册；
- Mixin 方法签名；
- `MixinCamera` 反射兜底逻辑；
- README、版本声明、发布说明。

---

## 1. 目标版本矩阵

| 项目 | 目标 |
|---|---|
| Minecraft | `1.21.1` |
| Loader | `NeoForge 21.1.x` |
| Java | `21` |
| TaCZ | `MUKSC/TACZ-1.21.1`, `1.1.7-hotfix-r5` |
| Leawind's Third Person | `2.3.0`, NeoForge, MC `1.21–1.21.1` |
| Architectury API | `13.0.8` 起步 |
| modid | 保留 `levanilla_tacztps` |
| 分支 | `neoforge-1.21.1` |
| 推荐版本号 | `3.0.0-mc1.21.1-neoforge` |
| 许可证 | 继续 `GPL-3.0-only` 或 `GNU General Public License v3.0` |

NeoForge 版本建议先用 `21.1.145`，因为 Leawind 1.21 分支以这个版本作为 NeoForge 版本。若你的 MUKSC TaCZ jar 在 `21.1.145` 下有运行问题，再回退测试 `21.1.93`，因为 MUKSC 分支的版本目录里使用过 `21.1.93`。

---

## 2. 推荐 commit 拆分

不要一次性把所有内容塞进一个 commit。推荐按下面顺序提交，后续排错会简单很多。

```text
docs: add NeoForge 1.21.1 port plan
chore: migrate Gradle project to NeoForge 1.21.1
chore: replace Forge mod metadata with neoforge.mods.toml
chore: update config and mod entrypoint for NeoForge
feat: add LeawindBridge and RecoilCallGuard
feat: port recoil camera compatibility
feat: port TaCZ crosshair compatibility
feat: port aiming perspective switch
fix: port LocalPlayerDraw hitResult guard
chore: remove obsolete Camera reflection mixin
docs: update README for MUKSC TaCZ 1.21.1 port
```

每个 commit 后至少跑一次：

```bash
./gradlew clean build
```

关键功能 commit 后再跑：

```bash
./gradlew runClient
```

---

## 3. 分支准备

在你的 fork 本地仓库执行：

```bash
git status
git branch --show-current
```

确认当前分支是：

```text
neoforge-1.21.1
```

建议先做一个安全标签，方便回滚：

```bash
git tag before-neoforge-1.21.1-port
git push origin before-neoforge-1.21.1-port
```

清理旧构建产物：

```bash
rm -rf build run .gradle
```

Windows PowerShell：

```powershell
Remove-Item -Recurse -Force build, run, .gradle -ErrorAction SilentlyContinue
```

---

## 4. 第一阶段：Gradle 工程迁移

### 4.1 删除或重写旧 ForgeGradle 配置

旧工程的 `build.gradle` 使用：

```groovy
id 'net.minecraftforge.gradle'
id 'org.parchmentmc.librarian.forgegradle'
id 'org.spongepowered.mixin'
```

迁移后应改为 NeoForge 的 ModDevGradle。

### 4.2 `settings.gradle`

建议替换为：

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri('https://maven.neoforged.net/releases') }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}

rootProject.name = 'TACZ-LeawindTPS-Compat'
```

### 4.3 `gradle.properties`

建议重写成下面结构：

```properties
org.gradle.jvmargs=-Xmx4G
org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.caching=true

minecraft_version=1.21.1
minecraft_version_range=[1.21.1,1.21.2)
neo_version=21.1.145
neo_version_range=[21.1,)
loader_version_range=[1,)

parchment_minecraft_version=1.21.1
parchment_mappings_version=2024.11.17

mod_id=levanilla_tacztps
mod_name=TaCZ: Leawind TPS Compat
mod_license=GPL-3.0-only
mod_version=3.0.0-mc1.21.1-neoforge
mod_group_id=com.levanilla
mod_authors=levanilla, airoucat
mod_description=Third person shooting compatibility for MUKSC TaCZ 1.21.1 NeoForge and Leawind's Third Person.

# Runtime/compile dependency versions
# TaCZ 目前建议本地 libs jar，不要依赖旧 Modrinth artifact。
tacz_file=tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar
leawind_tps_version=2.3.0
architectury_version=13.0.8+neoforge
mixinextras_version=0.5.4
```

`mod_authors` 里是否写你的名字取决于你是否要公开发布。保留原作者，同时加上你自己的名字是比较稳妥的做法。

### 4.4 `build.gradle`

建议用 Groovy DSL，便于在旧项目基础上迁移。

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

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

sourceSets.main.resources {
    srcDir 'src/generated/resources'
}

repositories {
    mavenCentral()

    maven {
        name = 'Modrinth'
        url = 'https://api.modrinth.com/maven'
        content { includeGroup 'maven.modrinth' }
    }

    maven {
        name = 'Architectury'
        url = 'https://maven.architectury.dev'
        content { includeGroup 'dev.architectury' }
    }

    flatDir {
        dirs 'libs'
    }
}

neoForge {
    version = project.neo_version

    parchment {
        mappingsVersion = project.parchment_mappings_version
        minecraftVersion = project.parchment_minecraft_version
    }

    runs {
        client {
            client()
            gameDirectory = project.file('run/client')
            systemProperty 'forge.logging.console.level', 'debug'
            systemProperty 'mixin.debug.export', 'true'
            systemProperty 'mixin.debug.verbose', 'true'
        }

        server {
            server()
            gameDirectory = project.file('run/server')
            programArgument '--nogui'
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

configurations {
    localRuntime
    runtimeClasspath.extendsFrom localRuntime
}

dependencies {
    def taczJar = file("libs/${project.tacz_file}")

    // TaCZ: 当前目标是 MUKSC 的 NeoForge 1.21.1 移植版。
    // 不要 jarJar，不要把 TaCZ 打进你的兼容 mod。
    compileOnly files(taczJar)
    localRuntime files(taczJar)

    // Leawind 和 Architectury 用于编译与本地 runClient。
    // 发布时仍要求用户单独安装这些依赖，不要打包进 compat jar。
    compileOnly "maven.modrinth:leawind-third-person:${project.leawind_tps_version}"
    localRuntime "maven.modrinth:leawind-third-person:${project.leawind_tps_version}"

    compileOnly "maven.modrinth:architectury-api:${project.architectury_version}"
    localRuntime "maven.modrinth:architectury-api:${project.architectury_version}"

    // MixinExtras 用于 @ModifyExpressionValue。
    compileOnly "io.github.llamalad7:mixinextras-common:${project.mixinextras_version}"
    jarJar(implementation("io.github.llamalad7:mixinextras-neoforge:${project.mixinextras_version}")) {
        version {
            strictly "[${project.mixinextras_version},)"
            prefer project.mixinextras_version
        }
    }
}

var generateModMetadata = tasks.register('generateModMetadata', ProcessResources) {
    var replaceProperties = [
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
            mod_description        : mod_description,
    ]

    inputs.properties replaceProperties
    expand replaceProperties
    from 'src/main/templates'
    into 'build/generated/sources/modMetadata'
}

sourceSets.main.resources.srcDir generateModMetadata
neoForge.ideSyncTask generateModMetadata

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release.set(21)
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

### 4.5 本地依赖目录

创建：

```text
libs/
```

放入：

```text
libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar
```

`.gitignore` 建议加：

```gitignore
/libs/*.jar
!/libs/.gitkeep
```

并提交一个空文件：

```bash
mkdir -p libs
touch libs/.gitkeep
git add libs/.gitkeep
```

原因：不要把 TaCZ jar 直接提交进你的 GPL 兼容 mod 仓库；让开发者或用户自己安装依赖。

---

## 5. 第二阶段：mod metadata 迁移

### 5.1 删除旧文件

删除：

```text
src/main/resources/META-INF/mods.toml
```

### 5.2 新建模板目录

创建：

```text
src/main/templates/META-INF/neoforge.mods.toml
```

内容：

```toml
modLoader="javafml"
loaderVersion="${loader_version_range}"
license="${mod_license}"
issueTrackerURL="https://github.com/YOUR_NAME/TACZ-LeawindTPS-Compat/issues"

[[mods]]
modId="${mod_id}"
version="${mod_version}"
displayName="${mod_name}"
authors="${mod_authors}"
description='''${mod_description}'''

[[mixins]]
config="${mod_id}.mixins.json"
requiredMods=["tacz", "leawind_third_person"]

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
versionRange="[2.3.0,)"
ordering="AFTER"
side="CLIENT"

[[dependencies.${mod_id}]]
modId="architectury"
type="required"
ordering="AFTER"
side="CLIENT"
```

注意：`requiredMods` 只控制 Mixin 配置在依赖存在时应用。`dependencies` 控制加载器层面的依赖检查。两者都写，启动报错会更清楚。

### 5.3 更新 Mixin JSON

替换：

```text
src/main/resources/levanilla_tacztps.mixins.json
```

内容：

```json
{
  "required": true,
  "minVersion": "0.8",
  "minMixinExtrasVersion": "0.5.4",
  "package": "com.levanilla.TacZtps.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [],
  "client": [
    "client.MixinCameraSetupEvent",
    "client.MixinRenderCrosshairEvent",
    "client.MixinTickAnimationEvent",
    "client.MixinLocalPlayerDraw"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

从列表里删除：

```text
client.MixinCamera
```

理由：1.21.1 的方案不再用反射搜索 recoil 方法。反射兜底不稳定，而且和 TaCZ 1.21.1 已存在的 `CameraSetupEvent.applyCameraRecoil` 事件链相比，没有维护优势。

---

## 6. 第三阶段：主类和配置

### 6.1 `ThirdPersonTacz.java`

替换为：

```java
package com.levanilla.TacZtps;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(value = ThirdPersonTacz.MOD_ID, dist = Dist.CLIENT)
public final class ThirdPersonTacz {
    public static final String MOD_ID = "levanilla_tacztps";

    public ThirdPersonTacz(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "tac-leawindtps.toml");
    }
}
```

要点：

- 不再使用 `MinecraftForge.EVENT_BUS.register(this)`。
- 不再使用 `ModLoadingContext.get()`。
- 这是纯客户端兼容 mod，所以 `@Mod(..., dist = Dist.CLIENT)`。

### 6.2 `ClientConfig.java`

替换 Forge import：

```java
package com.levanilla.TacZtps;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue SWITCH_FIRST_PERSON_AIMING;
    public static final ModConfigSpec.BooleanValue SWITCH_FIRST_PERSON_SCOPING;

    static {
        BUILDER.push("Configure how perspective is handled while using TaCZ guns");

        SWITCH_FIRST_PERSON_AIMING = BUILDER
                .comment(
                        "If enabled, holding the aim button will switch Leawind TPS into first-person transition.",
                        "This option overrides switch_first_person_scoping.",
                        "This is mainly intended for hold-to-aim mode.",
                        "Default: false"
                )
                .define("switch_first_person_aiming", false);

        SWITCH_FIRST_PERSON_SCOPING = BUILDER
                .comment(
                        "If enabled, holding the aim button on a gun with a scope attachment will switch Leawind TPS into first-person transition.",
                        "This is mainly intended for hold-to-aim mode.",
                        "Default: true"
                )
                .define("switch_first_person_scoping", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ClientConfig() {}
}
```

---

## 7. 第四阶段：兼容桥接层

创建目录：

```text
src/main/java/com/levanilla/TacZtps/compat/
```

### 7.1 `LeawindBridge.java`

```java
package com.levanilla.TacZtps.compat;

import com.github.leawind.thirdperson.ThirdPerson;
import com.github.leawind.thirdperson.ThirdPersonStatus;

public final class LeawindBridge {
    private LeawindBridge() {}

    public static boolean isThirdPersonRendering() {
        return ThirdPerson.isAvailable() && ThirdPersonStatus.isRenderingInThirdPerson();
    }

    public static boolean shouldRenderCrosshair() {
        return ThirdPersonStatus.shouldRenderThirdPersonCrosshair();
    }

    public static void turnCamera(float dYRot, float dXRot) {
        if (ThirdPerson.CAMERA_AGENT != null) {
            ThirdPerson.CAMERA_AGENT.turnCamera(dYRot, dXRot);
        }
    }

    public static void invertPerspective() {
        ThirdPersonStatus.isPerspectiveInverted = true;
    }
}
```

目的：不要让每个 Mixin 直接散落调用 Leawind API。以后 Leawind 改包名或字段时，只需要改这一层。

### 7.2 `RecoilCallGuard.java`

```java
package com.levanilla.TacZtps.compat;

public final class RecoilCallGuard {
    private static final ThreadLocal<Boolean> MANUAL_CALL =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private RecoilCallGuard() {}

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

目的：防止你手动调用 `CameraSetupEvent.applyCameraRecoil(event)` 时，又被自己的 `MixinCameraSetupEvent` 取消。

---

## 8. 第五阶段：Recoil 兼容实装

### 8.1 `MixinCameraSetupEvent.java`

替换为：

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
    private static void levanilla$cancelNativeRecoilForLeawind(
            ViewportEvent.ComputeCameraAngles event,
            CallbackInfo ci
    ) {
        if (!RecoilCallGuard.isManualCall() && LeawindBridge.isThirdPersonRendering()) {
            ci.cancel();
        }
    }
}
```

旧代码里用过 `event.getPhase() == EventPriority.NORMAL`。1.21.1 这里不建议继续依赖 event phase，直接通过 `RecoilCallGuard` 区分 TaCZ 原生事件调用和我们自己的手动调用。

### 8.2 `ModEventSubscriber.java`

替换为：

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
    private ModEventSubscriber() {}

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

预期效果：

- TaCZ 自己的 recoil 不再直接扭玩家视角。
- compat mod 在低优先级事件中手动运行 recoil 计算。
- 计算出来的差值转给 Leawind 相机。
- 玩家实体旋转被恢复，不会出现第三人称下身体/镜头混乱的问题。

### 8.3 Recoil 阶段验收

运行：

```bash
./gradlew runClient
```

测试：

1. 开新世界。
2. 开启 Leawind 第三人称。
3. 手持 TaCZ 枪械。
4. 连续射击。
5. 观察 recoil 是否作用在第三人称相机上。
6. 观察玩家身体朝向是否异常抽动。
7. 切回第一人称，确认 TaCZ 原本 recoil 仍可用。

失败排查：

| 现象 | 优先检查 |
|---|---|
| 完全没有 recoil | `MixinCameraSetupEvent` 是否命中；`ModEventSubscriber` 是否触发 |
| 第三人称和玩家身体都动 | Mixin 取消原生 recoil 失败 |
| 启动崩溃 InvalidInjectionException | 方法 descriptor 写错，检查导出的 `CameraSetupEvent` |
| 连续射击后角度反向 | `turnCamera(dYRot, dXRot)` 参数顺序或符号 |

---

## 9. 第六阶段：准星兼容实装

TaCZ 1.21.1 的准星逻辑已经内置 ShoulderSurfing 判断。推荐把 Leawind 的“第三人称准星显示”合并到 TaCZ 现有的 `ShoulderSurfingCompat.showCrosshair()` 分支，而不是继续硬改 `CameraType#isFirstPerson()`。

### 9.1 `MixinRenderCrosshairEvent.java`

替换为：

```java
package com.levanilla.TacZtps.mixin.client;

import com.levanilla.TacZtps.compat.LeawindBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.client.event.RenderCrosshairEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RenderCrosshairEvent.class, remap = false)
public final class MixinRenderCrosshairEvent {
    @ModifyExpressionValue(
            method = {
                    "renderCrosshair",
                    "lambda$onRenderCrosshair$0"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/compat/shouldersurfing/ShoulderSurfingCompat;showCrosshair()Z",
                    remap = false
            ),
            require = 0
    )
    private static boolean levanilla$forceCrosshairLikeShoulderSurfing(boolean original) {
        return original || LeawindBridge.shouldRenderCrosshair();
    }
}
```

### 9.2 Lambda 名称确认

这里最大风险是：`lambda$onRenderCrosshair$0` 可能因编译器、源码变动而变化。

你已经在 `build.gradle` 里加了：

```groovy
systemProperty 'mixin.debug.export', 'true'
systemProperty 'mixin.debug.verbose', 'true'
```

启动后检查：

```text
run/client/.mixin.out/
```

找到导出的：

```text
com/tacz/guns/client/event/RenderCrosshairEvent.class
```

如果 `lambda$onRenderCrosshair$0` 不存在，就用 IDEA 或反编译工具确认真实名称，并替换 Mixin 里的方法名。

### 9.3 准星阶段验收

测试矩阵：

| 状态 | 预期 |
|---|---|
| 第一人称，不持枪 | 原版/Leawind 不受影响 |
| 第一人称，持 TaCZ 枪 | TaCZ 准星正常 |
| Leawind 第三人称，持枪不瞄准 | 按 Leawind 配置显示或隐藏准星 |
| Leawind 第三人称，瞄准进度 > 0.9 | 若 Leawind 配置要求显示准星，TaCZ 不应隐藏它 |
| 换弹中 | TaCZ 换弹隐藏准星逻辑仍生效 |
| 打开改装界面 | TaCZ 改装界面隐藏准星逻辑仍生效 |

失败排查：

| 现象 | 可能原因 |
|---|---|
| 不瞄准有准星，瞄准后准星消失 | lambda 目标没命中 |
| 第三人称始终没准星 | `renderCrosshair` 里的 target 没命中 |
| 启动崩溃 | MixinExtras 未加载，或 target descriptor 错误 |
| 准星在换弹时仍显示 | Mixin 改得太宽，绕过了 TaCZ 原本 return 条件 |

---

## 10. 第七阶段：瞄准/开镜切第一人称

### 10.1 `MixinTickAnimationEvent.java`

替换为：

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
    private static void levanilla$switchFirstPersonWhenAiming(
            ClientTickEvent.Pre event,
            CallbackInfo ci
    ) {
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
        if (operator == null || !operator.isAim()) {
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

### 10.2 为什么必须写完整 descriptor

TaCZ 1.21.1 里有两个同名 `tickAnimation`：

```java
tickAnimation(ClientTickEvent.Pre event)
tickAnimation(RenderFrameEvent.Post event)
```

所以 Mixin 目标必须写成：

```text
tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V
```

否则可能打到错误重载，或者直接 ambiguous target。

### 10.3 瞄准阶段验收

默认配置：

```toml
switch_first_person_aiming = false
switch_first_person_scoping = true
```

测试：

| 场景 | 预期 |
|---|---|
| 无 scope，普通瞄准 | 不切第一人称 |
| 有 scope，瞄准 | 切入 Leawind 第一人称过渡 |
| `switch_first_person_aiming=true` | 所有瞄准都切第一人称 |
| `switch_first_person_scoping=false` 且 aiming=false | scope 瞄准也不切 |
| 松开瞄准键 | Leawind 状态恢复 |

失败排查：

| 现象 | 检查 |
|---|---|
| 配置文件没生成 | `ThirdPersonTacz` 是否用 `container.registerConfig` |
| 有 scope 但不切 | `getBuiltInAttachmentId` 是否被调用；scope 是否为内置瞄具 |
| 所有枪都切 | `switch_first_person_aiming` 配置是否为 true |
| 启动报 ambiguous target | Mixin descriptor 没写完整 |

---

## 11. 第八阶段：`LocalPlayerDraw` 防崩迁移

### 11.1 `MixinLocalPlayerDraw.java`

替换为：

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

### 11.2 验收

测试：

1. 快速切换枪和非枪物品。
2. 在 Leawind 第三人称和第一人称间切换。
3. 快速进出世界。
4. 持枪、收枪、拔枪、换枪。
5. 观察是否有 `mc.hitResult` 相关 NPE。

---

## 12. 第九阶段：删除旧反射 Mixin

删除文件：

```text
src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java
```

确认 Mixin JSON 中也已经删除：

```text
client.MixinCamera
```

不要保留备用。保留会造成两个问题：

1. 与新的 recoil pipeline 重复施加 recoil。
2. 反射搜索方法名包含 `recoil` 的逻辑对 1.21.1 太脆。

---

## 13. 第十阶段：README 更新

README 建议结构：

```md
# TaCZ: Leawind TPS Compat - NeoForge 1.21.1 Fork

This branch ports the original Forge 1.20.1 compatibility mod to NeoForge 1.21.1.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21
- MUKSC/TACZ-1.21.1, 1.1.7-hotfix-r5
- Leawind's Third Person 2.3.0+
- Architectury API 13.0.8+

## Important

This fork targets the unofficial MUKSC TaCZ 1.21.1 NeoForge port.
Do not report compatibility issues from this fork to the original TaCZ developers or the original compat mod author unless they explicitly accept this branch.

## Features

- Redirects TaCZ camera recoil to Leawind's third-person camera.
- Keeps TaCZ crosshair visible in Leawind third person when Leawind config allows it.
- Optionally switches to Leawind first-person transition while aiming or scoping.
- Adds a guard for null `Minecraft#hitResult` during TaCZ draw logic.

## Config

Config file:

```text
.minecraft/config/tac-leawindtps.toml
```

Options:

```toml
switch_first_person_aiming = false
switch_first_person_scoping = true
```

## License

Forked from `leva-nilla/TACZ-LeawindTPS-Compat`.
The original project is licensed under GNU General Public License v3.0.
This fork remains licensed under GPL-3.0.
```

---

## 14. 总测试矩阵

### 14.1 启动/加载测试

| 编号 | 测试 | 通过标准 |
|---|---|---|
| A1 | `./gradlew clean build` | BUILD SUCCESSFUL |
| A2 | `./gradlew runClient` | 客户端进入主菜单 |
| A3 | 只装 NeoForge + TaCZ + Leawind + Architectury + compat | 无缺依赖报错 |
| A4 | 打开 Mods 页面 | `levanilla_tacztps` 显示版本与 GPL 许可证 |
| A5 | 创建新世界 | 不崩溃 |

### 14.2 Recoil 测试

| 编号 | 场景 | 通过标准 |
|---|---|---|
| R1 | 第一人称开枪 | TaCZ recoil 正常 |
| R2 | Leawind 第三人称开枪 | recoil 作用在 Leawind 相机 |
| R3 | Leawind 第三人称连射 | 相机连续 recoil，无玩家身体异常抖动 |
| R4 | 停止射击 | recoil 回落正常 |
| R5 | 切换视角后开枪 | 不出现叠加 recoil |

### 14.3 准星测试

| 编号 | 场景 | 通过标准 |
|---|---|---|
| C1 | 第三人称持枪不瞄准 | 按 Leawind 配置显示/隐藏 |
| C2 | 第三人称瞄准进度 > 0.9 | Leawind 允许时不被 TaCZ 隐藏 |
| C3 | 换弹 | TaCZ 仍能隐藏准星 |
| C4 | 改装界面 | TaCZ 仍能隐藏准星 |
| C5 | 第一人称持枪 | TaCZ 准星逻辑不受破坏 |

### 14.4 瞄准切视角测试

| 编号 | 配置 | 场景 | 通过标准 |
|---|---|---|---|
| P1 | aiming=false, scoping=true | 无 scope 瞄准 | 不切第一人称 |
| P2 | aiming=false, scoping=true | 有 scope 瞄准 | 切第一人称 |
| P3 | aiming=true, scoping=true | 任意枪瞄准 | 切第一人称 |
| P4 | aiming=false, scoping=false | 任意枪瞄准 | 不切第一人称 |
| P5 | 任意 | 松开瞄准 | Leawind 视角恢复 |

### 14.5 稳定性测试

| 编号 | 场景 | 通过标准 |
|---|---|---|
| S1 | 快速切枪 | 无崩溃 |
| S2 | 快速切维度/重进世界 | 无崩溃 |
| S3 | 重载资源包 | 无 Mixin 或 classloading 错误 |
| S4 | 开启/关闭 Leawind 配置菜单 | 无状态卡死 |
| S5 | 单人世界退出再进入 | 配置仍生效 |

---

## 15. 常见错误与处理

### 15.1 `ClassNotFoundException: com.tacz...`

原因：TaCZ jar 没放到 `libs/`，或者 `tacz_file` 文件名不一致。

处理：

```bash
ls libs
```

确认文件名与：

```properties
tacz_file=...
```

一致。

### 15.2 `ClassNotFoundException: dev.architectury...`

原因：Leawind 需要 Architectury API，但本地运行 classpath 没加。

处理：确认 `localRuntime "maven.modrinth:architectury-api:${architectury_version}"` 存在，或者把 Architectury API jar 放入实际客户端 mods 文件夹。

### 15.3 `InvalidInjectionException` 指向 `TickAnimationEvent`

原因：`tickAnimation` 有重载，Mixin 方法没有写完整 descriptor。

处理：确认是：

```text
tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V
```

### 15.4 准星瞄准时仍消失

原因：`lambda$onRenderCrosshair$0` 没命中。

处理：

1. 开启 `mixin.debug.export`。
2. 找导出的 `RenderCrosshairEvent`。
3. 查真实 lambda 名。
4. 修改 `MixinRenderCrosshairEvent` 的 `method` 列表。

### 15.5 第三人称开枪没有 recoil

优先确认：

1. `MixinCameraSetupEvent` 是否命中。
2. `ModEventSubscriber.onCameraRotateThirdPerson` 是否触发。
3. `LeawindBridge.isThirdPersonRendering()` 是否返回 true。
4. `CameraSetupEvent.applyCameraRecoil(event)` 是否真的改变了 `player.getXRot/YRot()`。

可以临时加日志：

```java
System.out.println("TaCZ recoil delta x=" + dXRot + ", y=" + dYRot);
```

确认后删除。

### 15.6 recoil 方向反了

在 `LeawindBridge.turnCamera(dYRot, dXRot)` 附近试验符号：

```java
LeawindBridge.turnCamera(dYRot, dXRot);
// 或
LeawindBridge.turnCamera(dYRot, -dXRot);
// 或
LeawindBridge.turnCamera(-dYRot, dXRot);
```

不要一开始就改 TaCZ recoil 源码。只在 compat 层调差值方向。

---

## 16. 发布前检查清单

### 16.1 代码检查

- [ ] 没有 `net.minecraftforge.*` import。
- [ ] 没有 `mods.toml`。
- [ ] 使用 `META-INF/neoforge.mods.toml`。
- [ ] `compatibilityLevel` 是 `JAVA_21`。
- [ ] `MixinCamera` 已删除。
- [ ] `MixinTickAnimationEvent` 使用完整 descriptor。
- [ ] `MixinLocalPlayerDraw` 使用 `draw(ItemStack)` descriptor。
- [ ] `MixinRenderCrosshairEvent` 两个 target 都已通过 runtime 测试。
- [ ] `ThirdPersonTacz.MOD_ID` 与 `gradle.properties mod_id` 一致。

### 16.2 构建检查

- [ ] `./gradlew clean build` 成功。
- [ ] `build/libs/` 下生成目标 jar。
- [ ] jar 内包含 `META-INF/neoforge.mods.toml`。
- [ ] jar 内包含 `levanilla_tacztps.mixins.json`。
- [ ] jar 内不包含 TaCZ jar。
- [ ] jar 内不包含 Leawind jar。
- [ ] jar 内不包含 Architectury jar。

### 16.3 许可证检查

- [ ] 保留 `LICENSE`。
- [ ] README 写明 fork 来源。
- [ ] README 写明 GPLv3。
- [ ] 发布 jar 时提供源码链接。
- [ ] 不添加“禁止二次分发”“禁止修改”“禁止商用”等与 GPL 冲突的额外限制。

### 16.4 发布说明检查

发布标题建议：

```text
TaCZ: Leawind TPS Compat 3.0.0 for NeoForge 1.21.1
```

发布说明建议：

```md
## Target

- Minecraft 1.21.1
- NeoForge 21.1.x
- MUKSC/TACZ-1.21.1 1.1.7-hotfix-r5
- Leawind's Third Person 2.3.0
- Architectury API 13.0.8+

## Notes

This is an unofficial port targeting the unofficial MUKSC TaCZ 1.21.1 NeoForge port.
Do not report issues from this compatibility fork to the original TaCZ developers.

## Changes

- Ported project from ForgeGradle 1.20.1 to NeoForge 1.21.1.
- Redirected TaCZ camera recoil to Leawind's third-person camera.
- Reworked TaCZ crosshair compatibility for the 1.21.1 RenderCrosshairEvent flow.
- Reworked aiming/scoping perspective switch for the 1.21.1 TickAnimationEvent overloads.
- Updated LocalPlayerDraw null hitResult guard for draw(ItemStack).
```

---

## 17. 开发原则

1. **不要改 TaCZ 源码。** 兼容层应该只通过事件、Mixin 和桥接类解决问题。
2. **不要改 Leawind 源码。** Leawind 逻辑通过 `LeawindBridge` 调用。
3. **不要重新引入 `MixinCamera` 反射兜底。** 这是旧版本临时方案，不适合 1.21.1。
4. **不要把 TaCZ、Leawind、Architectury 打包进兼容 mod。** 这些应该是外部依赖。
5. **Mixin target 必须尽量窄。** 能改 `ShoulderSurfingCompat.showCrosshair()` 就不要改整个 `CameraType.isFirstPerson()`。
6. **先实现 recoil，再实现准星，再实现切视角。** 这三个功能相互影响，但调试优先级不同。
7. **每个阶段都要能单独通过构建。** 不要累积十几个错误一起查。

---

## 18. 最终完成标准

这个分支可以认为完成，当且仅当：

- `./gradlew clean build` 成功；
- `./gradlew runClient` 能进入新世界；
- TaCZ + Leawind + Architectury + compat 同时加载无 Mixin 错误；
- Leawind 第三人称下 TaCZ 开枪 recoil 正常；
- TaCZ 第一人称 recoil 不被破坏；
- 第三人称持枪准星按 Leawind 配置显示；
- 瞄准/开镜切第一人称配置生效；
- 快速切枪不因 `mc.hitResult == null` 崩溃；
- README 明确写明目标版本、非官方性质、GPLv3、源码链接；
- 发布 jar 不捆绑 TaCZ/Leawind/Architectury。

