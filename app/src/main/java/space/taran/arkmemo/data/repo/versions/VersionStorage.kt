package space.taran.arkmemo.data.repo.versions

import space.taran.arklib.ResourceId
import space.taran.arkmemo.data.models.Version

interface VersionStorage {
    suspend fun add(version: Version)

    suspend fun forget(id: ResourceId)

    fun versions(): List<Version>

    fun contains(id: ResourceId): Boolean

    fun parentsTreeByChild(
        child: ResourceId
    ): Map<ResourceId, List<ResourceId>>

    fun childrenNotParents(): List<ResourceId>

    fun isLatestVersion(id: ResourceId): Boolean

    suspend fun persist()
}