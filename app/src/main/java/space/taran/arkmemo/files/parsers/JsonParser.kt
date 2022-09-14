package space.taran.arkmemo.files.parsers

import com.google.gson.Gson
import space.taran.arkmemo.models.TextNote

class JsonParser {
    companion object{
        private val gson = Gson()

        fun parseNoteToJson(note: TextNote): String =
            gson.toJson(note, TextNote::class.java)

        fun parseNoteFromJson(json: String): TextNote =
            gson.fromJson(json, TextNote::class.java)

    }
}