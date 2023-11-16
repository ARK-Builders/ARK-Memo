package dev.arkbuilders.arkmemo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arklib.user.properties.PropertiesStorageRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PropertiesStorageModule {

    @Singleton
    @Provides
    @Named(STORAGE_SCOPE)
    fun storageScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)

    @Singleton
    @Provides
    fun propertiesStorageRepo(
        @Named(STORAGE_SCOPE) storageScope: CoroutineScope
    ): PropertiesStorageRepo {
        return PropertiesStorageRepo(storageScope)
    }

    const val STORAGE_SCOPE = "storageScope"
}