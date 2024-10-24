package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.LayoutSettingTextBinding
import dev.arkbuilders.arkmemo.utils.gone
import dev.arkbuilders.arkmemo.utils.visible

class SettingTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {
    init {
        val binding = LayoutSettingTextBinding.inflate(LayoutInflater.from(context), this, true)
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SettingTextView)
        val textResId = typedArray.getText(R.styleable.SettingTextView_stv_text)
        val iconResId = typedArray.getResourceId(R.styleable.SettingTextView_stv_icon, 0)
        val enableSwitch =
            typedArray.getBoolean(R.styleable.SettingTextView_stv_switch_on, false)
        textResId?.let {
            binding.tvText.text = textResId
        }
        binding.ivIcon.setImageResource(iconResId)
        if (enableSwitch) {
            binding.switchRight.visible()
        } else {
            binding.switchRight.gone()
        }

        typedArray.recycle()
    }
}