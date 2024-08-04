package dev.arkbuilders.arkmemo.repo.voices

import android.util.Log
import dev.arkbuilders.arklib.computeId
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.repo.NotesRepoHelper
import dev.arkbuilders.arkmemo.utils.extractDuration
import dev.arkbuilders.arkmemo.utils.listFiles
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.pathString

class VoiceNotesRepo
    @Inject
    constructor(
        private val memoPreferences: MemoPreferences,
        @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
        private val helper: NotesRepoHelper,
    ) : NotesRepo<VoiceNote> {
        private lateinit var root: Path

        override suspend fun init() {
            root = memoPreferences.getNotesStorage()
            helper.init()
        }

        override suspend fun read(): List<VoiceNote> =
            withContext(iODispatcher) {
                readStorage()
            }

        override suspend fun delete(notes: List<VoiceNote>) {
            helper.deleteNote(notes)
        }

        override suspend fun delete(note: VoiceNote) {
            helper.deleteNote(note)
        }

        override suspend fun save(
            note: VoiceNote,
            callback: (SaveNoteResult) -> Unit,
        ) {
            write(note) { callback(it) }
        }

        private suspend fun write(
            note: VoiceNote,
            callback: (SaveNoteResult) -> Unit,
        ) = withContext(iODispatcher) {
            val tempPath = note.path
            val size = tempPath.fileSize()
            val id = computeId(size, tempPath)

            val isPropertiesChanged =
                helper.persistNoteProperties(
                    resourceId = id,
                    noteTitle = note.title,
                    description = note.description,
                )

            Log.d(VOICES_REPO, "initial resource name is ${tempPath.name}")

            helper.persistNoteProperties(resourceId = id, noteTitle = note.title)

            val resourcePath = root.resolve("$id.$VOICE_EXT")
            if (resourcePath.exists()) {
                Log.d(
                    VOICES_REPO,
                    "resource with similar content already exists",
                )
                if (isPropertiesChanged) {
                    callback(SaveNoteResult.SUCCESS_UPDATED)
                } else {
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
            note.path = resourcePath
            Log.d(VOICES_REPO, "resource renamed to $resourcePath successfully")
            callback(SaveNoteResult.SUCCESS_NEW)
        }

        private suspend fun readStorage(): List<VoiceNote> =
            withContext(iODispatcher) {
                root.listFiles(VOICE_EXT) { path ->
                    val id = computeId(path.fileSize(), path)
                    val resource =
                        Resource(
                            id = id,
                            name = path.name,
                            extension = path.extension,
                            modified = path.getLastModifiedTime(),
                        )

                    val userNoteProperties = helper.readProperties(id, "")
                    VoiceNote(
                        title = userNoteProperties.title,
                        description = userNoteProperties.description,
                        path = path,
                        duration = extractDuration(path.pathString),
                        resource = resource,
                    )
                }.filter { voiceNote -> voiceNote.duration.isNotEmpty() }
            }
    }

private const val VOICES_REPO = "VoiceNotesRepo"
private const val VOICE_EXT = "3gp"
