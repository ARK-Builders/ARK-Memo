package space.taran.arkmemo.data.models

import dev.arkbuilders.arklib.ResourceId

data class Version(
    val parent: ResourceId,
    val child: ResourceId
)