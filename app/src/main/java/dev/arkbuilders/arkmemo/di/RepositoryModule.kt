package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.repo.graphics.GraphicNotesRepo
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.repo.text.TextNotesRepo
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.repo.NotesRepoHelper


@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindTextNotesRepo(impl: TextNotesRepo): NotesRepo<TextNote>

    @Binds
    abstract fun bindGraphicNotesRepo(impl: GraphicNotesRepo): NotesRepo<GraphicNote>

    companion object {
        @Provides
        fun provideNotesRepoHelper(
            memoPreferences: MemoPreferences,
            propertiesStorageRepo: PropertiesStorageRepo
        ) = NotesRepoHelper(memoPreferences, propertiesStorageRepo)
    }
}