package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import space.taran.arkmemo.data.ResourceMeta
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextNote (
    val title: String,
    val description: String = "",
    val content: Content,
    private val meta: ResourceMeta? = null
): BaseNote(title, description, content, meta), Parcelable