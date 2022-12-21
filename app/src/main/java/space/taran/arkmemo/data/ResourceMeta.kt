package space.taran.arkmemo.data

import space.taran.arkmemo.data.repositories.ResourceId
import java.nio.file.attribute.FileTime

data class ResourceMeta(
    val resourceId: ResourceId,
    val name: String,
    val extension: String,
    val modified: FileTime,
    val size: Long
)
