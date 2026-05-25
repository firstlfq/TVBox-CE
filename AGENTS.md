# TVBox CE - 开发上下文

## 项目简介

基于 takagen99/Box 源码二次开发的 TVBox Community Edition，目标平台 Android（TV + 手机）。

## 技术栈

- Java + Kotlin（原始 TVBox 源码）
- Android Gradle Plugin 7.4.2 + Gradle 7.5
- Kotlin 1.9.25
- compileSdk 34 / targetSdk 34 / minSdk 21
- Java 11（跨平台兼容，JDK 17 不可用）
- 构建命令：`./gradlew assembleArm64GenericNormalRelease`
- APK 产物：`app/build/outputs/apk/arm64GenericNormal/release/TVBox_CE_release-arm64-generic-java.apk`

## Git 仓库

- 远程：`git@github.com:firstlfq/TVBox-CE.git`
- 分支：`master`（稳定版），`dev`（日常开发）
- PR 用 SSH 推送

## 已完成工作

1. **Rebranding** — 包名 `com.tvbox.ce`，app_name "TVBox CE"，关于页面品牌更新
2. **移除不兼容依赖** — `nextlib-media3ext:0.7.1`（编译自 Java 17，本机只有 Java 11）
3. **HawkUtils.java** — 移除了 NextRenderersFactory 引用，统一用 DefaultRenderersFactory
4. **构建验证通过** — arm64 release APK 正常生成
5. **重新初始化 Git 仓库** — 作者已修正为 lee，已推送至 GitHub

## 关键架构

- `app/src/main/java/com/github/tvbox/osc/` — 主应用代码（UI、播放器、缓存、服务器）
- `app/src/main/java/com/github/catvod/crawler/` — 核心爬虫引擎（Spider/Jar/JS/Python 加载器）
- `app/src/main/jniLibs/` — 预编译 so（IJKPlayer、QuickJS、P2P）
- 播放器方案：ExoPlayer 单引擎（无 nextlib 扩展），未集成 IJKPlayer
- 数据层：Room DB (SQLite)
- DI：手动单例模式（待改造）
- 事件通信：EventBus（待改造）
- 无 Navigation Component，使用 Activity/Fragment 间显式调用

## 注意事项

- `app/proguardMapping.txt` 不要提交，已 gitignore
- `xwalk/` 目录的 .zip 已从 git 中移除（104MB）
- `nextlib-media3ext` 如果后续要重新引入，需将 JDK 升级到 17
- 本地构建时 `local.properties` 指向本地 Android SDK，不同机器需重新创建
- `commandlinetools-linux-11076708_latest.zip` 需要 Java 17，JDK 11 环境需使用 `commandlinetools-linux-9477386_latest.zip`

## 开发计划（Phase 2 开始）

### Phase 2 — 项目结构优化
- [ ] 在 `app/build.gradle` 中添加 `namespace = "com.github.tvbox.osc"`（AGP 8 兼容准备）
- [ ] 清理过时依赖（XWalkView 相关代码、过时第三方库）
- [ ] 添加 `.editorconfig`
- [ ] 添加 lint 检查配置

### Phase 3 — 基础架构改造
- [ ] Hilt DI 替换手动单例（ApiConfig, AppDataManager 等）
- [ ] 核心工具类 Kotlin 迁移
- [ ] Repository 模式统一数据访问
- [ ] Sealed Class + StateFlow 替代 EventBus

### Phase 4 — UI 现代化
- [ ] Jetpack Compose 首页/分类迁移
- [ ] Material 3 主题 + 动态取色
- [ ] Leanback D-pad 焦点优化
- [ ] 手机端 Portrait 布局适配

### Phase 5 — 播放器优化
- [ ] Media3 统一播放内核
- [ ] IJKPlayer ffmpeg 升级
- [ ] 播放器 UI 重构（手势、倍速、音轨）
- [ ] PiP 画中画优化

### Phase 6 — 新增功能
- [ ] Firebase 多端同步
- [ ] 配置管理 UI
- [ ] 下载管理 + 离线播放
- [ ] OTA 更新

### Phase 7 — 质量保障
- [ ] GitHub Actions CI/CD
- [ ] Firebase Crashlytics
- [ ] 性能优化

## 新机器恢复步骤

```bash
# 1. 生成 SSH Key 并添加到 GitHub
ssh-keygen -t ed25519 -C "firstlfq@foxmail.com"
# 公钥添加到 https://github.com/settings/keys

# 2. 克隆仓库
git clone git@github.com:firstlfq/TVBox-CE.git

# 3. 确认 Android SDK 环境
# 确保 ANDROID_HOME 环境变量已设置
# Android Studio 会自动检测

# 4. 用 Android Studio 打开 TVBox-CE 目录
# 等待 Gradle sync 完成后可直接构建

# 5. 命令行构建
cd TVBox-CE
./gradlew assembleArm64GenericNormalRelease
```
