package space.taran.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.taran.arkmemo.preferences.MemoPreferences
import space.taran.arkmemo.preferences.MemoPreferencesImpl

@InstallIn(SingletonComponent::class)
@Module
abstract class PreferencesModule {
    @Binds
    abstract fun bindRepository(impl: MemoPreferencesImpl): MemoPreferences
}