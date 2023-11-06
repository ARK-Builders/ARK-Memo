package dev.arkbuilders.arkmemo.files.parsers

import com.google.gson.Gson
import dev.arkbuilders.arkmemo.models.TextNote

class JsonParser {
    companion object{
        private val gson = Gson()

        fun parseNoteToJson(note: TextNote.Content): String =
            gson.toJson(note, TextNote.Content::class.java)

        fun parseNoteFromJson(json: String): TextNote.Content =
            gson.fromJson(json, TextNote.Content::class.java)

    }
}