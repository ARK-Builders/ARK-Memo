package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.BuildConfig
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentSettingsBinding
import dev.arkbuilders.arkmemo.ui.dialogs.CommonActionDialog
import dev.arkbuilders.arkmemo.ui.dialogs.DonateDialog
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.openLink
import dev.arkbuilders.arkmemo.utils.visible

open class SettingsFragment : Fragment(R.layout.fragment_settings) {
    val binding by viewBinding(FragmentSettingsBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarCustom.ivBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.toolbarCustom.tvTitle.text = getString(R.string.about)
        binding.toolbarCustom.tvTitle.visible()

        binding.toolbarCustom.tvRightActionText.gone()
        binding.toolbarCustom.ivRightActionIcon.gone()

        binding.tvAppVersion.text = getString(R.string.setting_app_version, BuildConfig.VERSION_NAME)

        initSettingActions()
    }

    private fun initSettingActions() {
        binding.tvWebsite.setOnClickListener {
            context?.openLink("https://www.ark-builders.dev/")
        }

        binding.tvTelegram.setOnClickListener {
            context?.openLink("https://t.me/ark_builders")
        }

        binding.tvDiscord.setOnClickListener {
            context?.openLink("https://discord.gg/tPUJTxud")
        }

        binding.tvDonatePatreon.setOnClickListener {
            context?.openLink("https://www.patreon.com/ARKBuilders")
        }

        binding.tvDonateCoffee.setOnClickListener {
            context?.openLink("https://buymeacoffee.com/arkbuilders")
        }

        binding.tvDonateBtc.setOnClickListener {
            DonateDialog(
                walletAddress = "bc1qx8n9r4uwpgrhgnamt2uew53lmrxd8tuevp7lv5",
                title = getString(R.string.setting_donate_btc),
                onPositiveClick = {
                },
            ).show(childFragmentManager, CommonActionDialog.TAG)
        }

        binding.tvDonateEth.setOnClickListener {
            DonateDialog(
                walletAddress = "0x9765C5aC38175BFbd2dC7a840b63e50762B80a1b",
                title = getString(R.string.setting_donate_eth),
                onPositiveClick = {
                },
            ).show(childFragmentManager, CommonActionDialog.TAG)
        }

        binding.tvDiscoverIssues.setOnClickListener {
            context?.openLink("https://www.ark-builders.dev/contribute/?tab=goodFirstIssue")
        }

        binding.tvBounties.setOnClickListener {
        }
    }
}
