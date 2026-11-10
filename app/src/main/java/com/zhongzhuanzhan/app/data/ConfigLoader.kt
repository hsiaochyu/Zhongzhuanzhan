package com.zhongzhuanzhan.app.data

import android.content.Context
import org.json.JSONObject

/** 一个入口：点一下就打开 [url] */
data class LinkItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val url: String,
)

/** 一组入口。style = "grid"（两列大卡）或 "compact"（三列小卡） */
data class Section(
    val title: String,
    val style: String,
    val items: List<LinkItem>,
)

data class Banner(
    val title: String,
    val accent: String,
    val subtitle: String,
    val url: String,
)

data class AppConfig(
    val appName: String,
    val banner: Banner,
    val sections: List<Section>,
)

/**
 * 读取 assets/links.json。改链接只需要改这个 JSON 文件，不用动代码。
 * 解析失败时回退到内置的兜底配置，保证 app 不会白屏。
 */
object ConfigLoader {

    private const val ASSET_NAME = "links.json"

    fun load(context: Context): AppConfig {
        val raw = runCatching {
            context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull()

        return raw?.let { text -> runCatching { parse(text) }.getOrNull() } ?: fallback()
    }

    private fun parse(raw: String): AppConfig {
        val root = JSONObject(raw)

        val bannerJson = root.optJSONObject("banner")
        val banner = Banner(
            title = bannerJson?.optString("title").orEmpty().ifBlank { "中转" },
            accent = bannerJson?.optString("accent").orEmpty().ifBlank { "直达站" },
            subtitle = bannerJson?.optString("subtitle").orEmpty(),
            url = bannerJson?.optString("url").orEmpty(),
        )

        val sections = mutableListOf<Section>()
        val sectionsJson = root.optJSONArray("sections")
        if (sectionsJson != null) {
            for (i in 0 until sectionsJson.length()) {
                val sectionJson = sectionsJson.optJSONObject(i) ?: continue
                val itemsJson = sectionJson.optJSONArray("items") ?: continue

                val items = mutableListOf<LinkItem>()
                for (j in 0 until itemsJson.length()) {
                    val itemJson = itemsJson.optJSONObject(j) ?: continue
                    val url = itemJson.optString("url").trim()
                    if (url.isEmpty()) continue
                    items += LinkItem(
                        id = itemJson.optString("id").ifBlank { "item_$i$j" },
                        title = itemJson.optString("title").ifBlank { "未命名" },
                        subtitle = itemJson.optString("subtitle"),
                        icon = itemJson.optString("icon").ifBlank { "web" },
                        url = url,
                    )
                }

                if (items.isNotEmpty()) {
                    sections += Section(
                        title = sectionJson.optString("title"),
                        style = sectionJson.optString("style").ifBlank { "grid" },
                        items = items,
                    )
                }
            }
        }

        return AppConfig(
            appName = root.optString("appName").ifBlank { "中转站" },
            banner = banner,
            sections = sections.ifEmpty { fallbackSections() },
        )
    }

    private fun fallbackSections() = listOf(
        Section(
            title = "特色推荐",
            style = "grid",
            items = listOf(
                LinkItem("baidu", "百度", "搜索 / 资讯", "web", "https://www.baidu.com"),
                LinkItem("bili", "哔哩哔哩", "视频 / 番剧", "game", "https://www.bilibili.com"),
            ),
        )
    )

    private fun fallback() = AppConfig(
        appName = "中转站",
        banner = Banner("中转", "直达站", "点一下，去你想去的地方", ""),
        sections = fallbackSections(),
    )
}
