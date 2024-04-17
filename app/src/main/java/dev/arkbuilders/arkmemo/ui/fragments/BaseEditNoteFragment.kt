package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesV2Binding
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.visible

open class BaseEditNoteFragment: Fragment(R.layout.fragment_edit_notes_v2) {

    val binding by viewBinding(FragmentEditNotesV2Binding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDescription.setOnClickListener {
            if (binding.editTextDescription.visibility == View.GONE) {
                binding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_chevron_down, 0
                )
                binding.editTextDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_chevron_right, 0
                )
                binding.editTextDescription.visibility = View.GONE
            }
        }

        binding.toolbar.ivBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        if (this is ArkMediaPlayerFragment) {
            binding.toolbar.ivRightActionIcon.gone()
            binding.toolbar.tvRightActionText.visible()
            binding.groupTextControls.gone()
            binding.layoutGraphicsControl.root.gone()
            binding.layoutAudioRecord.root.visible()
        } else {
            binding.toolbar.ivRightActionIcon.visible()
            binding.toolbar.tvRightActionText.gone()
            binding.layoutAudioRecord.root.gone()

            if (this is EditTextNotesFragment) {
                binding.layoutGraphicsControl.root.gone()
                binding.groupTextControls.visible()
            } else {
                binding.layoutGraphicsControl.root.visible()
                binding.groupTextControls.gone()
            }
        }

        if (this is EditGraphicNotesFragment) {
            binding.toolbar.tvRightActionText.visible()
            binding.toolbar.ivRightActionIcon.gone()
        } else {
            binding.toolbar.tvRightActionText.gone()
            binding.toolbar.ivRightActionIcon.visible()
        }
    }
}