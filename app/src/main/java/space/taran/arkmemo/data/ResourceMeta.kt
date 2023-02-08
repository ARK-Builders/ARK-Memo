package space.taran.arkmemo.data

import java.nio.file.attribute.FileTime
import space.taran.arklib.ResourceId

data class ResourceMeta(
    val id: ResourceId,
    val name: String,
    val extension: String,
    val modified: FileTime,
    val size: Long
    )
