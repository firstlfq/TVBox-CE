# TVBox CE (Community Edition)

基于 [takagen99/Box](https://github.com/takagen99/Box) 二次开发的 TVBox 社区版，支持 Android TV 和手机。

## 特性

- 多仓/单仓订阅管理（URL 订阅 + 线路选择）
- 播放器核心：ExoPlayer + IJKPlayer
- 多引擎爬虫：Java / JS / Python
- 搜索、收藏、历史记录
- 主题切换（7 套）、壁纸、DNS over HTTPS
- 嵌入式 HTTP 服务器（手机端遥控）

## 构建

```bash
# 环境：JDK 11, Android SDK 34
./gradlew assembleArm64GenericNormalRelease
```

产物：`app/build/outputs/apk/arm64GenericNormal/release/TVBox_CE_release-arm64-generic-java.apk`

| 构建变体 | 说明 |
|---------|------|
| arm64-generic-normal | arm64-v8a + Java 爬虫（推荐） |
| arm64-generic-python | arm64-v8a + Python 爬虫 |
| armeabi-generic-normal | armeabi-v7a + Java 爬虫 |

## 技术栈

- **语言**：Java + Kotlin
- **DI**：Hilt
- **播放器**：ExoPlayer + IJKPlayer
- **数据库**：Room (SQLite)
- **爬虫**：Java Jar / QuickJS / Python (Chaquopy)
- **事件**：EventBus

## License

AGPL-3.0
