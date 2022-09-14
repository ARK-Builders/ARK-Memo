package space.taran.arkmemo.models

data class TextNote (
    var title: String = "Test note",
    var contents: String = "Lorem ipsum",
    val  date: String = "01/01/2022"
    )