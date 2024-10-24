package dev.arkbuilders.arkmemo.ui.views.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.text.Charsets.UTF_8

@Singleton
class Preferences @Inject constructor(private val appCtx: Context) {

    suspend fun persistUsedColors(
        colors: List<Color>
    ) = withContext(Dispatchers.IO) {
        try {
            val colorsStorage = appCtx.filesDir.resolve(COLORS_STORAGE)
                .toPath()
            val lines = colors.map { it.value.toString() }
            Files.write(colorsStorage, lines, UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun readUsedColors(): List<Color> {
        val colors = mutableListOf<Color>()
        withContext(Dispatchers.IO) {

            try {
                val colorsStorage = appCtx
                    .filesDir
                    .resolve(COLORS_STORAGE)
                    .toPath()

                if (colorsStorage.exists()) {
                    Files.readAllLines(colorsStorage, UTF_8).forEach { line ->
                        val color = Color(line.toULong())
                        colors.add(color)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return colors
    }

    suspend fun persistDefaults(color: Color, resolution: Resolution) {
        withContext(Dispatchers.IO) {
            val defaultsStorage = appCtx.filesDir.resolve(DEFAULTS_STORAGE)
                .toPath()
            val defaults = ImageDefaults(
                color.value,
                resolution
            )
            val jsonString = Json.encodeToString(defaults)
            defaultsStorage.writeText(jsonString, UTF_8)
        }
    }

    suspend fun readDefaults(): ImageDefaults {
        var defaults = ImageDefaults()
        try {
            withContext(Dispatchers.IO) {
                val defaultsStorage = appCtx.filesDir.resolve(DEFAULTS_STORAGE)
                    .toPath()
                if (defaultsStorage.exists()) {
                    val jsonString = defaultsStorage.readText(UTF_8)
                    defaults = Json.decodeFromString(jsonString)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return defaults
    }

    companion object {
        private const val COLORS_STORAGE = "colors"
        private const val DEFAULTS_STORAGE = "defaults"
    }
}
