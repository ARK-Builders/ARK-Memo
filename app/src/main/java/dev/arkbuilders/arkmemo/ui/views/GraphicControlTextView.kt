package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import dev.arkbuilders.arkmemo.R

class GraphicControlTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    var isSelectedState = false
    private var iconTintColor: Int

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.GraphicControlTextView)
        val drawableResId =
            typedArray.getResourceId(R.styleable.GraphicControlTextView_gct_drawable, 0)
        val isSelected =
            typedArray.getBoolean(R.styleable.GraphicControlTextView_gct_selected, false)
        iconTintColor = typedArray.getColor(R.styleable.GraphicControlTextView_gct_icon_tint, -1)

        drawableResId.let {
            this.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0)
        }
        setSelectState(isSelected)

        typedArray.recycle()
    }

    fun setSelectState(selected: Boolean) {
        if (selected) {
            this.background = ContextCompat.getDrawable(
                context, R.drawable.bg_graphic_control_text_selected
            )
            val selectedColor = ContextCompat.getColor(context, R.color.warning_700)

            this.setTextColor(selectedColor)
            setDrawableTint(selectedColor)
        } else {
            this.background = ContextCompat.getDrawable(
                context, R.drawable.bg_border_r8
            )
            val selectedColor = ContextCompat.getColor(context, R.color.text_tertiary)
            val drawableColor = if (iconTintColor != -1) iconTintColor else selectedColor

            this.setTextColor(selectedColor)
            setDrawableTint(drawableColor)
        }

        isSelectedState = selected
    }

    private fun setDrawableTint(@ColorInt color: Int) {
        if (iconTintColor == -1) {
            TextViewCompat.setCompoundDrawableTintList(this,
                ColorStateList.valueOf(color)
            )
        }
    }
}