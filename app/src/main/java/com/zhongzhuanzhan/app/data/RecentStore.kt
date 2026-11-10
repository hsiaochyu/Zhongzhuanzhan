package com.zhongzhuanzhan.app.data

import android.content.Context

/** 记住上一次打开的站点，供首页底部「最近访问」条使用 */
object RecentStore {

    private const val PREF = "zzz_prefs"
    private const val KEY_TITLE = "recent_title"
    private const val KEY_URL = "recent_url"

    fun read(context: Context): Pair<String, String>? {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val title = sp.getString(KEY_TITLE, null) ?: return null
        val url = sp.getString(KEY_URL, null) ?: return null
        if (title.isBlank() || url.isBlank()) return null
        return title to url
    }

    fun write(context: Context, title: String, url: String) {
        if (title.isBlank() || url.isBlank()) return
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TITLE, title)
            .putString(KEY_URL, url)
            .apply()
    }

    /** 从完整 URL 里取一个简短域名，用作副标题 */
    fun prettyHost(url: String): String = runCatching {
        val host = android.net.Uri.parse(url).host.orEmpty()
        host.removePrefix("www.")
    }.getOrDefault(url)
}
