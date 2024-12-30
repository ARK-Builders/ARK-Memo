package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentSettingsBinding
import dev.arkbuilders.arkmemo.ui.viewmodels.SettingsViewModel
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.visible

@AndroidEntryPoint
open class SettingsFragment : Fragment(R.layout.fragment_settings) {
    val binding by viewBinding(FragmentSettingsBinding::bind)
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarCustom.ivBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.toolbarCustom.tvTitle.text = getString(R.string.settings)
        binding.toolbarCustom.tvTitle.visible()

        binding.toolbarCustom.tvRightActionText.gone()
        binding.toolbarCustom.ivRightActionIcon.gone()

        initSettingActions()
    }

    private fun initSettingActions() {
        binding.tvCrashReport.setSwitchChecked(settingsViewModel.getCrashReportEnabled())
        binding.tvCrashReport.onSwitchCheckChanged = { isChecked ->
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = isChecked
            settingsViewModel.storeCrashReportEnabled(isChecked)
        }
    }
}
