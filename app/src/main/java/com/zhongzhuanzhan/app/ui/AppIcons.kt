package com.zhongzhuanzhan.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CastForEducation
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * JSON 里的 icon 字段 -> 图标。
 * 想加新图标：在这里加一行，然后 JSON 里写对应的 key。
 * 可选值：portal/camera/bulb/calendar/person/forum/book/graduation/celebration/blackboard
 *        （旧场景：car/mood/sleep/sport/study/shop/game/dance/heal/wedding/baby）
 */
object AppIcons {

    fun of(key: String): ImageVector = when (key.trim().lowercase()) {
        // 新版：智能体入口
        "portal" -> Icons.Outlined.Explore            // 智能体导航门户：罗盘
        "camera" -> Icons.Outlined.PhotoCamera        // 摄影展示：相机
        "bulb" -> Icons.Outlined.Lightbulb            // 百事通：问答/点子
        "calendar" -> Icons.Outlined.EventAvailable   // 考勤助手：日历+勾
        "person" -> Icons.Outlined.Person             // 个人信息：个人中心
        "forum" -> Icons.Outlined.Forum               // 校园论坛：对话气泡
        "book" -> Icons.Outlined.AutoStories          // 学搭子：翻开的书
        "graduation" -> Icons.Outlined.School         // 四六级：学士帽
        "celebration" -> Icons.Outlined.Celebration   // 活动管家：庆祝彩带
        "blackboard" -> Icons.Outlined.CastForEducation // 小黑板：带支架的黑板

        // 旧场景（示例配置兼容）
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
