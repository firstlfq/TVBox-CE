# TVBox CE (Community Edition)

基于 [takagen99/Box](https://github.com/takagen99/Box) 二次开发的 TVBox 社区版。

## 环境要求

### 必须

| 工具 | 版本 | 说明 |
|------|------|------|
| Android Studio | Hedgehog (2023.1.1+) | 推荐 Arctic Fox+ |
| JDK | 11 | 本工程使用 Java 11 |
| Android SDK | compileSdk 34 | 需要 platform 34 + build-tools |
| Gradle | 7.5 (wrapper 自带) | 无需手动安装 |
| Git | 2.x | 代码管理 |
| Android TV 设备 / 模拟器 | API 21+ | 运行目标 |

### 推荐

- **SSH Key 配置到 GitHub**（免得每次输密码）
- **物理 Android TV 设备**（模拟器在 TV 模式下体验有限）

### 首次环境检查

```bash
java -version                          # 需 Java 11+
echo %ANDROID_HOME%                    # 应指向 Android SDK 路径
adb devices                            # 应检测到设备/模拟器
```

## 快速开始

```bash
# 克隆
git clone git@github.com:firstlfq/TVBox-CE.git
cd TVBox-CE

# 用 Android Studio 打开
# File -> Open -> 选择 TVBox-CE 目录

# 或用命令行构建（arm64）
./gradlew assembleArm64GenericNormalRelease

# 构建产物位置
# app/build/outputs/apk/arm64GenericNormal/release/TVBox_CE_release-arm64-generic-java.apk
```

### 构建所有变体

```bash
# arm64 + armeabi (32位) + python支持
./gradlew assembleRelease
```

## 项目结构

```
TVBox-CE/
├── app/                          # 主应用模块
│   ├── src/main/java/com/github/tvbox/osc/
│   │   ├── api/                  # 配置加载、Spider 调度
│   │   ├── base/                 # App、BaseActivity、BaseFragment
│   │   ├── bean/                 # 数据模型
│   │   ├── cache/                # Room 数据库
│   │   ├── player/               # 播放器（EXO/IJK/第三方）
│   │   ├── server/               # 嵌入式 HTTP 服务器
│   │   ├── subtitle/             # 字幕引擎
│   │   ├── ui/                   # 所有 UI 界面
│   │   ├── util/                 # 工具类（爬虫、JS引擎、加解密等）
│   │   └── viewmodel/            # ViewModel
│   ├── src/main/java/com/github/catvod/
│   │   └── crawler/              # 核心爬虫引擎（Spider/Jar/JS/Python 加载器）
│   ├── src/main/jniLibs/         # 预编译 so 库（IJKPlayer、QuickJS、P2P）
│   └── src/main/res/             # 资源文件
├── quickjs/                      # QuickJS JavaScript 引擎模块
├── pyramid/                      # Python 运行时模块（Chaquopy）
└── xwalk/                        # CrossWalk WebView（不再使用，仅保留参考）
```

## 已完成的工作

### V1 - 社区版初始构建（2026-05-25）

- [x] Fork takagen99/Box 源码
- [x] **重新品牌命名** → TVBox CE
- [x] **应用 ID** → `com.tvbox.ce`
- [x] **targetSdkVersion** → 34
- [x] **Java 编译版本** → 11
- [x] **Kotlin JVM target** → 11
- [x] **移除不兼容依赖** (nextlib-media3ext 编译自 Java 17, 与本项目 Java 11 不兼容)
- [x] **更新关于页面** → TVBox CE 品牌信息
- [x] **完整 Gradle 构建通过** → arm64 release APK 可正常生成
- [x] **GitHub 仓库初始化** → https://github.com/firstlfq/TVBox-CE
- [x] **README 开发文档**

## 后续开发计划

### Phase 2 ✅ 项目结构优化（预计 1-2 天）
- [ ] 引入 `namespace` 替代 AndroidManifest.xml 中的 package 声明（AGP 8.0 兼容准备）
- [ ] 清理过时的依赖（XWalkView 引用、过时第三方库）
- [ ] 统一代码风格（.editorconfig）
- [ ] 添加 lint 检查配置

### Phase 3 🔄 基础架构改造（预计 3-5 天）
- [ ] **Hilt DI 注入** — 替换手动单例模式（ApiConfig、AppDataManager 等）
- [ ] **Kotlin 迁移** — 核心工具类逐步转为 Kotlin
- [ ] **Repository 模式** — 数据访问层统一抽象
- [ ] **Sealed Class + StateFlow** — 替代 EventBus 的事件通信
- [ ] **AGP 升级** — 8.x（配合 Gradle 8.x）

### Phase 4 🎨 UI 现代化（预计 5-7 天）
- [ ] **Jetpack Compose** — 首页/分类页面试点迁移
- [ ] **Material 3 主题系统** — 动态取色
- [ ] **TV 端 Leanback 优化** — D-pad 焦点导航改进
- [ ] **手机端自适应布局** — portrait 模式支持
- [ ] **设置页面翻新** — Compose 重写

### Phase 5 🎬 播放器优化（预计 3-5 天）
- [ ] **Media3（ExoPlayer）升级** — 统一播放内核
- [ ] **IJKPlayer 编译更新** — 升级 ffmpeg 版本
- [ ] **播放器 UI 重构** — 手势操作、倍速、音轨切换
- [ ] **画中画（PiP）优化**

### Phase 6 ☁️ 新增功能（预计 5-7 天）
- [ ] **Firebase 多端同步** — 历史/收藏/配置跨设备
- [ ] **配置管理 UI** — 图形化源管理（不用输 JSON 地址）
- [ ] **下载管理** — 视频下载 + 离线播放
- [ ] **更新检查** — GitHub Release 检测 + OTA

### Phase 7 🔧 质量保障（持续）
- [ ] CI/CD 配置 (GitHub Actions)
- [ ] 崩溃监控 (Firebase Crashlytics)
- [ ] 性能优化（启动速度、内存、包体积）

## 开发说明

### 如何继续迭代

在其他电脑上继续开发：

```bash
git clone git@github.com:firstlfq/TVBox-CE.git
cd TVBox-CE
# 确保 Android SDK 已配置
```

### 分支策略

```
master        → 稳定版本，可构建发布
dev           → 日常开发分支
feat/xxx      → 功能分支
fix/xxx       → 修复分支
```

### 构建变体

| Flavor | 说明 |
|--------|------|
| arm64-generic-normal | arm64-v8a + Java 爬虫（推荐） |
| arm64-generic-python | arm64-v8a + Python 爬虫 |
| armeabi-generic-normal | armeabi-v7a + Java 爬虫 |
| hisense | 海信电视专用 |

### 注意点

1. **nextlib-media3ext 已被移除** — 因为该库编译自 Java 17，后续如需重新引入需升级 JDK
2. **`app/proguardMapping.txt` 不要提交** — 已添加到 .gitignore
3. **xwalk/ 目录仅保留参考** — 所有 zip 已从 git 中移除
4. **构建后记得清理**：`./gradlew clean`

## License

AGPL-3.0（沿用上游 TVBox 许可证）
