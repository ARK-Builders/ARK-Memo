package dev.arkbuilders.arkmemo.ui.views.resourceloader

import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager
import java.nio.file.Path

interface CanvasResourceLoader {
    suspend fun loadResourceInto(path: Path, editManager: EditManager)
    suspend fun getResource()
}