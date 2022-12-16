package space.taran.arkmemo.space.taran.arkmemo.utils

// shared preferences
const val CRASH_REPORT_ENABLE = "crash_report_enable"

enum class CODES_CREATING_NOTE(val errCode: Int) {
    NOTE_NOT_CREATED(-1),
    NOTE_ALREADY_EXISTS(-2),
}

enum class CODES_DELETING_NOTE(val code: Int) {
    SUCCESS(0),
    SUCCESS_NOTE_AND_VERSION_DELETED(1),
    SUCCESS_NOTE_DELETED_VERSION_CHANGED(2),
    NOTE_NOT_DELETED(-1),
}