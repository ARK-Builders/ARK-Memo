package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentSettingsBinding
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.visible

open class SettingsFragmentV2: Fragment(R.layout.fragment_settings) {

    val binding by viewBinding(FragmentSettingsBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarCustom.ivBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.toolbarCustom.tvTitle.text = getString(R.string.settings)
        binding.toolbarCustom.tvTitle.visible()

        binding.toolbarCustom.tvRightActionText.gone()
        binding.toolbarCustom.ivRightActionIcon.gone()

    }
}