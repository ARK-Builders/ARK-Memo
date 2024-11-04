package dev.arkbuilders.arkmemo.utils

fun String.insertStringAtPosition(stringToInsert: String, position: Int): String {
    if (position < 0 || position > this.length) {
        throw IndexOutOfBoundsException("Position is out of bounds of the string")
    }

    val stringBuilder = StringBuilder(this)
    stringBuilder.insert(position, stringToInsert)
    return stringBuilder.toString()
}
