package space.taran.arkmemo.data

import java.nio.file.attribute.FileTime

data class ResourceMeta(
    val id: String,
    val name: String,
    val extension: String,
    val modified: FileTime,
    val size: Long
    )
