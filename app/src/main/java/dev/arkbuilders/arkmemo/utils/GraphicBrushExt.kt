package dev.arkbuilders.arkmemo.utils

import dev.arkbuilders.arkmemo.graphics.Color
import dev.arkbuilders.arkmemo.graphics.Size

fun Int.getStrokeSize(): Float {
    return when(this) {
        Size.TINY.id   -> Size.TINY.value
        Size.SMALL.id  -> Size.SMALL.value
        Size.MEDIUM.id -> Size.MEDIUM.value
        Size.LARGE.id  -> Size.LARGE.value
        Size.HUGE.id   -> Size.HUGE.value
        else           -> Size.TINY.value
    }
}

fun Float.getBrushSizeId(): Int {
    return when(this) {
        Size.TINY.value -> Size.TINY.id
        Size.SMALL.value -> Size.SMALL.id
        Size.MEDIUM.value -> Size.MEDIUM.id
        Size.LARGE.value -> Size.LARGE.id
        Size.HUGE.value -> Size.HUGE.id
        else -> { Size.TINY.id }
    }
}

fun Int.getStrokeColor(): String {
    return when (this) {
        Color.BLACK.code  -> Color.BLACK.value
        Color.GRAY.code   -> Color.GRAY.value
        Color.RED.code    -> Color.RED.value
        Color.GREEN.code  -> Color.GREEN.value
        Color.BLUE.code   -> Color.BLUE.value
        Color.PURPLE.code -> Color.PURPLE.value
        Color.ORANGE.code -> Color.ORANGE.value
        else              -> Color.BLACK.value
    }
}

fun String.getColorCode(): Int {
    return when (this) {
        Color.BLACK.value  -> Color.BLACK.code
        Color.GRAY.value   -> Color.GRAY.code
        Color.RED.value    -> Color.RED.code
        Color.GREEN.value  -> Color.GREEN.code
        Color.BLUE.value   -> Color.BLUE.code
        Color.PURPLE.value -> Color.PURPLE.code
        Color.ORANGE.value -> Color.ORANGE.code
        else               -> Color.BLACK.code
    }
}