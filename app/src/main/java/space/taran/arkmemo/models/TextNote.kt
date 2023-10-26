package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import space.taran.arkmemo.data.ResourceMeta
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextNote (
    val content: Content,
    @IgnoredOnParcel
    var meta: ResourceMeta? = null
): BaseNote(content, meta), Parcelable
