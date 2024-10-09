package dev.arkbuilders.arkmemo.utils

import android.content.Context
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.models.VoiceNote

fun Note.getAutoTitle(context: Context? = null): String {
    return if (context != null) {
        when (this) {
            is TextNote -> {
                title.ifEmpty { text.take(20) }.ifEmpty {
                    context.getString(R.string.ark_memo_default_text_note_title)
                }
            }

            is GraphicNote -> {
                title.ifEmpty {
                    String.format(
                        context.getString(R.string.ark_memo_graphic_note), resource?.id
                    )
                }
            }

            is VoiceNote -> {
                title.ifEmpty {
                    String.format(
                        context.getString(R.string.ark_memo_voice_note), resource?.id
                    )
                }
            }

            else -> { "" }
        }
    } else { "" }
}