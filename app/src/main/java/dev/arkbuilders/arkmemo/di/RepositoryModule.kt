package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arkmemo.data.repositories.TextNotesRepo
import dev.arkbuilders.arkmemo.data.repositories.TextNotesRepoImpl


@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(impl: TextNotesRepoImpl): TextNotesRepo
}