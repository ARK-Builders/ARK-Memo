package dev.arkbuilders.arkmemo.repo.versions

import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.data.storage.Storage

interface VersionsStorage: Storage<Versions> {

    fun setParents(child: ResourceId, parents: Versions) = setValue(child, parents)

    fun getParents(child: ResourceId): Versions = getValue(child)

    fun removeParents(child: ResourceId) {
        remove(child)
    }
}