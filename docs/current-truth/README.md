# TACZ-LeawindTPS-Compat Current Truth

本目录是当前仓库的 shared current truth 根目录。

## 一句话定义

`TACZ-LeawindTPS-Compat` 是一个面向 `Minecraft 1.20.1 + Forge 47.x` 的客户端兼容模组，目标是在 `Timeless and Classics Zero` 与 `Leawind's Third Person` 之间维持更顺手的第三人称射击体验。

## 当前 source map

- 模组引导与 config 注册：
  - `src/main/java/com/levanilla/TacZtps/ThirdPersonTacz.java`
  - `src/main/java/com/levanilla/TacZtps/ClientConfig.java`
- 事件总线侧挂接：
  - `src/main/java/com/levanilla/TacZtps/ModEventSubscriber.java`
- client mixin 兼容层：
  - `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java`
  - `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCameraSetupEvent.java`
  - `src/main/java/com/levanilla/TacZtps/mixin/client/MixinLocalPlayerDraw.java`
  - `src/main/java/com/levanilla/TacZtps/mixin/client/MixinRenderCrosshairEvent.java`
  - `src/main/java/com/levanilla/TacZtps/mixin/client/MixinTickAnimationEvent.java`
- mixin 配置：
  - `src/main/resources/levanilla_tacztps.mixins.json`

## 默认阅读顺序

1. `docs/current-truth/README.md`
2. `docs/current-truth/2026-05-03-baseline-requirements.md`
3. `docs/harness/tacz-leawindtps-compat-builder.md`
4. `.tacz-leawindtps-builder/spec.md`
5. `.tacz-leawindtps-builder/feature_list.json`
6. `.tacz-leawindtps-builder/sprint_plan.json`
7. `.tacz-leawindtps-builder/progress.md`

## 当前已知边界

- 依赖组合固定围绕：
  - `Forge 47.3.5`
  - `Minecraft 1.20.1`
  - `Timeless and Classics Zero`
  - `Leawind's Third Person`
- 当前代码主要是 client 侧兼容逻辑，不应在没有明确需求和验证入口时随意扩成新的通用玩法层。
- 当前 shared docs 还处于 bootstrap 阶段；后续若补更细的兼容 contract，应继续落在 `docs/current-truth/` 下，而不是散写在提交描述里。
