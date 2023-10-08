package dev.arkbuilders.arkmemo.data

import java.nio.file.attribute.FileTime

data class ResourceMeta(
    val id: Long,
    val name: String,
    val extension: String,
    val modified: FileTime,
    val size: Long
    )
