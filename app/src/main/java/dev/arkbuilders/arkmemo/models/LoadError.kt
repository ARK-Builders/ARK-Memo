package dev.arkbuilders.arkmemo.models

sealed interface LoadError

data class RootNotFound(val rootPath: String) : LoadError
