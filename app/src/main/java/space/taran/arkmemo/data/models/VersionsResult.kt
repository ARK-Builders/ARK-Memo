package space.taran.arkmemo.data.models

import space.taran.arklib.ResourceId

data class VersionsResult (
    val versions: List<Version>,
    val parents: Set<ResourceId>,
    val children: List<ResourceId>
)