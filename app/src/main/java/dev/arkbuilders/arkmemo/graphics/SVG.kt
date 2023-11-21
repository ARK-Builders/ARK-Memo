package dev.arkbuilders.arkmemo.graphics

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path as AndroidDrawPath
import android.util.Log
import android.util.Xml
import dev.arkbuilders.arkmemo.ui.viewmodels.DrawPath
import org.xmlpull.v1.XmlPullParser
import java.nio.file.Path
import java.util.Stack
import kotlin.io.path.reader
import kotlin.io.path.writer

class SVG {
    var strokeColor = "black"
    private var fill = "none"
    private var viewBox = "0${SPACE}0$SPACE$100$SPACE$100"
    private var pathData = ""
    private var commandsArray = ArrayDeque<String>()

    private val paint
        get() = Paint().also {
            it.color = Color.parseColor(strokeColor)
            it.style = Paint.Style.STROKE
            it.strokeWidth = 10f
            it.strokeCap = Paint.Cap.ROUND
            it.strokeJoin = Paint.Join.ROUND
            it.isAntiAlias = true
        }

    fun writeData(command: String) {
        commandsArray.add(command)
    }

    fun moveTo(x: Float, y: Float) = "$SPACE$MOVE_TO$SPACE$x,$y"

    fun relativeLineTo(x: Float, y: Float) = "$SPACE$ABS_LINE_TO$SPACE$x,$y"

    fun quadraticBezierTo(x1: Float, y1: Float, x2: Float, y2: Float) =
        "$SPACE$ABS_QUAD_TO$SPACE$x1,$y1$SPACE$x2,$y2"

    fun setViewBox(width: Float, height: Float) {
        viewBox = "0${SPACE}0$SPACE$width$SPACE$height"
    }

    fun generate(path: Path) {
        if (commandsArray.isNotEmpty()) {
            val xmlSerializer = Xml.newSerializer()
            pathData = commandsArray.joinToString(separator = "")
            xmlSerializer.apply {
                setOutput(path.writer())
                startDocument("utf-8", false)
                startTag("", SVG_TAG)
                attribute("", Attributes.VIEW_BOX, viewBox)
                attribute("", Attributes.XML_NS_URI, XML_NS_URI)
                startTag("", PATH_TAG)
                attribute("", Attributes.Path.STROKE, strokeColor)
                attribute("", Attributes.Path.FILL, fill)
                attribute("", Attributes.Path.DATA, pathData)
                endTag("", PATH_TAG)
                endTag("", SVG_TAG)
                endDocument()
            }
        }
    }

    fun getPaths() = createPaths()

    fun copy() = SVG().apply {
        strokeColor = this@SVG.strokeColor
        fill = this@SVG.fill
        viewBox = this@SVG.viewBox
        commandsArray = this@SVG.commandsArray
    }

    private fun createPaths(): Stack<DrawPath> {
        val paths = Stack<DrawPath>()
        if (pathData.isNotEmpty()) {
            var path = AndroidDrawPath()
            var commandsString = pathData
            val numberOfCommands = commandsString.filter {
                it.isLetter()
            }.length
            if (commandsArray.isNotEmpty()) commandsArray.clear()
            Log.d("svg", "$numberOfCommands commands available")
            for (index in 1..numberOfCommands) {
                when (commandsString.substringAfter(SPACE).substringBefore(SPACE)) {
                    MOVE_TO -> {
                        path = AndroidDrawPath()
                        val point = commandsString.substringAfter("$MOVE_TO$SPACE")
                            .substringBefore(SPACE)
                        val pointList = point.split(COMMA)
                        val x = pointList[0].toFloat()
                        val y = pointList[1].toFloat()
                        Log.d("svg", "move to $point")
                        val command = moveTo(x, y)
                        commandsArray.add(command)
                        commandsString = commandsString.removePrefix(command)
                        path.moveTo(x, y)
                        paths.add(DrawPath(path, paint))
                    }

                    ABS_QUAD_TO -> {
                        val point1 = commandsString.substringAfter("$ABS_QUAD_TO$SPACE")
                            .substringBefore(SPACE)
                        commandsString =
                            commandsString.removePrefix("$SPACE$ABS_QUAD_TO$SPACE$point1")
                        val point2 = commandsString.substringAfter(SPACE).substringBefore(SPACE)
                        commandsString = commandsString.removePrefix("$SPACE$point2")
                        val point1List = point1.split(COMMA)
                        val point2List = point2.split(COMMA)
                        val x1 = point1List[0].toFloat()
                        val y1 = point1List[1].toFloat()
                        val x2 = point2List[0].toFloat()
                        val y2 = point2List[1].toFloat()
                        Log.d("svg", "quad to $point1 $point2")
                        commandsArray.add(quadraticBezierTo(x1, y1, x2, y2))
                        path.quadTo(x1, y1, x2, y2)
                    }

                    ABS_LINE_TO -> {}
                }
            }
        }
        return paths
    }

    companion object {
        fun parse(path: Path) = SVG().apply {
            val xmlParser = Xml.newPullParser()
            xmlParser.apply {
                setInput(path.reader())
                var event = xmlParser.eventType
                while (event != XmlPullParser.END_DOCUMENT) {
                    val tag = xmlParser.name
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            when (tag) {
                                SVG_TAG -> { viewBox = getAttributeValue("", Attributes.VIEW_BOX) }
                                PATH_TAG -> {
                                    strokeColor = getAttributeValue("", Attributes.Path.STROKE)
                                    fill = getAttributeValue("", Attributes.Path.FILL)
                                    pathData = getAttributeValue("", Attributes.Path.DATA)
                                }
                            }
                        }
                    }
                    event = next()
                }
                createPaths()
            }
        }

        private object Attributes {
            const val VIEW_BOX = "viewBox"
            const val XML_NS_URI = "xmlns"

            object Path {
                const val STROKE = "stroke"
                const val FILL = "fill"
                const val DATA = "d"
            }
        }
    }
}

private const val MOVE_TO = "M"
private const val ABS_LINE_TO = "L"
private const val ABS_QUAD_TO = "Q"

private const val SPACE = " "
private const val COMMA = ","
private const val XML_NS_URI = "http://www.w3.org/2000/svg"
private const val SVG_TAG = "svg"
private const val PATH_TAG = "path"