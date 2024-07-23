package dev.arkbuilders.arkmemo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.arkbuilders.arklib.initArkLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender
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
        initAcra()
    }

    private fun initAcra() = CoroutineScope(Dispatchers.IO).launch {
        val enabled = memoPreferences.getCrashReportEnabled()
        if (!enabled) return@launch

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            dialog {
                text = getString(R.string.crash_dialog_desc)
                title = getString(R.string.crash_dialog_title)
                commentPrompt = getString(R.string.crash_dialog_comment)
            }
            httpSender {
                uri = BuildConfig.ACRA_URI
                basicAuthLogin = BuildConfig.ACRA_LOGIN
                basicAuthPassword = BuildConfig.ACRA_PASS
                httpMethod = HttpSender.Method.POST
            }
        }
    }
}
