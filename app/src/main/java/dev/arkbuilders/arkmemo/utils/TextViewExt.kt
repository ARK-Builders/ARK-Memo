package dev.arkbuilders.arkmemo.utils

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat.getColor
import dev.arkbuilders.arkmemo.R

fun TextView.highlightWord(word: String) {

    val textString = this.text.toString()
    val wordToSpan = SpannableString(textString)
    val startIndex = textString.lowercase().indexOf(word.lowercase())

    if (startIndex == -1) return

    val endIndex = startIndex + word.length
    wordToSpan.setSpan(BackgroundColorSpan(
        getColor(this.context, R.color.warning_300)),
        startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = wordToSpan
}

fun TextView.setDrawableColor(@ColorInt color: Int) {
    compoundDrawables.filterNotNull().forEach {
        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}