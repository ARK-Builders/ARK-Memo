package dev.arkbuilders.arkmemo.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogCommonActionBinding.inflate(inflater)
        initViews()
        return mBinding.root
    }

    private fun initViews() {

        dialog?.setCanceledOnTouchOutside(false)

        if (isAlert) {
            mBinding.tvPositive.setTextAppearance(R.style.AlertButton)
            mBinding.tvPositive.setBackgroundResource(R.drawable.bg_red_button)
        }

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

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}