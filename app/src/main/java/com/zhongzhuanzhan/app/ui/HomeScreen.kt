package com.zhongzhuanzhan.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhongzhuanzhan.app.data.AppConfig
import com.zhongzhuanzhan.app.data.Banner
import com.zhongzhuanzhan.app.data.LinkItem
import com.zhongzhuanzhan.app.data.RecentStore

@Composable
fun HomeScreen(
    config: AppConfig,
    recent: Pair<String, String>?,
    onOpenUrl: (title: String, url: String) -> Unit,
    onReopen: () -> Unit,
    onExit: () -> Unit,
    onReload: () -> Unit,
) {
    var showAbout by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to PageTop,
                    0.32f to PageMid,
                    1f to Color.White,
                )
            )
    ) {
        GlowBackdrop()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 106.dp),
        ) {
            item(key = "topbar") {
                HomeTopBar(
                    title = config.appName,
                    onExit = onExit,
                    onReload = onReload,
                    onAbout = { showAbout = true },
                )
            }

            item(key = "banner") {
                BannerCard(
                    banner = config.banner,
                    onClick = {
                        if (config.banner.url.isNotBlank()) {
                            onOpenUrl(config.banner.accent, config.banner.url)
                        }
                    },
                )
            }

            config.sections.forEach { section ->
                item(key = "header_${section.title}") {
                    SectionHeader(section.title)
                }

                val rows = if (section.style == "compact") {
                    section.items.chunked(3)
                } else {
                    section.items.chunked(2)
                }

                items(
                    items = rows,
                    key = { row -> "row_${section.title}_${row.firstOrNull()?.id ?: row.hashCode()}" },
                ) { row ->
                    if (section.style == "compact") {
                        CompactRow(row) { onOpenUrl(it.title, it.url) }
                    } else {
                        GridRow(row) { onOpenUrl(it.title, it.url) }
                    }
                }
            }
        }

        RecentBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp),
            recent = recent,
            onReopen = onReopen,
        )
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}

@Composable
private fun GlowBackdrop() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x33C7B7FF), Color(0x00C7B7FF))),
            radius = size.width * 0.55f,
            center = Offset(size.width * 0.85f, -size.height * 0.08f),
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x22BFD8FF), Color(0x00BFD8FF))),
            radius = size.width * 0.5f,
            center = Offset(size.width * 0.02f, size.height * 0.6f),
        )
    }
}

@Composable
private fun HomeTopBar(
    title: String,
    onExit: () -> Unit,
    onReload: () -> Unit,
    onAbout: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "返回",
                tint = Ink,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Ink,
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        ) {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "菜单",
                    tint = Ink,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("重新载入配置") },
                    onClick = {
                        menuOpen = false
                        onReload()
                    },
                )
                DropdownMenuItem(
                    text = { Text("关于") },
                    onClick = {
                        menuOpen = false
                        onAbout()
                    },
                )
            }
        }
    }
}

@Composable
private fun BannerCard(banner: Banner, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp)
            .height(104.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFE9F1FF), Color(0xFFF5F0FF), Color(0xFFFDF3FA))
                )
            )
            .then(if (banner.url.isNotBlank()) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        CloudArt(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(140.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
                .fillMaxWidth(0.66f)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = banner.title,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    color = Accent,
                )
                Text(
                    text = banner.accent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    modifier = Modifier.padding(start = 6.dp, bottom = 3.dp),
                )
            }
            Spacer(Modifier.height(7.dp))
            Text(
                text = banner.subtitle,
                fontSize = 11.sp,
                color = SubInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CloudArt(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66B9D4FF), Color(0x00B9D4FF)),
                center = Offset(w * 0.55f, h * 0.45f),
                radius = w * 0.45f,
            ),
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = Size(w * 0.82f, h * 0.62f),
        )

        drawCircle(Color(0xCCFFFFFF), radius = w * 0.15f, center = Offset(w * 0.45f, h * 0.62f))
        drawCircle(Color(0xD9FFFFFF), radius = w * 0.19f, center = Offset(w * 0.60f, h * 0.53f))
        drawCircle(Color(0xCCFFFFFF), radius = w * 0.12f, center = Offset(w * 0.73f, h * 0.64f))
        drawRoundRect(
            color = Color(0xCCFFFFFF),
            topLeft = Offset(w * 0.36f, h * 0.62f),
            size = Size(w * 0.44f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.07f),
        )
        drawCircle(Color(0x55A9C8FF), radius = w * 0.035f, center = Offset(w * 0.28f, h * 0.40f))
        drawCircle(Color(0x44A9C8FF), radius = w * 0.024f, center = Offset(w * 0.80f, h * 0.36f))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FadeLine(reverse = true)
        Text(
            text = title,
            fontSize = 12.sp,
            color = SubInk,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
        FadeLine(reverse = false)
    }
}

@Composable
private fun FadeLine(reverse: Boolean) {
    val colors = if (reverse) {
        listOf(Color(0x00D8D8DE), Divider)
    } else {
        listOf(Divider, Color(0x00D8D8DE))
    }
    Box(
        modifier = Modifier
            .size(width = 26.dp, height = 1.dp)
            .background(Brush.horizontalGradient(colors))
    )
}

@Composable
private fun GridRow(row: List<LinkItem>, onClick: (LinkItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        row.forEach { item ->
            LinkCard(
                item = item,
                modifier = Modifier.weight(1f),
                onClick = { onClick(item) },
            )
        }
        if (row.size == 1) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun LinkCard(item: LinkItem, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .height(88.dp)
            .shadow(10.dp, shape, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(shape)
            .background(Color.White)
            .border(0.5.dp, Color(0x0D000000), shape)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = item.subtitle,
                fontSize = 11.sp,
                color = SubInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(IconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.of(item.icon),
                contentDescription = null,
                tint = Color(0xFF3A3D42),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun CompactRow(row: List<LinkItem>, onClick: (LinkItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        row.forEach { item ->
            CompactCard(
                item = item,
                modifier = Modifier.weight(1f),
                onClick = { onClick(item) },
            )
        }
        repeat(3 - row.size) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun CompactCard(item: LinkItem, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .height(78.dp)
            .shadow(8.dp, shape, ambientColor = Color(0x12000000), spotColor = Color(0x12000000))
            .clip(shape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
    ) {
        Text(
            text = item.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            maxLines = 1,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.subtitle,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            color = SubInk,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecentBar(
    modifier: Modifier,
    recent: Pair<String, String>?,
    onReopen: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val hasRecent = recent != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .shadow(14.dp, shape, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFB9C8E8), Color(0xFF8CA4CE)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Public,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (recent != null) recent.first else "还没有访问记录",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (hasRecent) Ink else SubInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (recent != null) RecentStore.prettyHost(recent.second) else "点上面的卡片开始",
                fontSize = 11.sp,
                color = SubInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onReopen, enabled = hasRecent) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "再次打开",
                tint = if (hasRecent) Ink else SubInk,
            )
        }
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
