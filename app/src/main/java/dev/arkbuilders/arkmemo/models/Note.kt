package dev.arkbuilders.arkmemo.models

import dev.arkbuilders.arklib.data.index.Resource

interface Note {
    val title: String
    val description: String
    var resource: Resource?
}