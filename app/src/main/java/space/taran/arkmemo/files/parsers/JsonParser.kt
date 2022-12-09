package space.taran.arkmemo.files.parsers

import com.google.gson.Gson
import space.taran.arkmemo.models.TextNote
import space.taran.arkmemo.models.Version

class JsonParser {
    companion object{
        private val gson = Gson()

        fun parseNoteContentToJson(note: TextNote.Content): String =
            gson.toJson(note, TextNote.Content::class.java)

        fun parseNoteContentFromJson(json: String): TextNote.Content =
            gson.fromJson(json, TextNote.Content::class.java)

        fun parseVersionContentToJson(note: Version.Content): String =
            gson.toJson(note, Version.Content::class.java)

        fun parseVersionContentFromJson(json: String): Version.Content =
            gson.fromJson(json, Version.Content::class.java)

    }
}