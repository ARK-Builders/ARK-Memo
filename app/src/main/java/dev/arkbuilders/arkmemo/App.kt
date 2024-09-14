package dev.arkbuilders.arkmemo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.arkbuilders.arklib.initArkLib
import dev.arkbuilders.arkfilepicker.folders.FoldersRepo
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {

    @Inject
    lateinit var memoPreferences: MemoPreferences

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("arklib")
        initArkLib()
        FoldersRepo.init(this)
    }
}
