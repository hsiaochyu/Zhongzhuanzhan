package com.zhongzhuanzhan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.zhongzhuanzhan.app.data.ConfigLoader
import com.zhongzhuanzhan.app.data.RecentStore
import com.zhongzhuanzhan.app.ui.HomeScreen
import com.zhongzhuanzhan.app.ui.HubTheme

class MainActivity : ComponentActivity() {

    private val recentState = mutableStateOf<Pair<String, String>?>(null)

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
                    onOpenUrl = { title, url ->
                        RecentStore.write(this, title, url)
                        recentState.value = RecentStore.read(this)
                        WebActivity.start(this, title, url)
                    },
                    onReopen = {
                        recentState.value?.let { WebActivity.start(this, it.first, it.second) }
                    },
                    onExit = { finish() },
                    onReload = { recreate() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recentState.value = RecentStore.read(this)
    }
}
