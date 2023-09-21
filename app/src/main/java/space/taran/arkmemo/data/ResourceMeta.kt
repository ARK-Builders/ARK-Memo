package space.taran.arkmemo.data

import dev.arkbuilders.arklib.ResourceId
import java.nio.file.attribute.FileTime

data class ResourceMeta(
    val id: ResourceId,
    val name: String,
    val extension: String,
    val modified: FileTime,
    val size: Long
    )
