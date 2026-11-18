package com.zhongzhuanzhan.app.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(
    initialUrl: String,
    initialTitle: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var pageTitle by remember { mutableStateOf(initialTitle) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var progress by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }

    val goBack: () -> Unit = {
        val wv = webView
        if (wv != null && wv.canGoBack()) wv.goBack() else onClose()
    }

    BackHandler { goBack() }

    DisposableEffect(Unit) {
        onDispose {
            webView?.let { wv ->
                wv.stopLoading()
                wv.loadUrl("about:blank")
                wv.destroy()
            }
            webView = null
            // 把内存里的 cookie 落盘，否则进程被杀时会丢掉登录态
            CookieManager.getInstance().flush()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = goBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Ink,
                )
            }
            Text(
                text = pageTitle.ifBlank { "网页" },
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = { webView?.reload() }) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "刷新",
                    tint = Ink,
                )
            }
            IconButton(onClick = { openExternal(context, currentUrl) }) {
                Icon(
                    imageVector = Icons.Outlined.OpenInBrowser,
                    contentDescription = "用浏览器打开",
                    tint = Ink,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            if (loading) {
                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = Accent,
                    trackColor = Color(0xFFEFF1F5),
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        setBackgroundColor(android.graphics.Color.WHITE)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadsImagesAutomatically = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString = userAgentString + " ZhongZhuanZhan/1.0"
                        }

                        // 登录态：接受 cookie + 允许第三方 cookie（分享页的登录回调常跨子域），
                        // 关掉 WebView 时再 flush 落盘，避免进程被杀后掉登录
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                                loading = newProgress < 100
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (!title.isNullOrBlank()) pageTitle = title
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                val target = view ?: return false
                                return handleUri(context, request?.url) { target.loadUrl(it) }
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let { currentUrl = it }
                                failed = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let { currentUrl = it }
                                loading = false
                                progress = 100
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    failed = true
                                    loading = false
                                }
                            }
                        }

                        setDownloadListener { url, _, _, mimeType, _ ->
                            downloadFile(context, url, mimeType)
                        }

                        loadUrl(initialUrl)
                        webView = this
                    }
                },
                update = { },
            )

            if (failed) {
                ErrorOverlay(
                    onRetry = {
                        failed = false
                        webView?.reload()
                    },
                    onClose = onClose,
                )
            }
        }
    }
}

@Composable
private fun ErrorOverlay(onRetry: () -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("页面没能打开", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text("可能是网络不通，或该站点不允许内嵌浏览", fontSize = 12.sp, color = SubInk)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRetry) { Text("重试") }
            OutlinedButton(onClick = onClose) { Text("返回") }
        }
    }
}

/**
 * 处理网页里的各种链接：
 * - http/https -> 留在应用内
 * - intent://  -> 交给对应的 App（微信、支付宝、应用市场等）
 * - 其它自定义 scheme -> 尝试用系统打开
 */
private fun handleUri(context: Context, uri: Uri?, load: (String) -> Unit): Boolean {
    if (uri == null) return false
    return when (uri.scheme?.lowercase()) {
        null, "http", "https" -> false

        "file" -> true

        "intent" -> {
            runCatching {
                val intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }.onFailure {
                openExternal(context, uri.toString())
            }
            true
        }

        else -> {
            runCatching {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }.onFailure {
                Toast.makeText(context, "没有可以打开该链接的应用", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }
}

private fun openExternal(context: Context, url: String) {
    if (url.isBlank()) return
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "没有可以打开该链接的应用", Toast.LENGTH_SHORT).show()
    }
}

private fun downloadFile(context: Context, url: String, mimeType: String?) {
    runCatching {
        val name = URLUtil.guessFileName(url, null, mimeType)
        val request = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(name)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, name)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "开始下载：$name", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
    }
}
