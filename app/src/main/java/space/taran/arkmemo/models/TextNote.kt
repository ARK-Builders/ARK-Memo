package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.taran.arkmemo.data.ResourceMeta

@Parcelize
data class TextNote(
    val content: Content,
    @IgnoredOnParcel
    val meta: ResourceMeta? = null
) : Parcelable {
    @Parcelize
    data class Content(
        val title: String,
        val data: String
    ) : Parcelable
}