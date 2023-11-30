package dev.arkbuilders.arkmemo.repo.versions

import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import java.nio.file.Path
import javax.inject.Inject

class VersionStorageRepo @Inject constructor(
    private val memoPreferences: MemoPreferences
) {

    private val storageByRoot = mutableMapOf<Path, PlainVersionStorage>()

    suspend fun provide(): VersionStorage {
        val root = memoPreferences.getPath()!!
        if (storageByRoot[root] == null) {
            val versionStorage = PlainVersionStorage(root)
            versionStorage.init()
            storageByRoot[root] = versionStorage
        }
        return storageByRoot[root]!!
    }
}