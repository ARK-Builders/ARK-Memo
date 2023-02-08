package space.taran.arkmemo.data.models

import space.taran.arklib.ResourceId

data class Version(
    val parent: ResourceId,
    val child: ResourceId
)