package dev.arkbuilders.arkmemo.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.arkbuilders.arkmemo.di.IO_DISPATCHER
import dev.arkbuilders.arkmemo.utils.dpToPx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class QRViewModel
    @Inject
    constructor(
        @Named(IO_DISPATCHER) private val iODispatcher: CoroutineDispatcher,
        @ApplicationContext private val appContext: Context,
    ) : ViewModel() {
        private val tag = "QRViewModel"

        fun generateQRCode(
            text: String,
            onSuccess: (bitmap: Bitmap) -> Unit,
        ) {
            Log.d(tag, "generateQRCode")
            viewModelScope.launch(iODispatcher) {
                // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
                val qrgEncoder = QRGEncoder(text, null, QRGContents.Type.TEXT, 300.dpToPx())
                qrgEncoder.colorBlack = Color.BLACK
                qrgEncoder.colorWhite = Color.WHITE
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(qrgEncoder.getBitmap(0))
                }
            }
        }

        fun saveQRCodeImage(
            text: String,
            bitmap: Bitmap,
            onSuccess: (path: String) -> Unit,
        ) {
            Log.d(tag, "saveQRCodeImage")
            viewModelScope.launch {
                // Save with location, value, bitmap returned and type of Image(JPG/PNG).
                val qrgSaver = QRGSaver()

                val savePath =
                    (appContext.getExternalFilesDir(null)?.path + "/images/").apply {
                        File(this).mkdirs()
                    }

                val isSuccess =
                    qrgSaver.save(
                        savePath,
                        text,
                        bitmap,
                        QRGContents.ImageType.IMAGE_JPEG,
                    )

                if (isSuccess) {
                    onSuccess.invoke(savePath)
                }
            }
        }
    }
