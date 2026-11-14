package com.zhongzhuanzhan.app.ui

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * 反馈页（全屏对话框形态）。
 *
 * 设计背景：GitHub 在当前网络环境下连不上，没法直接跳 Issue 页面。
 * 所以这里退一步——把反馈按固定格式整理好，一键复制，同学粘贴到班级群即可。
 */
@Composable
fun FeedbackDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F6F8),
        ) {
            FeedbackPage(onBack = onDismiss)
        }
    }
}

@Composable
private fun FeedbackPage(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val clipboard = LocalClipboardManager.current
    var scene by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var expect by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }

    val canCopy = scene.isNotBlank() || current.isNotBlank() || expect.isNotBlank()

    LaunchedEffect(copied) {
        if (copied) {
            delay(2600)
            copied = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
    ) {
        // 顶栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Ink,
                )
            }
            Text(
                text = "反馈",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        ) {
            // 说明
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x12000000), spotColor = Color(0x12000000))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "说点什么吧",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "想加个新入口、发现某个卡片打不开、或者觉得哪里不好用，都可以写在这里。" +
                        "填完点下面的按钮，内容会复制到你的剪贴板，粘贴到班级群就行——" +
                        "暂时连不上 GitHub，所以先走这条路。",
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    color = SubInk,
                )
            }

            Spacer(Modifier.height(12.dp))

            // 三个输入框
            FeedbackField(
                label = "什么场景下会用到",
                hint = "例：在宿舍连着 WiFi 打开学搭子",
                value = scene,
                onValueChange = { scene = it },
            )
            Spacer(Modifier.height(10.dp))
            FeedbackField(
                label = "现在会发生什么",
                hint = "例：白屏十几秒，不知道在干嘛",
                value = current,
                onValueChange = { current = it },
            )
            Spacer(Modifier.height(10.dp))
            FeedbackField(
                label = "你希望变成什么样",
                hint = "例：先显示「正在加载」，别让我干等",
                value = expect,
                onValueChange = { expect = it },
            )
            Spacer(Modifier.height(10.dp))
            FeedbackField(
                label = "你的名字（可不填）",
                hint = "方便我知道是谁提的",
                value = name,
                onValueChange = { name = it },
                singleLine = true,
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(buildFeedbackText(scene, current, expect, name)))
                    copied = true
                },
                enabled = canCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Color.White,
                    disabledContainerColor = Divider,
                    disabledContentColor = SubInk,
                ),
            ) {
                Text(
                    text = when {
                        copied -> "已复制 ✓ 去群里粘贴吧"
                        canCopy -> "复制反馈内容"
                        else -> "先填一下上面的内容"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "每一条都会看。被采纳的会写进版本记录，也会出现在「关于」里。",
                fontSize = 11.sp,
                lineHeight = 17.sp,
                color = SubInk,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FeedbackField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(hint, fontSize = 12.sp, color = Divider) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Ink),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun buildFeedbackText(
    scene: String,
    current: String,
    expect: String,
    name: String,
): String {
    val sb = StringBuilder()
    sb.append("【B260608 站 · 反馈】\n")
    if (scene.isNotBlank()) sb.append("\n场景：").append(scene.trim())
    if (current.isNotBlank()) sb.append("\n现状：").append(current.trim())
    if (expect.isNotBlank()) sb.append("\n期望：").append(expect.trim())
    if (name.isNotBlank()) sb.append("\n\n—— ").append(name.trim())
    return sb.toString()
}
