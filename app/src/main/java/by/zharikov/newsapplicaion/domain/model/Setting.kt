package by.zharikov.newsapplicaion.domain.model

import androidx.appcompat.widget.SwitchCompat
import by.zharikov.newsapplicaion.R

data class Setting(
    val settingText: String,
    val resImage: Int,
    var switcher: SwitchCompat?
)

object Settings {
    val settings = listOf<Setting>(
        Setting(
            "Apply Night Mode",
            R.drawable.ic_nights_24,
            switcher = null
        ),
        Setting(
            "Night Follow System",
            R.drawable.ic_settings_system,
            switcher = null
        )
    )
}