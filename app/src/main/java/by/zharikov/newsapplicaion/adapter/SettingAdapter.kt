package by.zharikov.newsapplicaion.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import by.zharikov.newsapplicaion.databinding.ItemSettingBinding
import by.zharikov.newsapplicaion.domain.model.Setting
import by.zharikov.newsapplicaion.utils.SwitchIconClickListener

class SettingAdapter(
    private val settings: List<Setting>,
    private val switchIconClickListener: SwitchIconClickListener,

    ) :
    RecyclerView.Adapter<SettingAdapter.SettingViewHolder>() {

    inner class SettingViewHolder(val binding: ItemSettingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val setting = settings[position]
        if (settings[position] == settings[0]) settings[0].switcher =
            holder.binding.itemSettingSwitcher
        else settings[1].switcher = holder.binding.itemSettingSwitcher
        if (settings[position] == settings[0]) settings[0].switcher?.isChecked = false
        else settings[1].switcher?.isChecked = false
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

        holder.binding.apply {
            itemSettingText.text = setting.settingText
            itemSettingImage.setImageResource(setting.resImage)
            itemSettingSwitcher.setOnCheckedChangeListener { _, isChecked ->

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