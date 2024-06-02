package dev.arkbuilders.arkmemo.utils

import android.content.Context
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.GraphicNote
import dev.arkbuilders.arkmemo.models.Note
import dev.arkbuilders.arkmemo.models.TextNote
import dev.arkbuilders.arkmemo.models.VoiceNote

fun Note.getAutoTitle(context: Context? = null): String {

    return if (this is TextNote) {
        this.title.ifEmpty { this.text.take(20) }.ifEmpty {
            context?.getString(R.string.ark_memo_default_text_note_title) ?: ""
        }
    } else if (this is GraphicNote && context != null) {
        this.title.ifEmpty {
            String.format(context.getString(R.string.ark_memo_graphic_note), this.resource?.id)
        }
    } else if (this is VoiceNote && context != null) {
        this.title.ifEmpty {
            String.format(context.getString(R.string.ark_memo_voice_note), this.resource?.id)
        }
    } else {
        ""
    }
}