package com.zhongzhuanzhan.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
private const val APP_VERSION = "1.2-beta2"

@Composable
fun MeScreen(
    onReload: () -> Unit,
    account: WpsAccountState,
    onCheckAccount: () -> Unit,
    onClearWebLogin: () -> Unit,
) {
    var showAbout by remember { mutableStateOf(false) }

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
            )
        }

        item(key = "settings") {
            SettingsCard(
                onReload = onReload,
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
}

@Composable
private fun AccountCard(
    account: WpsAccountState,
    onCheck: () -> Unit,
    onClear: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)

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
                    imageVector = if (account.status == WpsAccountStatus.LOGGED_IN) {
                        Icons.Filled.Person
                    } else {
                        Icons.Filled.Lock
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = "WPS 账户",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = accountStatusText(account),
                    fontSize = 12.sp,
                    color = SubInk,
                )
            }
        }

        Row(modifier = Modifier.padding(top = 4.dp)) {
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

private fun accountStatusText(account: WpsAccountState): String = when (account.status) {
    WpsAccountStatus.IDLE, WpsAccountStatus.CHECKING -> "检测中…"
    WpsAccountStatus.LOGGED_IN ->
        account.displayName?.let { "已登录 · $it" } ?: "已登录 · 重启 App 仍在线"
    WpsAccountStatus.LOGGED_OUT -> "未登录 · 打开任意入口登录一次即可"
    WpsAccountStatus.TIMEOUT -> "检测超时 · 点「重新检测」试试"
}

@Composable
private fun SettingsCard(onReload: () -> Unit, onAbout: () -> Unit) {
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
            icon = Icons.Filled.Info,
            title = "关于中转站",
            subtitle = "链接配置与打包说明",
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
            text = "$APP_VERSION（测试版）",
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
        title = { Text("关于中转站") },
        text = {
            Text(
                "把常用网页收进一屏的入口站。\n\n" +
                    "链接配置：assets/links.json\n" +
                    "改完重新打包即可，不需要改代码。"
            )
        },
    )
}
