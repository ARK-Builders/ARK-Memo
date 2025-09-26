package dev.arkbuilders.arkmemo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PropertiesStorageModule {
    @Singleton
    @Provides
    @Named(STORAGE_SCOPE)
    fun storageScope(
        @Named(IO_DISPATCHER) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(dispatcher)

    @Singleton
    @Provides
    fun propertiesStorageRepo(
        @Named(STORAGE_SCOPE) storageScope: CoroutineScope,
    ): PropertiesStorageRepo {
        return PropertiesStorageRepo(storageScope)
    }

    const val STORAGE_SCOPE = "storageScope"
}
