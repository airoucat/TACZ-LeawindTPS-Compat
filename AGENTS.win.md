# Windows Local Context

本文件只保存这台 Windows 机器上的本地上下文，不作为 shared repo truth。

## 本机工具链

- 当前机器已验证可用的 `JDK 21`：
  - `C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot`
- 当前用户的 `JAVA_HOME` 在其他 Forge 项目中已指向上述 JDK。
- 本仓库是 `Forge 1.20.1` 项目；若当前 shell 已拿到正确的 `JAVA_HOME`，直接使用 `.\gradlew.bat` 即可。

## 推荐命令

- `.\gradlew.bat test`
- `.\gradlew.bat runClient`
- `.\gradlew.bat runServer`
- `.\gradlew.bat runGameTestServer`
- `.\gradlew.bat build`
- `python3 scripts/dev/setup_graphify_local.py`
- `python3 scripts/dev/setup_graphify_local.py rebuild --reason manual-closeout`

## 本机测试实例

- NeoForge 1.21.1 手工验证实例 `mods/` 路径：
  - `D:\asobi\mc\.minecraft\versions\1.21.1(mod2)\mods`
- 已用于复制当前 compat jar 与 TaCZ / Leawind / Architectury 本地运行依赖，供 S09 runtime/gameplay 验证。
- 若后续确定了这台机器专用的 `mods/` 或实例根目录，只在本文件追加，不要写进 `docs/current-truth/` 或 `docs/harness/`。

## 排错

- 如果 Gradle 报 `JAVA_HOME` 或 JVM 版本不对，先重新打开终端再进入仓库。
- 如果 `graphify` 未安装，直接执行：
  - `python3 scripts/dev/setup_graphify_local.py`
