package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.preferences.MemoPreferencesImpl

@InstallIn(SingletonComponent::class)
@Module
abstract class PreferencesModule {
    @Binds
    abstract fun bindMemoPreferences(impl: MemoPreferencesImpl): MemoPreferences
}