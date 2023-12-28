package dev.arkbuilders.arkmemo.repo.versions

import dev.arkbuilders.arklib.ResourceId

typealias Version2 = ResourceId

data class Version(
    val parent: ResourceId,
    val child: ResourceId
)