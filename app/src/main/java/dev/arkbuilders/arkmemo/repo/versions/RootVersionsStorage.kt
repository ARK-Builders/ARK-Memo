package dev.arkbuilders.arkmemo.repo.versions

import dev.arkbuilders.arklib.ResourceId
import dev.arkbuilders.arklib.arkFolder
import dev.arkbuilders.arklib.data.storage.FileStorage
import kotlinx.coroutines.CoroutineScope
import space.taran.arkmemo.utils.arkVersions
import java.nio.file.Path

class RootVersionsStorage(
    private val scope: CoroutineScope,
    private val root: Path
):
    FileStorage<Versions>("versions", scope, root.arkFolder().arkVersions(), VersionsMonoid),
    VersionsStorage {

    override fun valueFromString(raw: String): Versions =
        raw.split(",").filter { it.isNotEmpty() }.map {
            ResourceId.fromString(it)
        }.toSet()

    override fun valueToString(value: Versions): String = value.joinToString(",")
}
