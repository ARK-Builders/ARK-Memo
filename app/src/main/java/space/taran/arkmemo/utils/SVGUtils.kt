package space.taran.arkmemo.utils

import space.taran.arkmemo.utils.SVG.Tags.viewBox

class SVG {
    var strokeColor = "${QUOTE}black$QUOTE"
    private var d = ""

    fun writeData(command: String) {
        d += "$SPACE$command"
    }

    fun moveTo(x: Float, y: Float) = "M$SPACE$x,$y"

    fun relativeLineTo(x: Float, y: Float) = "l$SPACE$x,$y"

    fun quadraticBezierTo(x1: Float, y1: Float, x2: Float, y2: Float) = "q$SPACE$x1,$y1$SPACE$x2,$y2"

    fun setViewBox(width: Float, height: Float) {
        viewBox = "${QUOTE}0${SPACE}0$SPACE$width $height$QUOTE"
    }

    fun get() = "${Tags.open}$NEWLINE${Tags.openPath}$SPACE${Attributes.Path.STROKE}$ASSIGN" +
            "$strokeColor$SPACE${Attributes.Path.FILL}$ASSIGN$fill$SPACE${Attributes.Path.DATA}" +
            "$ASSIGN${QUOTE}$d$QUOTE${Tags.closePath}$NEWLINE${Tags.close}"

    companion object {
        private const val NEWLINE = "\n"
        private const val SPACE = " "
        private const val ASSIGN = "="
        private const val QUOTE = "\""
        private const val XML_NS = "${QUOTE}http://www.w3.org/2000/svg$QUOTE"
        private const val fill = "${QUOTE}none$QUOTE"
    }

    private object Tags {
        var viewBox = "${QUOTE}0${SPACE}0${SPACE}100${SPACE}100$QUOTE"
        val open = "<svg$SPACE$viewBox$SPACE${Attributes.XML_NAMESPACE}${ASSIGN}$XML_NS>"
        const val close = "</svg>"
        const val openPath = "<path"
        const val closePath = "/>"
    }

    private object Attributes {
        const val XML_NAMESPACE = "xmlns"
        object Path {
            const val STROKE = "stroke"
            const val FILL = "fill"
            const val DATA = "d"
        }
    }
}