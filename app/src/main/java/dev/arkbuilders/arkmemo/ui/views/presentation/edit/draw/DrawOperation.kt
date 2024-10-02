package dev.arkbuilders.arkmemo.ui.views.presentation.edit.draw

import androidx.compose.ui.graphics.Path
import dev.arkbuilders.arkmemo.ui.views.presentation.drawing.EditManager
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.Operation

class DrawOperation(private val editManager: EditManager) : Operation {
    private var path = Path()

    override fun apply() {
        editManager.addDrawPath(path)
    }

    override fun undo() {
        editManager.apply {
            if (drawPaths.isNotEmpty()) {
                redoPaths.push(drawPaths.pop())
                updateRevised()
                return
            }
        }
    }

    override fun redo() {
        editManager.apply {
            if (redoPaths.isNotEmpty()) {
                drawPaths.push(redoPaths.pop())
                updateRevised()
                return
            }
        }
    }

    fun draw(path: Path) {
        this.path = path
    }
}
