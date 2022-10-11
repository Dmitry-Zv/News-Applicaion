package by.zharikov.newsapplicaion.ui.setting

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.adapter.SettingAdapter
import by.zharikov.newsapplicaion.data.model.Settings
import by.zharikov.newsapplicaion.databinding.FragmentSettingBinding
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.utils.SwitchIconClickListener


class SettingFragment : Fragment(), SwitchIconClickListener {

    private var _binding: FragmentSettingBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var settingAdapter: SettingAdapter
    private val pref: SharedPreferences by lazy{
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingAdapter = SettingAdapter(
            Settings.settings,
            this,
            isChecked0 = pref.getBoolean("SettingBooleanPosition0", false),
            isChecked1 = pref.getBoolean("SettingBooleanPosition1", false)
        )
        mBinding.settingRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = settingAdapter
        }
    }

    override fun onSwitchIconClickListener(isChecked: Boolean, position: Int) {
        when (position) {
            0 -> {
                sharedViewModel.setStateIsCheckedForPosition0(isChecked)
                pref.edit().putBoolean("SettingBooleanPosition0", isChecked).apply()
            }
            1 -> {
                sharedViewModel.setStateIsCheckedForPosition1(isChecked)
                pref.edit().putBoolean("SettingBooleanPosition1", isChecked).apply()
            }
        }
    }
}