package space.taran.arkmemo.utils

import android.graphics.Path
import android.util.Log
import space.taran.arkmemo.data.viewmodels.DrawPath
import space.taran.arkmemo.utils.SVG.Tags.viewBox
import java.util.Stack

class SVG {
    var strokeColor = "${QUOTE}black$QUOTE"
    private var d = ""

    fun writeData(command: String) {
        d += "$SPACE$command"
    }

    fun moveTo(x: Float, y: Float) = "M$SPACE$x,$y"

    fun relativeLineTo(x: Float, y: Float) = "L$SPACE$x,$y"

    fun quadraticBezierTo(x1: Float, y1: Float, x2: Float, y2: Float) = "Q$SPACE$x1,$y1$SPACE$x2,$y2"

    fun setViewBox(width: Float, height: Float) {
        viewBox = "${QUOTE}0${SPACE}0$SPACE$width$SPACE$height$QUOTE"
        Tags.updateOpen()
    }

    fun get() = "${Tags.open}$NEWLINE${Tags.openPath}$SPACE${Attributes.Path.STROKE}$ASSIGN" +
            "$strokeColor$SPACE${Attributes.Path.FILL}$ASSIGN$fill$SPACE${Attributes.Path.DATA}" +
            "$ASSIGN${QUOTE}$d$QUOTE${Tags.closePath}$NEWLINE${Tags.close}"

    fun getPaths(): Stack<Path> {
        val paths = Stack<Path>()
        if (d.isNotEmpty()) {
            var path = Path()
            var commandsString = d.removeSurrounding(QUOTE)
            val numberOfCommands = commandsString.filter {
                it.isLetter()
            }.length
            Log.d("svg-utils", "$numberOfCommands commands available")
            for (index in 1..numberOfCommands) {
                when(commandsString.substringAfter(SPACE).substringBefore(SPACE)) {
                    MOVE_TO -> {
                        path = Path()
                        val point = commandsString.substringAfter("$MOVE_TO$SPACE")
                            .substringBefore(SPACE)
                        val pointList = point.split(COMMA)
                        val x = pointList[0].toFloat(); val y = pointList[1].toFloat()
                        Log.d("svg-utils", "move to $point")
                        commandsString = commandsString.removePrefix("$SPACE$MOVE_TO$SPACE$point")
                        path.moveTo(x, y)
                        paths.add(path)
                    }
                    ABS_QUAD_TO -> {
                        val point1 = commandsString.substringAfter("$ABS_QUAD_TO$SPACE")
                            .substringBefore(SPACE)
                        commandsString = commandsString.removePrefix("$SPACE$ABS_QUAD_TO$SPACE$point1")
                        val point2 = commandsString.substringAfter(SPACE).substringBefore(SPACE)
                        commandsString = commandsString.removePrefix("$SPACE$point2")
                        val point1List = point1.split(COMMA)
                        val point2List = point2.split(COMMA)
                        val x1 = point1List[0].toFloat(); val y1 = point1List[1].toFloat()
                        val x2 = point2List[0].toFloat(); val y2 = point2List[1].toFloat()
                        Log.d("svg-utils", "quad to $point1 $point2")
                        path.quadTo(x1, y1, x2, y2)
                    }
                    ABS_LINE_TO -> {}
                }
            }
        }
        return paths
    }

    fun getTestPaths(): Stack<Path> {
        val paths = Stack<Path>()
        if (this.d.isNotEmpty()) {
            var path = Path()
            var commandsString = d.removeSurrounding(SVG.QUOTE)
            val numberOfCommands = commandsString.filter {
                it.isLetter()
            }.length
            for (index in 1..numberOfCommands) {
                when(commandsString.substringAfter(SVG.SPACE).substringBefore(SVG.SPACE)) {
                    SVG.MOVE_TO -> {
                        path = Path()
                        val point = commandsString.substringAfter("${SVG.MOVE_TO}${SVG.SPACE}")
                            .substringBefore(SVG.SPACE)
                        val pointList = point.split(SVG.COMMA)
                        val x = pointList[0].toFloat(); val y = pointList[1].toFloat()
                        commandsString = commandsString.removePrefix("${SVG.SPACE}${SVG.MOVE_TO}${SVG.SPACE}$point")
                        paths.add(path)
                    }
                    SVG.ABS_QUAD_TO -> {
                        val point1 = commandsString.substringAfter("${SVG.ABS_QUAD_TO}${SVG.SPACE}")
                            .substringBefore(SVG.SPACE)
                        commandsString = commandsString.removePrefix("${SVG.SPACE}${SVG.ABS_QUAD_TO}${SVG.SPACE}$point1")
                        val point2 = commandsString.substringAfter(SVG.SPACE).substringBefore(SVG.SPACE)
                        commandsString = commandsString.removePrefix("${SVG.SPACE}$point2")
                        val point1List = point1.split(SVG.COMMA)
                        val point2List = point2.split(SVG.COMMA)
                        val x1 = point1List[0].toFloat(); val y1 = point1List[1].toFloat()
                        val x2 = point2List[0].toFloat(); val y2 = point2List[1].toFloat()
                    }
                    SVG.ABS_LINE_TO -> {}
                }
            }
        }
        return paths
    }
    companion object {
        private const val NEWLINE = "\n"
        private const val SPACE = " "
        private const val COMMA = ","
        private const val ASSIGN = "="
        private const val QUOTE = "\""
        private const val XML_NS = "${QUOTE}http://www.w3.org/2000/svg$QUOTE"
        private const val fill = "${QUOTE}none$QUOTE"

        private const val MOVE_TO = "M"
        private const val ABS_LINE_TO = "L"
        private const val ABS_QUAD_TO = "Q"

        fun parse(string: String) = SVG().apply {
            val lines = string.lines()
            Tags.open = lines[0]
            val path = lines[1]
            strokeColor = path.substringAfter("${Attributes.Path.STROKE}$ASSIGN")
                .substringBefore(SPACE)
            d = path.substringAfter("${Attributes.Path.DATA}$ASSIGN")
                .substringBefore(Tags.closePath).removeSurrounding(QUOTE)
        }
    }

    private object Tags {
        var viewBox = "${QUOTE}0${SPACE}0${SPACE}100${SPACE}100$QUOTE"
        var open = "<svg$SPACE${Attributes.VIEW_BOX}$ASSIGN$viewBox$SPACE${Attributes.XML_NAMESPACE}${ASSIGN}$XML_NS>"
        const val close = "</svg>"
        const val openPath = "<path"
        const val closePath = "/>"

        fun updateOpen() {
            open = "<svg$SPACE${Attributes.VIEW_BOX}$ASSIGN$viewBox$SPACE${Attributes.XML_NAMESPACE}${ASSIGN}$XML_NS>"
        }
    }

    private object Attributes {
        const val VIEW_BOX = "viewBox"
        const val XML_NAMESPACE = "xmlns"
        object Path {
            const val STROKE = "stroke"
            const val FILL = "fill"
            const val DATA = "d"
        }
    }
}