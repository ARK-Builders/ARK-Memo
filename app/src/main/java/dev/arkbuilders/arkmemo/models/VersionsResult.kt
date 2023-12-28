package dev.arkbuilders.arkmemo.models

import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arkmemo.repo.versions.Version

data class VersionsResult (
    val versions: List<Version>,
    val parents: Set<ResourceId>,
    val children: List<ResourceId>
)