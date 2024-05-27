package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.LayoutSupportTextBinding
import dev.arkbuilders.arkmemo.utils.gone

class SupportTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {
    init {
        val binding = LayoutSupportTextBinding.inflate(LayoutInflater.from(context), this, true)
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SupportTextView)
        val textResId = typedArray.getText(R.styleable.SupportTextView_support_text)
        val iconResId = typedArray.getResourceId(R.styleable.SupportTextView_support_icon, 0)
        val enabled = typedArray.getBoolean(R.styleable.SupportTextView_support_enabled, true)
        textResId?.let {
            binding.tvText.text = textResId
        } ?: let {
            binding.tvText.gone()
            binding.ivIcon.gone()
        }

        if (iconResId != 0) {
            binding.ivIcon.setImageResource(iconResId)
        } else {
            binding.ivIcon.gone()
        }

        if (!enabled) {
            binding.tvText.setTextColor(ContextCompat.getColor(context, R.color.gray_400))
            binding.tvText.isEnabled = false
        }

        typedArray.recycle()
    }
}