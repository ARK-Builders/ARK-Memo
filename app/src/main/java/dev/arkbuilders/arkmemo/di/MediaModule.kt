package dev.arkbuilders.arkmemo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.arkbuilders.arkmemo.media.ArkMediaPlayer
import dev.arkbuilders.arkmemo.media.ArkMediaPlayerImpl
import dev.arkbuilders.arkmemo.media.ArkAudioRecorder
import dev.arkbuilders.arkmemo.media.ArkAudioRecorderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    abstract fun bindArkAudioRecorder(impl: ArkAudioRecorderImpl): ArkAudioRecorder

    @Binds
    abstract fun bindArkMediaPlayer(impl: ArkMediaPlayerImpl): ArkMediaPlayer
}
