package dev.arkbuilders.arkmemo.models

import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.index.Resource
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface Note {
    val title: String
    var resource: Resource?

    fun exists(id: ResourceId) = resource != null && resource?.id == id
}

val DEFAULT_TITLE = "note ${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}"