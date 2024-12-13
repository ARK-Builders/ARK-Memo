package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.models.VoiceNote
import dev.arkbuilders.arkmemo.repo.NotesRepo
import dev.arkbuilders.arkmemo.repo.NotesRepoHelper
import dev.arkbuilders.arkmemo.repo.graphics.GraphicNotesRepo
import dev.arkbuilders.arkmemo.repo.text.TextNotesRepo
import dev.arkbuilders.arkmemo.repo.voices.VoiceNotesRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindTextNotesRepo(impl: TextNotesRepo): NotesRepo<TextNote>

    @Binds
    abstract fun bindGraphicNotesRepo(impl: GraphicNotesRepo): NotesRepo<GraphicNote>

    @Binds
    abstract fun bindVoiceNotesRepo(impl: VoiceNotesRepo): NotesRepo<VoiceNote>
}

@InstallIn(SingletonComponent::class)
@Module
object RepoHelperModule {
    @Singleton
    @Provides
    fun provideNotesRepoHelper(
        propertiesStorageRepo: PropertiesStorageRepo,
        @Named(IO_DISPATCHER) coroutineDispatcher: CoroutineDispatcher,
    ) = NotesRepoHelper(propertiesStorageRepo, coroutineDispatcher)
}
