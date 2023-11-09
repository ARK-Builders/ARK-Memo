package dev.arkbuilders.arkmemo.data

import android.os.Parcelable
import dev.arkbuilders.arklib.ResourceId
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.nio.file.attribute.FileTime
import java.time.Instant

@Parcelize
data class ResourceMeta(
    val id: ResourceId,
    val name: String,
    val extension: String,
    @IgnoredOnParcel
    val modified: FileTime = FileTime.from(Instant.now()),
    val size: Long
    ): Parcelable
