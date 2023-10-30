package space.taran.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.taran.arkmemo.data.repositories.TextNotesRepo
import space.taran.arkmemo.data.repositories.TextNotesRepoImpl


@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(impl: TextNotesRepoImpl): TextNotesRepo
}