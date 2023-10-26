package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.taran.arkmemo.data.ResourceMeta

@Parcelize
open class BaseNote(
    val resourceContent: Content,
    @IgnoredOnParcel
    var resourceMeta: ResourceMeta? = null
) : Parcelable

@Parcelize
data class Content(
    val title: String,
    val data: String
): Parcelable
