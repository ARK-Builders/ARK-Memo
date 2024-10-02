package dev.arkbuilders.arkmemo.ui.views.presentation.edit

interface Operation {
    fun apply()

    fun undo()

    fun redo()
}
