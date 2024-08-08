package dev.arkbuilders.arkmemo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesModule {

    @Named(IO_DISPATCHER)
    @Singleton
    @Provides
    fun provideDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

const val IO_DISPATCHER = "iODispatcher"
