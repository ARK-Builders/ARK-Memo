package dev.arkbuilders.arkmemo.ui.adapters

sealed interface BrushAttribute {
    var isSelected: Boolean
}

sealed class BrushSize : BrushAttribute {
    override var isSelected = false
}

sealed class BrushColor : BrushAttribute {
    override var isSelected = false
}

data object BrushSizeTiny : BrushSize()
data object BrushSizeSmall : BrushSize()
data object BrushSizeMedium : BrushSize()
data object BrushSizeLarge : BrushSize()
data object BrushSizeHuge : BrushSize()

data object BrushColorBlack : BrushColor()
data object BrushColorGrey : BrushColor()
data object BrushColorRed : BrushColor()
data object BrushColorOrange : BrushColor()
data object BrushColorGreen : BrushColor()
data object BrushColorBlue : BrushColor()
data object BrushColorPurple : BrushColor()