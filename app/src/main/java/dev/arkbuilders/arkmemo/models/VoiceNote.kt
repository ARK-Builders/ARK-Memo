package dev.arkbuilders.arkmemo.models

import android.os.Parcelable
import dev.arkbuilders.arklib.data.index.Resource
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.nio.file.Path
import kotlin.io.path.createTempFile

@Parcelize
class VoiceNote(
    override val title: String = "",
    override val description: String = "",
    var duration: String = "",
    @IgnoredOnParcel
    var path: Path = createTempFile(),
    @IgnoredOnParcel
    override var resource: Resource? = null,
    override var pendingForDelete: Boolean = false,
    var isPlaying: Boolean = false,
    var pendingForPlaybackReset: Boolean = false,
    var waitToBeResumed: Boolean = false,
    var currentPlayingPos: Int = 0,
    var currentMaxAmplitude: Int = 0,
    override var selected: Boolean = false,
) : Note, Parcelable
