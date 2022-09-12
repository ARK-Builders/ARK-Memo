package space.taran.arkmemo

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

@HiltAndroidApp
class App: Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        initAcra{
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            dialog{
                text = getString(R.string.crash_dialog_desc)
                title = getString(R.string.crash_dialog_title)
                commentPrompt = getString(R.string.crash_dialog_comment)
            }

            httpSender{
                uri = BuildConfig.ACRA_URI
                basicAuthLogin = BuildConfig.ACRA_LOGIN
                basicAuthPassword = BuildConfig.ACRA_PASS
                httpMethod = HttpSender.Method.POST
            }
        }
    }
}