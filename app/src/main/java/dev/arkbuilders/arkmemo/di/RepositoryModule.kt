package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arkmemo.repo.text.TextNotesRepo
import dev.arkbuilders.arkmemo.repo.text.TextNotesRepoImpl
import dev.arkbuilders.arkmemo.repo.versions.VersionStorageRepo
import dev.arkbuilders.arkmemo.preferences.MemoPreferences


@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(impl: TextNotesRepoImpl): TextNotesRepo

    companion object {
        @Provides
        fun provideVersionStorageRepo(
            memoPreferences: MemoPreferences
        ) = VersionStorageRepo(memoPreferences)
    }
}