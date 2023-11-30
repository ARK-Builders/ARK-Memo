package dev.arkbuilders.arkmemo.models

import dev.arkbuilders.arklib.ResourceId

data class VersionsResult (
    val versions: List<Version>,
    val parents: Set<ResourceId>,
    val children: List<ResourceId>
)