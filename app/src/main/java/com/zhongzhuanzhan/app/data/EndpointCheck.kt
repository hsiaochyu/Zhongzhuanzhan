package com.zhongzhuanzhan.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 入口体检：并发探一遍所有入口，回答「是站点挂了，还是我这边的问题」。
 *
 * 注意一个反直觉的结论——**分享类入口返回 403 是正常的**。
 * comate.wpsgo.com/s/xxx 这类页面在服务端会拦掉所有匿名请求，
 * 而这里用的是 HttpURLConnection（不带 WebView 的登录 cookie），
 * 所以必然会拿到 403。它不能证明「你登录了」，只能证明「服务器还活着且在正常拦截」。
 *
 * 真正有问题的是超时、域名解析失败、5xx——那才是站点真的挂了。
 */
enum class EndpointHealth {
    CHECKING,
    OK,
    NEED_LOGIN,
    FAILED,
}

data class EndpointResult(
    val title: String,
    val url: String,
    val health: EndpointHealth = EndpointHealth.CHECKING,
    val detail: String = "",
)

object EndpointChecker {

    private const val CONNECT_TIMEOUT_MS = 6000
    private const val READ_TIMEOUT_MS = 6000

    suspend fun checkAll(items: List<LinkItem>): List<EndpointResult> =
        withContext(Dispatchers.IO) {
            items.map { item -> async { probe(item) } }.awaitAll()
        }

    private fun probe(item: LinkItem): EndpointResult {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(item.url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = true
                // 只要响应头，正文最多捞 1KB，避免真的把整个页面拖下来
                setRequestProperty("Range", "bytes=0-1023")
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/130.0 Mobile Safari/537.36",
                )
                setRequestProperty("Accept", "text/html,*/*")
            }

            val code = conn.responseCode
            when (code) {
                in 200..399 -> EndpointResult(item.title, item.url, EndpointHealth.OK, "$code")
                401, 403 -> EndpointResult(
                    item.title,
                    item.url,
                    EndpointHealth.NEED_LOGIN,
                    "$code 需要登录",
                )
                in 500..599 -> EndpointResult(
                    item.title,
                    item.url,
                    EndpointHealth.FAILED,
                    "$code 服务器错误",
                )
                else -> EndpointResult(item.title, item.url, EndpointHealth.FAILED, "HTTP $code")
            }
        } catch (_: SocketTimeoutException) {
            EndpointResult(item.title, item.url, EndpointHealth.FAILED, "超时")
        } catch (_: UnknownHostException) {
            EndpointResult(item.title, item.url, EndpointHealth.FAILED, "域名解析失败")
        } catch (_: SSLException) {
            EndpointResult(item.title, item.url, EndpointHealth.FAILED, "证书错误")
        } catch (e: IOException) {
            val reason = e.message?.take(40)?.ifBlank { "网络不通" } ?: "网络不通"
            EndpointResult(item.title, item.url, EndpointHealth.FAILED, reason)
        } catch (e: Exception) {
            EndpointResult(item.title, item.url, EndpointHealth.FAILED, e.javaClass.simpleName)
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    /** 汇总成一段可复制的纯文本，方便贴到群里或直接发给我。 */
    fun formatReport(results: List<EndpointResult>, stamp: String): String = buildString {
        val ok = results.count { it.health == EndpointHealth.OK }
        val login = results.count { it.health == EndpointHealth.NEED_LOGIN }
        val bad = results.count { it.health == EndpointHealth.FAILED }
        appendLine("【中转站 · 入口体检】$stamp")
        appendLine("正常 $ok · 需登录 $login · 异常 $bad")
        results.forEach { r ->
            val mark = when (r.health) {
                EndpointHealth.OK -> "✓"
                EndpointHealth.NEED_LOGIN -> "🔒"
                EndpointHealth.FAILED -> "✗"
                EndpointHealth.CHECKING -> "…"
            }
            appendLine("$mark ${r.title} — ${r.detail}")
        }
    }
}
