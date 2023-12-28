package space.taran.arkmemo.utils

import java.nio.file.Path

fun Path.arkVersions(): Path = resolve("versions")