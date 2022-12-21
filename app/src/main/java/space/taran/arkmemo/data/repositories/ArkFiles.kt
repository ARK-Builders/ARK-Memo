package space.taran.arkmemo.data.repositories

import space.taran.arkmemo.data.repositories.ArkFiles.ARK_FOLDER
import space.taran.arkmemo.data.repositories.ArkFiles.VERSIONS_FOLDER
import java.nio.file.Path

object ArkFiles {
    const val ARK_FOLDER = ".ark"
    const val VERSIONS_FOLDER = "versions"
}

fun Path.arkFolder() = resolve(ARK_FOLDER)
fun Path.arkVersions() = resolve(VERSIONS_FOLDER)