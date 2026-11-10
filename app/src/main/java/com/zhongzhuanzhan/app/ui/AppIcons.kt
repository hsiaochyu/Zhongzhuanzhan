package com.zhongzhuanzhan.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * JSON 里的 icon 字段 -> 图标。
 * 想加新图标：在这里加一行，然后 JSON 里写对应的 key。
 */
object AppIcons {

    fun of(key: String): ImageVector = when (key.trim().lowercase()) {
        "car" -> Icons.Outlined.DirectionsCar
        "mood" -> Icons.Outlined.Mood
        "sleep" -> Icons.Outlined.DarkMode
        "sport" -> Icons.Outlined.DirectionsRun
        "study" -> Icons.Outlined.MenuBook
        "shop" -> Icons.Outlined.Storefront
        "game" -> Icons.Outlined.SportsEsports
        "dance" -> Icons.Outlined.MusicNote
        "heal" -> Icons.Outlined.Spa
        "wedding" -> Icons.Outlined.Cake
        "baby" -> Icons.Outlined.ChildCare
        else -> Icons.Outlined.Public
    }
}
