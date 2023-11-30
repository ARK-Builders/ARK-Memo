package dev.arkbuilders.arkmemo.repo.versions

import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arkmemo.models.Version

interface VersionStorage {
    suspend fun add(version: Version)

    suspend fun forget(id: ResourceId)

    fun versions(): List<Version>

    fun contains(id: ResourceId): Boolean

    fun parentsTreeByChild(
        child: ResourceId
    ): Map<ResourceId, List<ResourceId>>

    fun childrenNotParents(): List<ResourceId>

    fun isLatestResourceVersion(id: ResourceId): Boolean

    suspend fun persist()
}