package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arkmemo.data.repositories.GraphicNotesRepo
import space.taran.arkmemo.data.repositories.NotesRepo
import dev.arkbuilders.arkmemo.data.repositories.TextNotesRepo
import space.taran.arkmemo.models.GraphicNote
import space.taran.arkmemo.models.TextNote


@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindTextNotesRepo(impl: TextNotesRepo): NotesRepo<TextNote>

    @Binds
    abstract fun bindGraphicNotesRepo(impl: GraphicNotesRepo): NotesRepo<GraphicNote>
}