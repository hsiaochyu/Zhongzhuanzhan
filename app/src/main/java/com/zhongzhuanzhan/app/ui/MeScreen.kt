package com.zhongzhuanzhan.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhongzhuanzhan.app.data.WpsAccountState
import com.zhongzhuanzhan.app.data.WpsAccountStatus

// 与 build.gradle.kts 的 versionName 保持一致（改版本时两处都要动）
private const val APP_VERSION = "1.3"

@Composable
fun MeScreen(
    onReload: () -> Unit,
    account: WpsAccountState,
    onCheckAccount: () -> Unit,
    onClearWebLogin: () -> Unit,
    onLoginWps: () -> Unit,
) {
    var showAbout by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onCheckAccount() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "title") {
            Text(
                text = "我的",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 14.dp),
            )
        }

        item(key = "account") {
            AccountCard(
                account = account,
                onCheck = onCheckAccount,
                onClear = onClearWebLogin,
                onLogin = onLoginWps,
            )
        }

        item(key = "settings") {
            SettingsCard(
                onReload = onReload,
                onFeedback = { showFeedback = true },
                onAbout = { showAbout = true },
            )
        }

        item(key = "version") {
            VersionCard()
        }
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    if (showFeedback) {
        FeedbackDialog(onDismiss = { showFeedback = false })
    }
}

@Composable
private fun AccountCard(
    account: WpsAccountState,
    onCheck: () -> Unit,
    onClear: () -> Unit,
    onLogin: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    val loggedIn = account.status == WpsAccountStatus.LOGGED_IN

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 14.dp)
            .shadow(10.dp, shape, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFB9C8E8), Color(0xFF8CA4CE)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (loggedIn) Icons.Filled.Person else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = "WPS 账户",
                    fontSize = 11.sp,
                    color = SubInk,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = accountMainText(account),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (loggedIn) Ink else SubInk,
                )
                if (loggedIn) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "已登录 · 重启 App 仍在线",
                        fontSize = 11.sp,
                        color = SubInk,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (!loggedIn && account.status == WpsAccountStatus.LOGGED_OUT) {
                TextButton(onClick = onLogin) {
                    Text("登录 WPS 账号", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            TextButton(onClick = onCheck) {
                Text("重新检测", fontSize = 12.sp)
            }
            TextButton(onClick = { confirmClear = true }) {
                Text("清除网页登录数据", fontSize = 12.sp, color = Color(0xFFB0543F))
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    onClear()
                }) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            },
            title = { Text("清除网页登录数据？") },
            text = {
                Text(
                    "会退出 WPS 登录，并清掉各入口已记住的密码状态（含论坛等其它网页的登录数据）。之后打开任意入口重新登录一次即可恢复。"
                )
            },
        )
    }
}

private fun accountMainText(account: WpsAccountState): String = when (account.status) {
    WpsAccountStatus.IDLE, WpsAccountStatus.CHECKING -> "检测中…"
    WpsAccountStatus.LOGGED_IN -> account.displayName ?: "已登录"
    WpsAccountStatus.LOGGED_OUT -> "未登录"
    WpsAccountStatus.TIMEOUT -> "检测超时"
}

@Composable
private fun SettingsCard(
    onReload: () -> Unit,
    onFeedback: () -> Unit,
    onAbout: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(8.dp, shape, ambientColor = Color(0x12000000), spotColor = Color(0x12000000))
            .clip(shape)
            .background(Color.White),
    ) {
        SettingRow(
            icon = Icons.Filled.Refresh,
            title = "重新载入配置",
            subtitle = "重新读取配置并刷新界面",
            onClick = onReload,
            showDivider = true,
        )
        SettingRow(
            icon = Icons.Filled.Feedback,
            title = "反馈",
            subtitle = "想加新入口、发现打不开、觉得哪里不好用",
            onClick = onFeedback,
            showDivider = true,
        )
        SettingRow(
            icon = Icons.Filled.Info,
            title = "关于「探索 AI 世界」",
            subtitle = "这个 App 是干嘛的",
            onClick = onAbout,
            showDivider = false,
        )
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF3A3D42),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SubInk,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Divider,
                modifier = Modifier.size(20.dp),
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 68.dp)
                    .height(0.5.dp)
                    .background(Divider)
            )
        }
    }
}

@Composable
private fun VersionCard() {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp)
            .shadow(8.dp, shape, ambientColor = Color(0x12000000), spotColor = Color(0x12000000))
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Tag,
                    contentDescription = null,
                    tint = Color(0xFF3A3D42),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "版本",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
            )
        }

        Text(
            text = APP_VERSION,
            fontSize = 13.sp,
            color = SubInk,
        )
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了") }
        },
        title = { Text("B260608 站 · 探索 AI 世界") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                AboutSection(
                    "为什么要做这个 App",
                    "前些天我们用 AI 搭了一批应用，可惜它们都住在聊天记录里——消息一刷，" +
                        "链接就被顶上去再也找不到了（校园论坛一直没人讨论，多半也是这个原因）。\n\n" +
                        "与其在群里考古，不如把它们装进一个 App，点一下即达。",
                )
                AboutSection(
                    "里面有什么",
                    "我们班用智能体搭建的各类网站，一站式直达：学搭子、活动管家、考勤助手、" +
                        "百事通、四六级、摄影展示、个人信息中心……不用记链接，不用翻聊天记录。\n\n" +
                        "App 本体：Kotlin + Jetpack Compose，Android 8.0 及以上可装。",
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "~~那这个 App 是怎么打的包？当然也是用 WPS AI 😂~~",
                    fontSize = 12.sp,
                    color = SubInk,
                )
            }
        },
    )
}

@Composable
private fun AboutSection(title: String, body: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Ink,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = body,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = SubInk,
    )
    Spacer(Modifier.height(12.dp))
}
