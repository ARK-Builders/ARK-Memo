package space.taran.arkmemo.data.repo.versions

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import space.taran.arkmemo.preferences.MemoPreferences
import java.nio.file.Path
import javax.inject.Inject

class VersionStorageRepo @Inject constructor() {

    @Inject @ApplicationContext lateinit var context: Context
    private val storageByRoot = mutableMapOf<Path, PlainVersionStorage>()

    suspend fun provide(): VersionStorage {
        val root = MemoPreferences.getInstance(context).getPath()!!
        if (storageByRoot[root] == null) {
            val versionStorage = PlainVersionStorage(root)
            versionStorage.init()
            storageByRoot[root] = versionStorage
        }
        return storageByRoot[root]!!
    }
}