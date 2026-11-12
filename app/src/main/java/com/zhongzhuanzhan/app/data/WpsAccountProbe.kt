package com.zhongzhuanzhan.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject

enum class WpsAccountStatus { IDLE, CHECKING, LOGGED_IN, LOGGED_OUT, TIMEOUT }

data class WpsAccountState(
    val status: WpsAccountStatus,
    val displayName: String? = null,
)

/**
 * 用一个隐藏 WebView 探测 wpsgo 登录态。
 * 原理：匿名访问 share 链接会被服务端 403 挡下（实测），
 * 因此「页面加载成功且没有跳到登录页」≈ WPS 账号已登录；
 * 登录后再从 localStorage 里找常见用户名字段来展示。
 */
class WpsAccountProbe(private val context: Context) {

    private var webView: WebView? = null
    private var callback: ((WpsAccountState) -> Unit)? = null
    private var finished = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val timeoutRunnable = Runnable {
        if (!finished) {
            finished = true
            deliver(WpsAccountState(WpsAccountStatus.TIMEOUT))
        }
    }

    fun check(onResult: (WpsAccountState) -> Unit) {
        callback = onResult
        finished = false
        deliver(WpsAccountState(WpsAccountStatus.CHECKING))
        val wv = ensureWebView()
        mainHandler.removeCallbacks(timeoutRunnable)
        mainHandler.postDelayed(timeoutRunnable, 12_000)
        wv.loadUrl(PROBE_URL)
    }

    fun destroy() {
        mainHandler.removeCallbacks(timeoutRunnable)
        webView?.stopLoading()
        webView?.destroy()
        webView = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun ensureWebView(): WebView {
        webView?.let { return it }
        val wv = WebView(context)
        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (finished) return
                // 被重定向到登录页 = 未登录
                if (url.contains("login", ignoreCase = true) || url.contains("account.wps.cn")) {
                    finish(WpsAccountState(WpsAccountStatus.LOGGED_OUT))
                    return
                }
                view.evaluateJavascript(PROBE_JS) { raw ->
                    if (finished) return@evaluateJavascript
                    finish(parseProbeResult(raw))
                }
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse,
            ) {
                // 匿名访问 share 链接主文档 403 = 未登录（子资源 403 不管）
                if (finished) return
                if (request.isForMainFrame) {
                    finish(WpsAccountState(WpsAccountStatus.LOGGED_OUT))
                }
            }
        }
        webView = wv
        return wv
    }

    private fun finish(state: WpsAccountState) {
        finished = true
        mainHandler.removeCallbacks(timeoutRunnable)
        deliver(state)
    }

    private fun deliver(state: WpsAccountState) {
        mainHandler.post { callback?.invoke(state) }
    }

    private fun parseProbeResult(raw: String?): WpsAccountState {
        val fallback = WpsAccountState(WpsAccountStatus.LOGGED_IN)
        if (raw.isNullOrEmpty() || raw == "null") return fallback
        return try {
            // evaluateJavascript 返回的是 JSON 编码过的字符串，先解开一层
            val decoded = JSONObject("{\"r\":$raw}").optString("r")
            val obj = JSONObject(decoded)
            WpsAccountState(WpsAccountStatus.LOGGED_IN, displayName = findName(obj.optJSONObject("ls")))
        } catch (_: Exception) {
            fallback
        }
    }

    private fun findName(ls: JSONObject?): String? {
        if (ls == null) return null
        val nameFields = listOf(
            "nickname", "nick_name", "nickName", "userName", "username",
            "name", "displayName", "realName", "real_name",
        )
        for (key in ls.keys()) {
            val value = ls.optString(key)
            if (value.isBlank()) continue
            try {
                val inner = JSONObject(value)
                for (field in nameFields) {
                    val candidate = inner.optString(field)
                    if (candidate.isNotBlank()) return cleanName(candidate)
                }
            } catch (_: Exception) {
                // 值是普通字符串：key 像昵称、值够短且不像 token 才敢当用户名
                val looksLikeNameKey = key.contains(Regex("nick|user_?name|display_?name", RegexOption.IGNORE_CASE))
                if (looksLikeNameKey && value.length <= 24 && !value.contains('=')) {
                    return cleanName(value)
                }
            }
        }
        return null
    }

    private fun cleanName(s: String): String = s.trim().take(24)

    companion object {
        private const val PROBE_URL = "https://comate.wpsgo.com/s/mVJ7J8UuzHx2/?"

        private val PROBE_JS = """
            (function(){
                var hits = {};
                try {
                    for (var i = 0; i < localStorage.length; i++) {
                        var k = localStorage.key(i);
                        var v = localStorage.getItem(k) || '';
                        if (/user|account|nick|name|login|member|profile|avatar/i.test(k)) {
                            hits[k] = v.length > 300 ? v.slice(0, 300) : v;
                        }
                    }
                } catch (e) {}
                return JSON.stringify({ cookieLen: document.cookie.length, ls: hits });
            })();
        """.trimIndent()
    }
}
