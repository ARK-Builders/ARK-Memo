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

class SettingTextView
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {
        private var binding: LayoutSettingTextBinding
        var onSwitchCheckChanged: ((isChecked: Boolean) -> Unit)? = null

        init {
            binding = LayoutSettingTextBinding.inflate(LayoutInflater.from(context), this, true)
            val typedArray: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.SettingTextView)
            val textResId = typedArray.getText(R.styleable.SettingTextView_stv_text)
            val iconResId = typedArray.getResourceId(R.styleable.SettingTextView_stv_icon, 0)
            val enableSwitch =
                typedArray.getBoolean(R.styleable.SettingTextView_stv_switch_on, false)
            val switchChecked = typedArray.getBoolean(R.styleable.SettingTextView_stv_switch_checked, false)
            textResId?.let {
                binding.tvText.text = textResId
            }
            binding.ivIcon.setImageResource(iconResId)
            if (enableSwitch) {
                binding.switchRight.visible()
            } else {
                binding.switchRight.gone()
            }

            binding.switchRight.isChecked = switchChecked
            binding.switchRight.setOnCheckedChangeListener(
                object : SwitchButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(
                        view: SwitchButton?,
                        isChecked: Boolean,
                    ) {
                        onSwitchCheckChanged?.invoke(isChecked)
                    }
                },
            )

            typedArray.recycle()
        }

        fun setSwitchChecked(checked: Boolean) {
            binding.switchRight.isChecked = checked
        }
    }
