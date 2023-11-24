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
    private var strokeColor = "black"
    private var fill = "none"
    private var viewBox = "$ZERO${SPACE}$ZERO$SPACE$HUNDRED$SPACE$HUNDRED"
    private val commandsArray = ArrayDeque<String>()
    private val paths = Stack<DrawPath>()


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

    fun addPath(path: DrawPath) {
        paths.add(path)
    }

    fun moveTo(x: Float, y: Float) = "$MOVE_TO$SPACE$x$SPACE$y"

    fun relativeLineTo(x: Float, y: Float) = "$ABS_LINE_TO$SPACE$x$SPACE$y"

    fun quadraticBezierTo(x1: Float, y1: Float, x2: Float, y2: Float) =
        "$ABS_QUAD_TO$SPACE$x1$SPACE$y1$SPACE$x2$SPACE$y2"

    fun setViewBox(width: Float, height: Float) {
        viewBox = "$ZERO${SPACE}$ZERO$SPACE$width$SPACE$height"
    }

    fun generate(path: Path) {
        if (commandsArray.isNotEmpty()) {
            val xmlSerializer = Xml.newSerializer()
            val pathData = commandsArray.joinToString()
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

    fun getPaths() = paths

    fun copy() = SVG().apply {
        strokeColor = this@SVG.strokeColor
        fill = this@SVG.fill
        viewBox = this@SVG.viewBox
        commandsArray.addAll(this@SVG.commandsArray)
        paths.addAll(this@SVG.paths)
    }

    private fun createPaths(pathData: String) {
        commandsArray.addAll(pathData.split("$COMMA$SPACE"))
        if (commandsArray.isNotEmpty()) {
            if (paths.isNotEmpty()) paths.clear()
            var path = AndroidDrawPath()
            commandsArray.forEach { command ->
                when (command.first().toString()) {
                    MOVE_TO -> {
                        path = AndroidDrawPath()
                        val point = command.removePrefix("$MOVE_TO$SPACE")
                        val pointList = point.split(SPACE)
                        val x = pointList[0].toFloat()
                        val y = pointList[1].toFloat()
                        Log.d("svg", "move to $point")
                        path.moveTo(x, y)
                    }

                    ABS_QUAD_TO -> {
                        val points = command.removePrefix("$ABS_QUAD_TO$SPACE")
                        val pointList = points.split(SPACE)
                        val x1 = pointList[0].toFloat()
                        val y1 = pointList[1].toFloat()
                        val x2 = pointList[2].toFloat()
                        val y2 = pointList[3].toFloat()
                        Log.d("svg", "quad to $x1 $y1 $x2 $y2")
                        path.quadTo(x1, y1, x2, y2)
                    }

                    ABS_LINE_TO -> {}
                }
                paths.add(DrawPath(path, paint))
            }
        }
    }

    companion object {
        fun parse(path: Path) = SVG().apply {
            val xmlParser = Xml.newPullParser()
            var pathData = ""
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
                createPaths(pathData)
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

private const val ZERO = "0"
private const val HUNDRED = "100"
private const val SPACE = " "
private const val COMMA = ","
private const val XML_NS_URI = "http://www.w3.org/2000/svg"
private const val SVG_TAG = "svg"
private const val PATH_TAG = "path"