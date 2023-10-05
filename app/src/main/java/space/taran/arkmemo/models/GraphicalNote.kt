package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.taran.arkmemo.data.ResourceMeta

@Parcelize
data class GraphicalNote(
    val svgData: String,
    @IgnoredOnParcel
    val meta: ResourceMeta? = null
) : Parcelable