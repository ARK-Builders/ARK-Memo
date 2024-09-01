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
    val duration: String = "",
    @IgnoredOnParcel
    var path: Path = createTempFile(),
    @IgnoredOnParcel
    override var resource: Resource? = null,
) : Note, Parcelable
