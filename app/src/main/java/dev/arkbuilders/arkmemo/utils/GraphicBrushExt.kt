package dev.arkbuilders.arkmemo.utils

import dev.arkbuilders.arkmemo.graphics.Color
import dev.arkbuilders.arkmemo.graphics.Size
import dev.arkbuilders.arkmemo.ui.adapters.BrushColor
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlack
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorBlue
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGreen
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorGrey
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorOrange
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorPurple
import dev.arkbuilders.arkmemo.ui.adapters.BrushColorRed
import dev.arkbuilders.arkmemo.ui.adapters.BrushSize
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeHuge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeLarge
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeMedium
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeSmall
import dev.arkbuilders.arkmemo.ui.adapters.BrushSizeTiny

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
        Color.WHITE.code  -> Color.WHITE.value
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
        Color.WHITE.value  -> Color.WHITE.code
        else               -> Color.BLACK.code
    }
}

fun BrushColor.getColorCode(): Int {
    return when (this) {
        is BrushColorBlack -> Color.BLACK.code
        is BrushColorGrey -> Color.GRAY.code
        is BrushColorRed -> Color.RED.code
        is BrushColorOrange -> Color.ORANGE.code
        is BrushColorGreen -> Color.GREEN.code
        is BrushColorBlue -> Color.BLUE.code
        is BrushColorPurple -> Color.PURPLE.code
    }
}

fun BrushSize.getBrushSize(): Float {
    return when(this) {
        is BrushSizeTiny -> Size.TINY.value
        is BrushSizeSmall -> Size.SMALL.value
        is BrushSizeMedium -> Size.MEDIUM.value
        is BrushSizeLarge -> Size.LARGE.value
        is BrushSizeHuge -> Size.HUGE.value
    }
}