package dev.arkbuilders.arkmemo.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.DialogCommonActionBinding

/**
 * This is a common action dialog that can be used inside app.
 * It's a basic dialog with customizable title, message, one positive button and one negative button
 */
class CommonActionDialog(@StringRes private val title: Int,
                         @StringRes private val message: Int,
                         @StringRes private val positiveText: Int,
                         @StringRes private val negativeText: Int,
                         private val isAlert: Boolean = false,
                         private val onPositiveClick: (() -> Unit)? = null,
                         private val onNegativeClicked: (() -> Unit)? = null,
                         private val onCloseClicked: (() -> Unit)? = null): DialogFragment() {

    companion object {
        val TAG = CommonActionDialog::class.java.name
    }
    private lateinit var mBinding: DialogCommonActionBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mBinding = DialogCommonActionBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext(), R.style.MemoDialog)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setContentView(mBinding.root)
        if (isAlert) {
            mBinding.tvPositive.setTextAppearance(R.style.AlertButton)
            mBinding.tvPositive.setBackgroundResource(R.drawable.bg_red_button)
        }
        initViews()
        return dialog
    }

    private fun initViews() {
        mBinding.tvTitle.setText(title)
        mBinding.tvMessage.setText(message)
        mBinding.tvPositive.setText(positiveText)
        mBinding.tvNegative.setText(negativeText)
        mBinding.ivClose.setOnClickListener {
            onCloseClicked?.invoke()
            dismiss()
        }

        mBinding.tvPositive.setOnClickListener {
            onPositiveClick?.invoke()
            dismiss()
        }

        mBinding.tvNegative.setOnClickListener {
            onNegativeClicked?.invoke()
            dismiss()
        }
    }

}