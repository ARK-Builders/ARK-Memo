package dev.arkbuilders.arkmemo.graphics

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path as AndroidDrawPath
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
    private var viewBox = "0 0 100 100"
    private val commandsArray = ArrayDeque<SVGCommand>()
    private val canvasPaths = Stack<DrawPath>()

    private val paint
        get() = Paint().also {
            it.color = Color.parseColor(strokeColor)
            it.style = Paint.Style.STROKE
            it.strokeWidth = 10f
            it.strokeCap = Paint.Cap.ROUND
            it.strokeJoin = Paint.Join.ROUND
            it.isAntiAlias = true
        }

    fun addCommand(command: SVGCommand) {
        commandsArray.add(command)
    }

    fun addPath(path: DrawPath) {
        canvasPaths.add(path)
    }

    fun setViewBox(width: Float, height: Float) {
        viewBox = "0 0 $width $height"
    }

    fun generate(path: Path) {
        if (commandsArray.isNotEmpty()) {
            val xmlSerializer = Xml.newSerializer()
            val pathData = commandsArray.joinToString(COMMA)
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

    fun getPaths() = canvasPaths

    fun copy() = SVG().apply {
        strokeColor = this@SVG.strokeColor
        fill = this@SVG.fill
        viewBox = this@SVG.viewBox
        commandsArray.addAll(this@SVG.commandsArray)
        canvasPaths.addAll(this@SVG.canvasPaths)
    }

    private fun createCanvasPaths() {
        if (commandsArray.isNotEmpty()) {
            if (canvasPaths.isNotEmpty()) canvasPaths.clear()
            var path = AndroidDrawPath()
            commandsArray.forEach { command ->
                when (command) {
                    is SVGCommand.MoveTo -> {
                        path = AndroidDrawPath()
                        path.moveTo(command.x, command.y)
                    }

                    is SVGCommand.AbsQuadTo -> {
                        path.quadTo(command.x1, command.y1, command.x2, command.y2)
                    }

                    is SVGCommand.AbsLineTo -> {
                        path.lineTo(command.x, command.y)
                    }
                }
                canvasPaths.add(DrawPath(path, paint))
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
                val pathDataList = pathData.split(COMMA)
                pathDataList.forEach {
                    when (it.first().toString()) {
                        MOVE_TO_CODE -> {
                            commandsArray.add(SVGCommand.MoveTo.fromString(it))
                        }
                        ABS_LINE_TO_CODE -> {
                            commandsArray.add(SVGCommand.MoveTo.fromString(it))
                        }
                        ABS_QUAD_TO_CODE -> {
                            commandsArray.add(SVGCommand.AbsQuadTo.fromString(it))
                        }
                        else -> {}
                    }
                }
                createCanvasPaths()
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

sealed class SVGCommand(val code: String) {

    class MoveTo(
        val x: Float,
        val y: Float
    ) : SVGCommand(MOVE_TO_CODE) {
        override fun toString() = "$code$x $y"

        companion object {
            fun fromString(string: String): SVGCommand {
                val coords = string.removePrefix(MOVE_TO_CODE).split(" ")
                val x = coords[0].toFloat()
                val y = coords[1].toFloat()
                return MoveTo(x, y)
            }
        }
    }

    class AbsLineTo(
        val x: Float,
        val y: Float
    ) : SVGCommand(ABS_LINE_TO_CODE) {
        override fun toString() = "$code$x $y"

        companion object {
            fun fromString(string: String): SVGCommand {
                val coords = string.removePrefix(ABS_LINE_TO_CODE).split(" ")
                val x = coords[0].toFloat()
                val y = coords[1].toFloat()
                return AbsLineTo(x, y)
            }
        }
    }

    class AbsQuadTo(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float
    ) : SVGCommand(ABS_QUAD_TO_CODE) {
        override fun toString() = "$code$x1 $y1 $x2 $y2"

        companion object {
            fun fromString(string: String): SVGCommand {
                val coords = string.removePrefix(ABS_QUAD_TO_CODE).split(" ")
                val x1 = coords[0].toFloat()
                val y1 = coords[1].toFloat()
                val x2 = coords[2].toFloat()
                val y2 = coords[3].toFloat()
                return AbsQuadTo(x1, y1, x2, y2)
            }
        }
    }
}

private const val MOVE_TO_CODE = "M"
private const val ABS_LINE_TO_CODE = "L"
private const val ABS_QUAD_TO_CODE = "Q"

private const val COMMA = ","
private const val XML_NS_URI = "http://www.w3.org/2000/svg"
private const val SVG_TAG = "svg"
private const val PATH_TAG = "path"