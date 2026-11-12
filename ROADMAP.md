# 路线图 · v1.2 体验修整 & v2 认证整合

> 状态：**预研，未开工**。这份文档只用来把想法和坑记下来，等要动手时按它拆任务。
> 2026-11-12 版本号重组：原 v2.0（登录态保持）量级只有半天，降级并入 v1.2；
> v2.0 留给「认证整合」完整落地（一次登录全站通行），才配得上大版本号。

## 1. 要解决的问题

中转站里的入口大多是「金山文档（WPS）分享页 + 页面内二次认证」的结构：

```
点卡片 → 打开 comate.wpsgo.com/s/xxx  →  ① WPS 账号登录
                                      →  ② 页面内认证（学号/密码、验证码、或智能体自己的 token）
                                      →  ③ 进入功能页
```

现在的体验：每个入口都要走一遍 ①+②，来回切、反复输密码。目标是**一次认证，所有入口免重复登录**。

## 2. 先分清两种「认证」

| 层 | 谁管 | 能不能在 App 里做 | 难点 |
| --- | --- | --- | --- |
| ① WPS 账号登录 | Cookie / 登录态，同域（`.wpsgo.com`、`.wps.cn`） | **能**，且成本低 | 登录态持久化、UA 识别、跨卡片共享 |
| ② 页面内认证 | 各站点自己的逻辑（表单一、token、短信） | **部分能** | 表单会改版、不能明文存密码、可能违反站点条款 |

结论：**① 值得做，收益最大**；② 只做「辅助填充」，不追求全自动。

## 3. 技术方案拆解

### 3.1 登录态保持（成本最低，优先级最高）

WebView 的 Cookie / localStorage 默认就带持久化，但有几个坑：

- `CookieManager.getInstance().flush()`：进程被杀前主动落盘，否则冷启动可能丢登录态
- `setAcceptThirdPartyCookies(webView, true)`：WPS 页面常嵌跨域 iframe，不开这个登录会失效
- `setAcceptCookie(true)` + `CookieManager.removeSessionCookies()` 不要乱调
- 多张卡片共用一个 WebView（或同一 `WebView` 实例池），Cookie 天然共享；现在每张卡开新 Activity 也会共享进程级 Cookie，问题不大
- **冷启动恢复**：App 启动时检查关键域是否还有登录 Cookie，没有才引导登录

### 3.2 账号中心 + 自动填充（推荐路线）

不要在 App 里自己存密码，改用系统能力：

- **Android Autofill Framework**（API 26+ 正好是 minSdk）：给 WebView 的输入框挂 autofill hint，系统密码管理器（Google 密码管理器 / 厂商密码本）自动弹「用保存的账号填充」
- **Credential Manager**（API 34+ 起官方主推）：支持密码、Passkey
- 好处：密码不进我们的数据库，安全责任交给系统；页面改版也不影响
- 代价：用户首次仍要自己输一遍并存到系统密码本

### 3.3 统一会话（进阶）

如果多个站点同属一个域，一次登录即全部免登；跨域则需要：

- 从页面读 token：`evaluateJavascript("localStorage.getItem('xxx')")` → 存到 EncryptedSharedPreferences
- 再注入到其它入口：`evaluateJavascript("localStorage.setItem('xxx','...')")`（**仅同源有效**，跨域会被浏览器安全策略挡住）
- 真正跨域只能靠站点自己提供 OAuth/OIDC/扫码登录接口

### 3.4 安全与合规

- 密码/token 一律 `EncryptedSharedPreferences`（底层 Android Keystore），禁止明文、禁止进日志
- WebView 的 `addJavascriptInterface` 慎用：注入的 JS 桥等于给网页开了原生能力
- 自动化登录可能违反站点服务条款，动手前先确认：这些页面是学校/自己可控的服务，还是第三方？
- `usesCleartextTraffic=true` 现在是开着的（为了那几个 http 入口），正式版建议收窄成域名白名单

## 4. 分阶段计划

| 阶段 | 内容 | 预估工作量 | 产出 |
| --- | --- | --- | --- |
| **v1.2** | **体验修整版**：登录态保持（cookie flush、第三方 cookie、单一 WebView 复用、启动检查登录态，即原 v2.0）+ 链接疑点排查（`124.156.164.21:8080` 疑似应为 http）+ `usesCleartextTraffic` 收窄成域名白名单 + versionCode 升 3 | 半天～1 天 | 一次登录，重启 App 仍在线；几个小修一并打包 |
| v2.0 | 账号中心：Autofill / Credential Manager 接入 + 系统密码本联动 | 1 天 | 首次输一次，之后系统自动填 |
| v2.1 | 统一会话：同域 token 提取与注入、按站点配置的注入脚本（远程 JSON 下发） | 2-3 天 | 绝大多数入口免二次认证 |
| v2.2（可选） | 扫码登录 / OAuth：如果站点支持，走标准协议最稳 | 视接口而定 | 完全无感的登录 |

> 前提：v1.2 建议在 v1.1 发 Release 并收集一两周同学反馈之后动工，
> 反馈里最痛的点优先级高于本表预设顺序。

## 5. 动手前需要确认的事

1. 这些入口站点是**学校/自己搭的**，还是第三方？能不能改它的登录逻辑？（决定走 3.2 还是 3.3）
2. 页面内认证具体是什么形式：账号密码？短信？扫码？企业微信/微信授权？
3. 有没有后端可以配合？如果有，最省事的方案是「App 登录一次 → 后端签发统一 token → 各站点用 token 换登录态」
4. 是否接受「密码交给系统密码管理器」这条路（推荐）还是坚持 App 内记住账号

## 6. 参考资料

- [Autofill Framework](https://developer.android.com/guide/topics/text/autofill)
- [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager)
- [WebView Cookie 管理](https://developer.android.com/reference/android/webkit/CookieManager)
- [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
