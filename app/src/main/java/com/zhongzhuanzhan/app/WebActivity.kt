package com.zhongzhuanzhan.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zhongzhuanzhan.app.ui.HubTheme
import com.zhongzhuanzhan.app.ui.WebScreen

class WebActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_URL = "extra_url"

        fun start(context: Context, title: String, url: String) {
            val intent = Intent(context, WebActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_URL, url)
            context.startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        // 切到后台就把 cookie 落盘：WebView 的 cookie 默认只在内存里攒着，
        // 系统在后台杀进程时会连带丢掉，表现为「下次打开又要重新登录」
        CookieManager.getInstance().flush()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        if (url.isBlank()) {
            finish()
            return
        }

        setContent {
            HubTheme {
                WebScreen(
                    initialUrl = url,
                    initialTitle = title,
                    onClose = { finish() },
                )
            }
        }
    }
}
