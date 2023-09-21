package space.taran.arkmemo.data.repo.versions

import android.util.Log
import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.arkFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.taran.arkmemo.data.models.Version
import space.taran.arkmemo.data.models.VersionsResult
import space.taran.arkmemo.utils.arkVersions
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.writeLines

class PlainVersionStorage(private val root: Path): VersionStorage {

    private val storageFile = root.arkFolder().arkVersions()
    private var lastModified = FileTime.fromMillis(0L)
    private val versions = mutableListOf<Version>()
    private val parents = mutableSetOf<ResourceId>()
    private val children = mutableListOf<ResourceId>()


    suspend fun init() =
        withContext(Dispatchers.IO) {
            if (Files.exists(storageFile)) {
                val result = readStorage()
                lastModified = Files.getLastModifiedTime(storageFile)
                Log.d(
                    VERSIONS_STORAGE,
                    "file $storageFile exists," +
                            " last modified at $lastModified"
                )
                versions.addAll(result.versions)
                parents.addAll(result.parents)
                children.addAll(result.children)
            } else Log.d(
                VERSIONS_STORAGE,
                "file $storageFile doesn't exists"
            )
        }

    override fun isLatestVersion(id: ResourceId) = childrenNotParents().contains(id)

    private fun replace(oldVersion: Version, newVersion: Version) {
        val replaceIndex = versions.indexOf(oldVersion)
        versions[replaceIndex] = newVersion
    }

    override fun contains(id: ResourceId): Boolean {
        return parents.contains(id) || children.contains(id)
    }

    override suspend fun add(version: Version) {
        versions.add(version)
        parents.add(version.parent)
        children.add(version.child)
        persist()
    }

    override suspend fun forget(id: ResourceId) {
        if (!parents.contains(id)) {
            val myParents = parentsTreeByChild(id)
            myParents[id]?.forEach { parent ->
                val version = versions
                    .find { it.parent == parent }
                versions.remove(version)
                parents.remove(version?.parent)
                children.remove(version?.child)
            }
        }
        if (parents.contains(id) && !children.contains(id)) {
            val version = versions.find {
                it.parent == id
            }
            versions.remove(version)
            parents.remove(id)
        }
        if (parents.contains(id) && children.contains(id)) {
            val versionIdIsChild = versions.find {
                it.child == id
            }
            val versionIdIsParent = versions.find {
                it.parent == id
            }
            val newVersion = Version(
                versionIdIsChild?.parent!!,
                versionIdIsParent?.child!!
            )
            replace(versionIdIsChild, newVersion)
            versions.remove(versionIdIsParent)
            parents.remove(id)
            children.remove(id)
        }
        persist()
    }

    override fun versions() = versions

    override fun parentsTreeByChild(
        child: ResourceId
    ): Map<ResourceId, List<ResourceId>> {
        var localChild = child
        var parent: ResourceId?
        val parents = mutableListOf<ResourceId>()
        for (version in versions) {
            parent = versions.find {
                it.child == localChild
            }?.parent
            if (parent != null && children.contains(parent))
                localChild = parent
            if (parent != null) parents.add(parent)
            if (!children.contains(parent))
                break
        }
        return mapOf(child to parents)
    }

    override fun childrenNotParents(): List<ResourceId> {
        return children.filter {
            !parents.contains(it)
        }
    }

    private suspend fun writeToStorage() =
        withContext(Dispatchers.IO) {
            val lines = mutableListOf<String>()
            lines.add(
                "$STORAGE_VERSION_PREFIX$STORAGE_VERSION"
            )
            lines.addAll(
                versions.map {
                    "${it.parent}$KEY_VALUE_SEPARATOR${it.child}"
                }
            )
            storageFile.writeLines(lines, Charsets.UTF_8)
        }

    private suspend fun readStorage(): VersionsResult =
        withContext(Dispatchers.IO) {
            val lines = Files.readAllLines(storageFile)
            val storageVersion = lines.removeAt(0)
            verifyVersion(storageVersion)
            val versions = lines.map {
                val parts = it.split(KEY_VALUE_SEPARATOR)
                val parent = ResourceId.fromString(parts[0])
                val child = ResourceId.fromString(parts[1])
                Log.d(
                    VERSIONS_STORAGE,
                    it
                )
                Version(parent, child)
            }
            val parents = versions.map {
                Log.d(
                    VERSIONS_STORAGE,
                    "parent: ${it.parent}"
                )
                it.parent
            }.toSet()
            val children = versions.map {
                Log.d(
                    VERSIONS_STORAGE,
                    "child: ${it.child}"
                )
                it.child
            }
            return@withContext VersionsResult(
                versions,
                parents,
                children
            )
        }

    override suspend fun persist() =
        withContext(Dispatchers.IO) {
            writeToStorage()
            return@withContext
        }

    companion object {
        private const val VERSIONS_STORAGE = "versions"
        private const val STORAGE_VERSION_PREFIX = "version "
        private const val STORAGE_VERSION = 1
        private const val KEY_VALUE_SEPARATOR = "->"

        private fun verifyVersion(header: String) {
            if (!header.startsWith(STORAGE_VERSION_PREFIX))
                throw IllegalStateException("Unknown storage version")
            val version = header.removePrefix(STORAGE_VERSION_PREFIX).toInt()
            if (version > STORAGE_VERSION)
                throw IllegalStateException("Storage version is newer than app")
            if (version < STORAGE_VERSION)
                throw IllegalStateException("Storage version is older than app")
        }
    }
}
