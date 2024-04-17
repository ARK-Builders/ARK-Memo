package dev.arkbuilders.arkmemo.graphics

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.graphics.Path as AndroidDrawPath
import android.util.Xml
import dev.arkbuilders.arkmemo.ui.viewmodels.DrawPath
import org.xmlpull.v1.XmlPullParser
import java.nio.file.Path
import kotlin.io.path.reader
import kotlin.io.path.writer

class SVG {
    private var strokeColor = "black"
    private var fill = "none"
    private var viewBox = ViewBox()
    private val commands = ArrayDeque<SVGCommand>()
    private val paths = ArrayDeque<DrawPath>()

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
        commands.addLast(command)
    }

    fun addPath(path: DrawPath) {
        paths.addLast(path)
    }

    fun setViewBox(width: Float, height: Float) {
        viewBox = ViewBox(width = width, height = height)
    }

    fun getViewBox(): ViewBox {
        return viewBox
    }

    fun generate(path: Path) {
        if (commands.isNotEmpty()) {
            val xmlSerializer = Xml.newSerializer()
            val pathData = commands.joinToString()
            xmlSerializer.apply {
                setOutput(path.writer())
                startDocument("utf-8", false)
                startTag("", SVG_TAG)
                attribute("", Attributes.VIEW_BOX, viewBox.toString())
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

    fun getPaths(): Collection<DrawPath> = paths

    fun copy(): SVG = SVG().apply {
        strokeColor = this@SVG.strokeColor
        fill = this@SVG.fill
        viewBox = this@SVG.viewBox
        commands.addAll(this@SVG.commands)
        paths.addAll(this@SVG.paths)
    }

    private fun createCanvasPaths() {
        if (commands.isNotEmpty()) {
            if (paths.isNotEmpty()) paths.clear()
            var path = AndroidDrawPath()
            commands.forEach { command ->
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
                paths.addLast(DrawPath(path, paint))
            }
        }
    }

    companion object {
        fun parse(path: Path): SVG = SVG().apply {
            val xmlParser = Xml.newPullParser()
            var pathData = ""

            xmlParser.apply {
                setInput(path.reader())

                var event = xmlParser.eventType
                var pathCount = 0
                while (event != XmlPullParser.END_DOCUMENT) {
                    val tag = xmlParser.name
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            when (tag) {
                                SVG_TAG -> {
                                    viewBox = ViewBox.fromString(
                                        getAttributeValue("", Attributes.VIEW_BOX)
                                    )
                                }
                                PATH_TAG -> {
                                    pathCount += 1
                                    strokeColor = getAttributeValue("", Attributes.Path.STROKE)
                                    fill = getAttributeValue("", Attributes.Path.FILL)
                                    pathData = getAttributeValue("", Attributes.Path.DATA)
                                }
                            }
                            if (pathCount > 1) {
                                Log.d("svg", "found more than 1 path in file")
                                break
                            }
                        }
                    }

                    event = next()
                }

                pathData.split(COMMA).forEach {
                    val command = it.trim()
                    if (command.isEmpty()) return@forEach
                    when (command.first()) {
                        SVGCommand.MoveTo.CODE -> {
                            commands.addLast(SVGCommand.MoveTo.fromString(command))
                        }
                        SVGCommand.AbsLineTo.CODE -> {
                            commands.addLast(SVGCommand.MoveTo.fromString(command))
                        }
                        SVGCommand.AbsQuadTo.CODE -> {
                            commands.addLast(SVGCommand.AbsQuadTo.fromString(command))
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

data class ViewBox(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 100f,
    val height: Float = 100f
) {
    override fun toString(): String = "$x $y $width $height"

    companion object {
        fun fromString(string: String): ViewBox {
            val viewBox = string.split(" ")
            return ViewBox(
                viewBox[0].toFloat(),
                viewBox[1].toFloat(),
                viewBox[2].toFloat(),
                viewBox[3].toFloat()
            )
        }
    }
}

sealed class SVGCommand {

    class MoveTo(
        val x: Float,
        val y: Float
    ) : SVGCommand() {
        override fun toString(): String = "$CODE $x $y"

        companion object {
            const val CODE = 'M'

            fun fromString(string: String): SVGCommand {
                val coords = string.removePrefix("$CODE").trim().split(" ")
                val x = coords[0].toFloat()
                val y = coords[1].toFloat()
                return MoveTo(x, y)
            }
        }
    }

    class AbsLineTo(
        val x: Float,
        val y: Float
    ) : SVGCommand() {
        override fun toString(): String = "$CODE $x $y"

        companion object {
            const val CODE = 'L'

            fun fromString(string: String): SVGCommand {
                val coords = string.removePrefix("$CODE").trim().split(" ")
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
    ) : SVGCommand() {
        override fun toString(): String = "$CODE $x1 $y1 $x2 $y2"

        companion object {
            const val CODE = 'Q'

            fun fromString(string: String): SVGCommand {
                val coords = string.removePrefix("$CODE").trim().split(" ")
                val x1 = coords[0].toFloat()
                val y1 = coords[1].toFloat()
                val x2 = coords[2].toFloat()
                val y2 = coords[3].toFloat()
                return AbsQuadTo(x1, y1, x2, y2)
            }
        }
    }
}

private const val COMMA = ","
private const val XML_NS_URI = "http://www.w3.org/2000/svg"
private const val SVG_TAG = "svg"
private const val PATH_TAG = "path"