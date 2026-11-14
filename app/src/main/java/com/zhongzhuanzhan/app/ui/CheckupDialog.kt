package com.zhongzhuanzhan.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zhongzhuanzhan.app.data.EndpointChecker
import com.zhongzhuanzhan.app.data.EndpointHealth
import com.zhongzhuanzhan.app.data.EndpointResult
import com.zhongzhuanzhan.app.data.LinkItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HealthyGreen = androidx.compose.ui.graphics.Color(0xFF4C8B3F)
private val WarnAmber = androidx.compose.ui.graphics.Color(0xFFB07A21)
private val FailRed = androidx.compose.ui.graphics.Color(0xFFA9402F)

/**
 * 入口体检：一次并发探测所有入口，回答「站点挂了还是我这边的问题」。
 * 打开即自动跑一轮，结果可复制成纯文本。
 */
@Composable
fun CheckupDialog(
    items: List<LinkItem>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var results by remember { mutableStateOf<List<EndpointResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }
    var stamp by remember { mutableStateOf("") }

    fun runCheck() {
        if (running) return
        running = true
        scope.launch {
            val now = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(Date())
            results = EndpointChecker.checkAll(items)
            stamp = now
            running = false
        }
    }

    LaunchedEffect(Unit) { runCheck() }

    Dialog(onDismissRequest = { if (!running) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = androidx.compose.ui.graphics.Color.White,
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(
                    text = "入口体检",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (running) "正在探测 ${items.size} 个入口…" else summaryText(results),
                    fontSize = 11.sp,
                    color = SubInk,
                )

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(results, key = { it.url + it.title }) { r ->
                        ResultRow(r)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Divider)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { runCheck() }, enabled = !running) {
                        Text(if (running) "检测中…" else "重新检测", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = {
                            if (results.isEmpty()) return@TextButton
                            clipboard.setText(
                                AnnotatedString(EndpointChecker.formatReport(results, stamp))
                            )
                            Toast.makeText(context, "体检结果已复制", Toast.LENGTH_SHORT).show()
                        },
                        enabled = results.isNotEmpty() && !running,
                    ) {
                        Text("复制结果", fontSize = 12.sp)
                    }
                    TextButton(onClick = onDismiss, enabled = !running) {
                        Text("关闭", fontSize = 12.sp, color = Ink)
                    }
                }

                Text(
                    text = "分享类入口返回 403 属正常——服务端在拦匿名访问，不代表它坏了。",
                    fontSize = 10.sp,
                    color = SubInk,
                    lineHeight = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun ResultRow(r: EndpointResult) {
    val (icon, tint) = when (r.health) {
        EndpointHealth.OK -> Icons.Filled.CheckCircle to HealthyGreen
        EndpointHealth.NEED_LOGIN -> Icons.Filled.Lock to WarnAmber
        EndpointHealth.FAILED -> Icons.Filled.ErrorOutline to FailRed
        EndpointHealth.CHECKING -> Icons.Filled.CheckCircle to SubInk
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = r.title,
            fontSize = 13.sp,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (r.health == EndpointHealth.CHECKING) "…" else r.detail,
            fontSize = 11.sp,
            color = tint,
            maxLines = 1,
        )
    }
}

private fun summaryText(results: List<EndpointResult>): String {
    if (results.isEmpty()) return "准备中…"
    val ok = results.count { it.health == EndpointHealth.OK }
    val login = results.count { it.health == EndpointHealth.NEED_LOGIN }
    val bad = results.count { it.health == EndpointHealth.FAILED }
    return buildString {
        append("正常 $ok")
        if (login > 0) append(" · 需登录 $login")
        if (bad > 0) append(" · 异常 $bad")
        if (bad == 0) append("，没有发现打不开的入口")
    }
}
