package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.databinding.LayoutWebLinkTextBinding
import dev.arkbuilders.arkmemo.utils.gone

class WebLinkTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    init {
        val binding = LayoutWebLinkTextBinding.inflate(LayoutInflater.from(context), this, true)
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.WebLinkTextView)
        val textResId = typedArray.getText(R.styleable.WebLinkTextView_web_link_text)
        val iconResId = typedArray.getResourceId(R.styleable.WebLinkTextView_web_link_icon, 0)
        textResId?.let {
            binding.tvText.text = textResId
        }

        if (iconResId != 0) {
            binding.ivIcon.setImageResource(iconResId)
        } else {
            binding.ivIcon.gone()
        }

        typedArray.recycle()
    }
}