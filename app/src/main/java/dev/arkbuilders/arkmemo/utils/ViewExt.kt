package dev.arkbuilders.arkmemo.utils

import android.view.MotionEvent
import android.view.View
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.showAlignTop
import dev.arkbuilders.arkmemo.R

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.setOnDebounceTouchListener(action: (v: View, e: MotionEvent) -> Unit) {
    var isTouched = false
    setOnTouchListener { v, e ->
        if (!isTouched) {
            isTouched = true
            action(v, e)
            postDelayed({ isTouched = false }, 500)
        }
        true

    }
}

fun View.showAvailabilityToolTip() {
    val balloon = Balloon.Builder(context)
        .setWidthRatio(1.0f)
        .setHeight(BalloonSizeSpec.WRAP)
        .setText(context.getString(R.string.tips_will_be_available_soon))
        .setTextColorResource(R.color.white)
        .setTextSize(12f)
        .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        .setArrowSize(10)
        .setArrowPosition(0.5f)
        .setPadding(12)
        .setCornerRadius(8f)
        .setWidthRatio(0.5f)
        .setBackgroundColorResource(R.color.warning_300)
        .setBalloonAnimation(BalloonAnimation.ELASTIC)
        .build()
    showAlignTop(balloon)
}