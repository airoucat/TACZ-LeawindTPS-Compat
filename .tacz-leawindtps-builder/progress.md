# TACZ-LeawindTPS-Compat Builder Progress

## 2026-05-03

- `start`
  - 目标：把 `AI Soldier` 当前使用的 `harness + ce + graphify` workflow 骨架迁到 `TACZ-LeawindTPS-Compat`
  - 现状：仓库只有 Forge 模组源码与 Gradle 基础文件，还没有 `AGENTS.md`、`docs/harness/`、memory layer、`.githooks/` 或 graphify setup 脚本
  - 策略：保留 workflow 骨架，但把 current truth、memory layer 命名、验证入口和项目语义改成当前仓库自己的版本

- `complete`
  - 已新增：`AGENTS.md`、`AGENTS.win.md`、`AGENTS.mac.md`、`docs/current-truth/`、`docs/harness/`、`.tacz-leawindtps-builder/`、`.githooks/`、`scripts/dev/setup_graphify_local.py`
  - 已验证：
    - `python scripts/dev/setup_graphify_local.py`
    - `python scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`
    - `git config --local --get core.hooksPath` 返回 `.githooks`
    - `graphify-out/GRAPH_REPORT.md` 已生成
    - `.codex/hooks.json` 已生成
  - 结果：`WF0` 完成，后续会话可按 repo-local harness workflow 继续推进
- EXECUTION_BRANCH codex/neoforge-1.21.1 TARGET_RELEASE_BRANCH neoforge-1.21.1 2026-05-04T00:57:41.2015982+08:00

- START S00 2026-05-04T00:58:17.7851398+08:00

## NeoForge 1.21.1 Migration Append
- Existing memory layer preserved. WF0 / INV1 / VER1 history remains untouched.
- Execution branch recorded as codex/neoforge-1.21.1.
- Target release branch recorded as neoforge-1.21.1.
- Added or merged NF1211 units under feature_list.json units.
- Added or merged activeSprint.neoforge1211Migration slices.

- DONE S00 2026-05-04T00:58:18.1459757+08:00

- START S01 2026-05-04T00:58:40.9995048+08:00

## Evidence Lock Summary - NeoForge 1.21.1

- Execution shell locked to Windows PowerShell.
- Gradle command locked to `\.\gradlew.bat`.
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

- DONE S01 2026-05-04T00:58:41.3919020+08:00

- START S02 2026-05-04T00:59:32.4672354+08:00

- BLOCKED S02 B-DEP-001 2026-05-04T00:59:32.6877038+08:00 missing local dependency jars: libs/tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar, libs/leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar, libs/architectury-api-13.0.8-neoforge.jar
- CORRECTION S01 Gradle command locked to `.\gradlew.bat`; previous escaped display line was a transcription artifact.

- RETRY S02 2026-05-04T01:07:03.9562523+08:00 dependencies downloaded; resuming S02 gate

Algorithm : SHA256
Hash      : DFB5B8DA5A5E38839668037B347E419A65E75D74DC4C7753B75A943616955C4B
Path      : C:\Users\xuany\Documents\TACZ-LeawindTPS-Compat\libs\tacz-neoforge-1.21.1-1.1.7-hotfix-r5.jar



Algorithm : SHA256
Hash      : 9B931BA49A6FB9E89ABD261494B4FD5521AC16369AA56FED72CA6EEDA4409011
Path      : C:\Users\xuany\Documents\TACZ-LeawindTPS-Compat\libs\leawind-third-person-2.3.0-mc1.21-1.21.1-neoforge.jar



Algorithm : SHA256
Hash      : 2EB06668281BE9C57ED6BA8B2CA39B155567063C3096AA94A1C2140694F787DF
Path      : C:\Users\xuany\Documents\TACZ-LeawindTPS-Compat\libs\architectury-api-13.0.8-neoforge.jar


- DESCRIPTOR PASS S02 CameraSetupEvent.applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V applyCameraRecoil(Lnet/neoforged/neoforge/client/event/ViewportEvent$ComputeCameraAngles;)V
- DESCRIPTOR PASS S02 ShoulderSurfingCompat.showCrosshair()Z showCrosshair()Z
- DESCRIPTOR PASS S02 TickAnimationEvent.tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V tickAnimation(Lnet/neoforged/neoforge/client/event/ClientTickEvent$Pre;)V
- DESCRIPTOR PASS S02 LocalPlayerDraw.draw(Lnet/minecraft/world/item/ItemStack;)V draw(Lnet/minecraft/world/item/ItemStack;)V
- DESCRIPTOR PASS S02 CameraAgent.turnCamera(DD)V turnCamera(DD)V

- RETRY S02 2026-05-04T01:13:29.1963676+08:00 moved additionalRuntimeClasspath after neoForge block

- DONE S02 2026-05-04T01:13:35.1785952+08:00

- START S03 2026-05-04T01:15:30.4410754+08:00

- DONE S03 2026-05-04T01:15:36.1246557+08:00

- START S04 2026-05-04T01:17:11.8545631+08:00

- DONE S04 2026-05-04T01:17:17.0510968+08:00

- START S05 2026-05-04T01:18:07.2371313+08:00

- DONE S05 2026-05-04T01:18:12.4251959+08:00

- START S06 2026-05-04T01:19:13.1351223+08:00

