package dev.arkbuilders.arkmemo.repo.graphics

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Environment
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.graphics.ColorCode
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.graphics.SVG
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.repo.NotesRepoHelper
import dev.arkbuilders.arkmemo.utils.dpToPx
import dev.arkbuilders.arkmemo.utils.listFiles
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.createTempFile
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name

class GraphicNotesRepo @Inject constructor(
    private val memoPreferences: MemoPreferences,
    @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
    private val helper: NotesRepoHelper,
    @ApplicationContext private val context: Context
): NotesRepo<GraphicNote> {

    private lateinit var root: Path

    private val displayMetrics by lazy { Resources.getSystem().displayMetrics }
    private val screenWidth by lazy { displayMetrics.widthPixels }
    private val screenHeight by lazy { displayMetrics.heightPixels - 150.dpToPx() }
    private val thumbViewWidth by lazy { context.resources.getDimension(R.dimen.graphic_thumb_width) }

    private val thumbDirectory by lazy { context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) }

    override suspend fun init() {
        helper.init()
        root = memoPreferences.getNotesStorage()
    }

        override suspend fun save(
            note: GraphicNote,
            callback: (SaveNoteResult) -> Unit,
        ) = withContext(iODispatcher) {
            write(note) { callback(it) }
        }

        override suspend fun delete(note: GraphicNote) =
            withContext(iODispatcher) {
                helper.deleteNote(note)
            }

        override suspend fun read(): List<GraphicNote> =
            withContext(iODispatcher) {
                readStorage()
            }

        private suspend fun write(
            note: GraphicNote,
            callback: (SaveNoteResult) -> Unit,
        ) = withContext(iODispatcher) {
            val tempPath = createTempFile()
            note.svg?.generate(tempPath)
            val size = tempPath.fileSize()
            val id = computeId(size, tempPath)
            Log.d(GRAPHICS_REPO, "initial resource name is ${tempPath.name}")
            val isPropertiesChanged =
                helper.persistNoteProperties(
                    resourceId = id,
                    noteTitle = note.title,
                    description = note.description,
                )

            val resourcePath = root.resolve("$id.$SVG_EXT")
            if (resourcePath.exists()) {
                if (isPropertiesChanged) {
                    callback(SaveNoteResult.SUCCESS_UPDATED)
                } else {
                    Log.d(GRAPHICS_REPO, "resource with similar content already exists")
                    callback(SaveNoteResult.ERROR_EXISTING)
                }
                return@withContext
            }

            helper.renameResource(
                note,
                tempPath,
                resourcePath,
                id,
            )
            Log.d(GRAPHICS_REPO, "resource renamed to $resourcePath successfully")
            callback(SaveNoteResult.SUCCESS_NEW)
        }

        private suspend fun readStorage() =
            withContext(iODispatcher) {
                root.listFiles(SVG_EXT) { path ->
                    val svg = SVG.parse(path)
                    if (svg == null) {
                        Log.w(GRAPHICS_REPO, "Skipping invalid SVG: " + path)
                    }
                    val size = path.fileSize()
                    val id = computeId(size, path)
                    val resource =
                        Resource(
                            id = id,
                            name = path.fileName.name,
                            extension = path.extension,
                            modified = path.getLastModifiedTime(),
                        )

            val userNoteProperties = helper.readProperties(id, "")
            val bitmap = exportBitmapFromSvg(fileName = id.toString(), svg = svg)

            GraphicNote(
                title = userNoteProperties.title,
                description = userNoteProperties.description,
                svg = svg,
                resource = resource,
                thumb = bitmap
            )

        }.filter { graphicNote -> graphicNote.svg != null }
    }

    private fun exportBitmapFromSvg(fileName: String, svg: SVG?): Bitmap? {

        // Check if thumb bitmap already exists
        val file = File(thumbDirectory, "$fileName.png")
        try {
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If thumb doesn't exist, create a bitmap and a canvas for offscreen drawing
        val bitmap = Bitmap.createBitmap(
            thumbViewWidth.toInt(), thumbViewWidth.toInt(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        canvas.drawColor(ColorCode.lightYellow)
        svg?.getPaths()?.forEach { path ->

            canvas.save()

            // Scale factor to fit the SVG path into the view
            val scaleX = thumbViewWidth / screenWidth
            val scaleY = thumbViewWidth / screenHeight

            // Find the smallest scale to maintain the aspect ratio
            val scale = minOf(scaleX, scaleY)

            // Center the path in the view
            val dx = (thumbViewWidth - screenWidth * scale) / 2f
            val dy = (thumbViewWidth - screenHeight * scale) / 2f

            // Apply scaling and translation to center the path
            canvas.translate(dx, dy)
            canvas.scale(scale, scale)

            canvas.drawPath(path.path, path.paint)
            canvas.restore()
        } ?: let {
            return null
        }

        // Save the bitmap to a file
        try {

            // Open an output stream and write the bitmap to the file
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)  // Save as PNG
            }
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}

private const val GRAPHICS_REPO = "GraphicNotesRepo"
private const val SVG_EXT = "svg"
