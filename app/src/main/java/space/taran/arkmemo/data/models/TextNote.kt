package space.taran.arkmemo.data.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import space.taran.arkmemo.data.ResourceMeta
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextNote (
    var content: Content,
    @IgnoredOnParcel
    val meta: ResourceMeta? = null
): Parcelable
{
    fun putContent(content: Content) {
        this.content = content
    }

    fun isNotEmpty() = content.data.isNotEmpty()

    @Parcelize
    data class Content(
        val title: String,
        val data: String
        ): Parcelable
}