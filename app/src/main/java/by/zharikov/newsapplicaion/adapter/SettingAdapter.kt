package by.zharikov.newsapplicaion.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.Setting
import by.zharikov.newsapplicaion.utils.SwitchIconClickListener
import kotlinx.android.synthetic.main.item_setting.view.*

class SettingAdapter(
    private val settings: List<Setting>,
    private val switchIconClickListener: SwitchIconClickListener,
    private val isChecked0: Boolean,
    private val isChecked1: Boolean
) :
    RecyclerView.Adapter<SettingAdapter.SettingViewHolder>() {

    inner class SettingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        return SettingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val setting = settings[position]
        if (settings[position] == settings[0]) settings[0].switcher =
            holder.itemView.item_setting_switcher
        else settings[1].switcher = holder.itemView.item_setting_switcher
        if (settings[position] == settings[0]) settings[0].switcher?.isChecked = isChecked0
        else settings[1].switcher?.isChecked = isChecked1
        if (settings[1].switcher?.isChecked == true) {
            settings[0].switcher?.isChecked = false
            settings[0].switcher?.isEnabled = false
        } else {
            settings[0].switcher?.isEnabled = true
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) settings[0].switcher?.isChecked =
                true
            else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) settings[0].switcher?.isChecked =
                false
        }

        holder.itemView.apply {
            item_setting_text.text = setting.settingText
            item_setting_image.setImageResource(setting.resImage)
            item_setting_switcher.setOnCheckedChangeListener { _, isChecked ->

                if (settings[position] == settings[1]) {
                    if (settings[1].switcher?.isChecked == true) {
                        settings[0].switcher?.isChecked = false
                        settings[0].switcher?.isEnabled = false
                    } else {
                        settings[0].switcher?.isEnabled = true
                        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) settings[0].switcher?.isChecked =
                            true
                        else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) settings[0].switcher?.isChecked =
                            false
                    }

                }
                switchIconClickListener.onSwitchIconClickListener(isChecked, position)
            }
        }


    }

    override fun getItemCount() = settings.size
}