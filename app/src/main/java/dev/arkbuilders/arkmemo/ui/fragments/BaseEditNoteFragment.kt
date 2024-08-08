package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.FragmentEditNotesBinding

open class BaseEditNoteFragment : Fragment(R.layout.fragment_edit_notes) {

    val binding by viewBinding(FragmentEditNotesBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDescription.setOnClickListener {
            if (binding.editTextDescription.visibility == View.GONE) {
                binding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_down, 0)
                binding.editTextDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_right, 0)
                binding.editTextDescription.visibility = View.GONE
            }
        }
    }
}
