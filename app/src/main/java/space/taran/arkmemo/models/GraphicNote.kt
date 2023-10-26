package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.utils.SVG

@Parcelize
data class GraphicNote(
    val content: Content,
    @IgnoredOnParcel
    val svg: SVG? = null,
    @IgnoredOnParcel
    var meta: ResourceMeta? = null
) : BaseNote(content, meta), Parcelable