package dev.arkbuilders.arkmemo.repo.text

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.repo.NotesRepoHelper
import dev.arkbuilders.arkmemo.utils.listFiles
import dev.arkbuilders.arkmemo.utils.readLines
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.createTempFile
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.writeLines

class TextNotesRepo
    @Inject
    constructor(
        @Named(IO_DISPATCHER)
        private val iODispatcher: CoroutineDispatcher,
        private val helper: NotesRepoHelper,
    ) : NotesRepo<TextNote> {
        private val root: Path by lazy { helper.root }

        override suspend fun init(root: String) {
            helper.init(root)
        }

        override suspend fun save(
            note: TextNote,
            callback: (SaveNoteResult) -> Unit,
        ) {
            write(note) { callback(it) }
        }

        override suspend fun delete(notes: List<TextNote>) {
            helper.deleteNotes(notes)
        }

        override suspend fun delete(note: TextNote) {
            helper.deleteNote(note)
        }

        override suspend fun read(): List<TextNote> =
            withContext(iODispatcher) {
                readStorage()
            }

        private suspend fun write(
            note: TextNote,
            callback: (SaveNoteResult) -> Unit,
        ) = withContext(iODispatcher) {
            val tempPath = createTempFile()
            val lines = note.text.split('\n')
            tempPath.writeLines(lines)
            val size = tempPath.fileSize()
            val id = computeId(size, tempPath)
            Log.d(TEXT_REPO, "initial resource name is ${tempPath.name}")
            val isPropertiesChanged =
                helper.persistNoteProperties(
                    resourceId = id,
                    noteTitle = note.title,
                    description = note.description,
                )

            val resourcePath = root.resolve("$id.$NOTE_EXT")
            if (resourcePath.exists()) {
                if (isPropertiesChanged) {
                    callback(SaveNoteResult.SUCCESS_UPDATED)
                } else {
                    Log.d(TEXT_REPO, "resource with similar content already exists")
                    callback(SaveNoteResult.ERROR_EXISTING)
                }
                return@withContext
            }

            helper.renameResource(
                note = note,
                tempPath = tempPath,
                resourcePath = resourcePath,
                resourceId = id,
            )
            Log.d(TEXT_REPO, "resource renamed to $resourcePath successfully")
            callback(SaveNoteResult.SUCCESS_NEW)
        }

        private suspend fun readStorage(): List<TextNote> =
            withContext(iODispatcher) {
                root.listFiles(NOTE_EXT) { path ->
                    val size = path.fileSize()
                    val id = computeId(size, path)
                    val resource =
                        Resource(
                            id = id,
                            name = path.fileName.name,
                            extension = path.extension,
                            modified = path.getLastModifiedTime(),
                        )

                    try {
                        path.readLines { data ->
                            val userNoteProperties =
                                helper.readProperties(
                                    id,
                                    data.substringBefore("\n"),
                                )

                            TextNote(
                                title = userNoteProperties.title,
                                description = userNoteProperties.description,
                                text = data,
                                resource = resource,
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        TextNote(
                            text = "",
                        )
                    }
                }.filter { textNote -> textNote.text.isNotEmpty() }
            }
    }

private const val TEXT_REPO = "TextNotesRepo"
private const val NOTE_EXT = "note"