- DONE S06 2026-05-04T01:19:18.7178007+08:00

- START S07 2026-05-04T01:20:39.6570288+08:00

- DONE S07 2026-05-04T01:27:28.7229742+08:00

- START S08 2026-05-04T01:28:25.2408881+08:00

- DONE S08 2026-05-04T01:28:25.8229134+08:00

- PREP S09 manual instance 2026-05-04T01:28:00+08:00 copied compat, TaCZ, Leawind, and Architectury jars to D:\asobi\mc\.minecraft\versions\1.21.1(mod2)\mods; awaiting user gameplay validation.

- OBS S09 manual validation 2026-05-04T02:00:00+08:00 user reported first-person recoil normal and crosshair behavior normal; user did not understand third-person recoil redirect, aim/scoping config, or draw guard checks.
- OBS S09 log scan 2026-05-04T02:00:00+08:00 instance entered singleplayer world and exited cleanly; tac-leawindtps.toml generated; no ModLoadingException, InvalidInjectionException, MixinApplyError, NoClassDefFoundError, NullPointerException, or hitResult NPE found. Strict V02 still not closed because latest.log contains a WARN ClassNotFoundException for optional Controllable BindingContext.

- START S09 2026-05-04T02:07:26.9382300+08:00
- S09 LOG PASS 2026-05-04T02:07:27.3703858+08:00 manual instance loaded required mods, entered singleplayer world, generated config, and had no compat-blocking loading/mixin/NPE/hitResult errors. Optional Controllable BindingContext ClassNotFoundException warning observed and recorded as unrelated to this compat mod.
- S09 USER GAMEPLAY PASS 2026-05-04T02:07:27.3714526+08:00 user reported third-person recoil redirect, first-person recoil, crosshair behavior, aim/scoping config behavior, and draw guard switching checks passed.

## Close-out Summary - NeoForge 1.21.1

- Gradle build passed.
- Manual instance loaded required mods and entered singleplayer world.
- Config `tac-leawindtps.toml` generated.
- Recoil validation passed by user gameplay check.
- Crosshair validation passed by user gameplay check.
- Aim/scoping perspective validation passed by user gameplay check.
- LocalPlayerDraw guard validation passed by user gameplay check and latest.log scan.
- README validation passed.
- Dependency jars not bundled.
- Local dependency jars not tracked by Git.
- Forge imports removed.
- MixinExtras removed.
- Obsolete `src/main/java/com/levanilla/TacZtps/mixin/client/MixinCamera.java` removed and exact mixin entry `"client.MixinCamera"` absent.
- Optional Controllable BindingContext ClassNotFoundException warning was observed in latest.log; it is unrelated to this compat mod and did not prevent world entry or gameplay validation.

- FINAL CLOSE-OUT PASS

- DONE S09 2026-05-04T02:07:36.8469499+08:00

## 2026-05-04 first-shot third-person hip-fire rotation sync

- START BUGFIX 2026-05-04T02:20:00+08:00
- Symptom: in Leawind third-person hip-fire, when cursor and player facing differ, the first shot after pressing fire rotates the player but does not show impact near the cursor; the second shot does.
- Root-cause evidence: TaCZ ClientMessagePlayerShoot carries only timestamp; server-side LivingEntityShoot receives ServerPlayer::getXRot/getYRot suppliers, so the first shot can use stale server rotation if Leawind has not synchronized camera-facing rotation before the shoot packet.
- Change: sync Leawind camera rotation to LocalPlayer and send ServerboundMovePlayerPacket.Rot during client GunShootEvent before TaCZ sends the shoot packet.
- BUILD PASS BUGFIX 2026-05-04T02:25:00+08:00 `.
gradlew.bat clean build` passed.
- GRAPHIFY PASS BUGFIX 2026-05-04T02:25:00+08:00 graphify manual-closeout passed.
- DEPLOY BUGFIX 2026-05-04T02:25:00+08:00 copied rebuilt compat jar to D:\asobi\mc\.minecraft\versions\1.21.1(mod2)\mods for user validation.
- CORRECTION BUGFIX 2026-05-04T02:26:00+08:00 build command was gradlew.bat clean build; previous line wrapped the command text incorrectly.
- RETRY BUGFIX 2026-05-04T02:32:00+08:00 user reported first shot impact is visible but offset from crosshair; changed sync target from raw Leawind camera rotation to yaw/pitch from player eye position toward Leawind CAMERA_AGENT hitResult location, with camera rotation fallback.
- BUILD PASS BUGFIX RETRY 2026-05-04T02:34:00+08:00 `gradlew.bat clean build` passed after crosshair target rotation sync.
- GRAPHIFY PASS BUGFIX RETRY 2026-05-04T02:34:00+08:00 graphify manual-closeout passed.
- DEPLOY BUGFIX RETRY 2026-05-04T02:34:00+08:00 copied rebuilt compat jar to D:\asobi\mc\.minecraft\versions\1.21.1(mod2)\mods for user validation.
- USER PASS BUGFIX RETRY 2026-05-04T02:40:00+08:00 user confirmed first-shot third-person hip-fire crosshair impact is normal after crosshair-target rotation sync.
- BUILD PASS BUGFIX FINAL 2026-05-04T02:40:00+08:00 `gradlew.bat clean build` passed before commit.
