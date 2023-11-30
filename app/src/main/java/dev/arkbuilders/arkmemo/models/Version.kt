package dev.arkbuilders.arkmemo.models

import dev.arkbuilders.arklib.ResourceId

data class Version(
    val parent: ResourceId,
    val child: ResourceId
)