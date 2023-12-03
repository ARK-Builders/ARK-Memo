package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arkmemo.data.repo.GraphicNotesRepo
import dev.arkbuilders.arkmemo.data.repo.NotesRepo
import dev.arkbuilders.arkmemo.data.repo.TextNotesRepo
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.TextNote


@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindTextNotesRepo(impl: TextNotesRepo): NotesRepo<TextNote>

    @Binds
    abstract fun bindGraphicNotesRepo(impl: GraphicNotesRepo): NotesRepo<GraphicNote>
}