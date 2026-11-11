# 中转站 (ZhongZhuanZhan)

一个「网页入口站」安卓 App：首页是一个个场景卡片，点一下就在应用内打开对应的网站。

UI 按参考图还原：顶部标题栏 + 渐变横幅 + 「特色推荐」两列大卡 + 「其他」三列小卡 + 底部「最近访问」条。

- 语言/框架：**Kotlin + Jetpack Compose**（Material 3）
- 最低版本：Android 8.0 (API 26)，targetSdk 35
- 依赖极少：只有 Compose + Activity，无第三方库，APK 约 3 MB

## 目录结构

```
zhongzhuanzhan/
├── app/src/main/
│   ├── assets/links.json          ← 所有入口链接都在这里
│   ├── java/com/zhongzhuanzhan/app/
│   │   ├── MainActivity.kt        首页
│   │   ├── WebActivity.kt         承载网页的页面
│   │   ├── data/ConfigLoader.kt   解析 links.json
│   │   ├── data/RecentStore.kt    记录最近访问
│   │   └── ui/
│   │       ├── HomeScreen.kt      首页 UI
│   │       ├── WebScreen.kt       WebView + 顶部返回栏
│   │       ├── AppIcons.kt        icon 字段 → 图标
│   │       └── Theme.kt           配色
│   └── res/                       图标、主题、字符串
└── preview/index.html             浏览器里直接看 UI 的静态预览
```

## 怎么改链接（不用改代码）

编辑 `app/src/main/assets/links.json`：

```json
{
  "appName": "中转站",
  "banner": { "title": "中转", "accent": "直达站", "subtitle": "点一下，去你想去的地方", "url": "" },
  "sections": [
    {
      "title": "特色推荐",
      "style": "grid",
      "items": [
        { "id": "car", "title": "车载", "subtitle": "音乐 / 电台", "icon": "car", "url": "https://music.163.com/" }
      ]
    },
    {
      "title": "其他",
      "style": "compact",
      "items": [
        { "id": "heal", "title": "疗愈", "subtitle": "音频 / 电台", "icon": "heal", "url": "https://www.ximalaya.com/" }
      ]
    }
  ]
}
```

- `style: "grid"` → 两列大卡片；`style: "compact"` → 三列小卡片（可以再加 section，会一直往下排）
- `icon` 可选值：`portal`（罗盘）`camera`（相机）`bulb`（灯泡）`calendar`（日历勾）`person`（人像）`forum`（对话）`book`（书本）`graduation`（学士帽）`celebration`（庆祝）`web`（地球）
  旧场景图标仍兼容：`car` `mood` `sleep` `sport` `study` `shop` `game` `dance` `heal` `wedding` `baby`
- `banner.url` 留空 → 横幅不可点；填了就在应用内打开
- 首页右上角「⋮ → 重新载入配置」可以直接热重载，不必重装

## 打包

用 Android Studio 打开本目录，等 Gradle 同步完，Run 即可；或命令行：

```bash
./gradlew assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

### 签名 release 包

release 包用 `keystore/zhongzhuanzhan.jks` 签名，签名信息写在项目根目录的 `keystore.properties`（两者都在 `.gitignore` 里，不会进仓库）：

```properties
storeFile=keystore/zhongzhuanzhan.jks
storePassword=******
keyAlias=zhongzhuanzhan
keyPassword=******
```

```bash
./gradlew assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk
```

⚠️ **这个 jks 是唯一一份，丢了就无法再覆盖升级**（只能用新密钥重装）。务必把 `keystore/` 目录和 `keystore.properties` 备份到网盘/密码管理器。
换机器协作时，把这两个东西单独传给对方，不要提交到 git。

> 没配置 keystore 时，`assembleRelease` 会产出未签名包（安装会失败），gradle 脚本会自动跳过签名配置。

需要 `local.properties` 指向本机 SDK：

```properties
sdk.dir=C\:\\path\\to\\Android\\Sdk
```

### 本机已配好的离线构建链

本机没有 Android Studio，已用镜像装好一套可复用的工具链（约 2 GB，可以随时删）：

| 组件 | 路径 |
| --- | --- |
| JDK 21 | `../.toolchain/jdk/jdk-21.0.13+9` |
| Gradle 8.9 | `../.toolchain/gradle-8.9` |
| Android SDK (platform 35 / build-tools 35.0.0 / platform-tools) | `../.toolchain/sdk` |
| 依赖缓存 | `../.toolchain/gradle-home` |

双击 `build-debug.bat` 即可重新打包（已设好 JAVA_HOME / GRADLE_USER_HOME），产物在 `app/build/outputs/apk/debug/`。


## 网页打开策略

- `http/https` → 留在应用内 WebView，顶部自带返回/刷新/用浏览器打开
- `intent://` → 解析后交给对应 App（微信、支付宝、应用市场…），失败自动退回系统浏览器
- `weixin://` `alipays://` 等自定义 scheme → 尝试用系统打开，没有对应 App 时提示
- `<a download>` 触发的下载 → 交给系统 DownloadManager
- 页面主文档加载失败 → 显示「页面没能打开 / 重试」
