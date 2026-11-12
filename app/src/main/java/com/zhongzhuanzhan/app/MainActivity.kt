package com.zhongzhuanzhan.app

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.zhongzhuanzhan.app.data.ConfigLoader
import com.zhongzhuanzhan.app.data.RecentStore
import com.zhongzhuanzhan.app.data.WpsAccountProbe
import com.zhongzhuanzhan.app.data.WpsAccountState
import com.zhongzhuanzhan.app.data.WpsAccountStatus
import com.zhongzhuanzhan.app.ui.HomeScreen
import com.zhongzhuanzhan.app.ui.HubTheme

class MainActivity : ComponentActivity() {

    private val recentState = mutableStateOf<Pair<String, String>?>(null)
    private val wpsAccountState = mutableStateOf(WpsAccountState(WpsAccountStatus.IDLE))
    private var accountProbe: WpsAccountProbe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val config = ConfigLoader.load(this)
        recentState.value = RecentStore.read(this)

        setContent {
            HubTheme {
                HomeScreen(
                    config = config,
                    recent = recentState.value,
                    account = wpsAccountState.value,
                    onOpenUrl = { title, url ->
                        RecentStore.write(this, title, url)
                        recentState.value = RecentStore.read(this)
                        WebActivity.start(this, title, url)
                    },
                    onReopen = {
                        recentState.value?.let { WebActivity.start(this, it.first, it.second) }
                    },
                    onReload = { recreate() },
                    onCheckAccount = { checkWpsAccount() },
                    onClearWebLogin = { clearWebLogin() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recentState.value = RecentStore.read(this)
    }

    override fun onDestroy() {
        accountProbe?.destroy()
        accountProbe = null
        super.onDestroy()
    }

    /** 隐藏 WebView 探测 wpsgo 登录态，结果驱动「我的」页的账户卡片 */
    private fun checkWpsAccount() {
        val probe = accountProbe ?: WpsAccountProbe(this).also { accountProbe = it }
        probe.check { state -> wpsAccountState.value = state }
    }

    /** 清掉全部 WebView Cookie/localStorage（含 WPS 登录态与各入口的免密状态） */
    private fun clearWebLogin() {
        CookieManager.getInstance().removeAllCookies { }
        WebStorage.getInstance().deleteAllData()
        wpsAccountState.value = WpsAccountState(WpsAccountStatus.IDLE)
    }
}
