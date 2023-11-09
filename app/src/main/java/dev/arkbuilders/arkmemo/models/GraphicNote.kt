package space.taran.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.taran.arkmemo.data.ResourceMeta
import space.taran.arkmemo.utils.SVG

@Parcelize
data class GraphicNote(
    val title: String,
    val description: String = "",
    val content: Content,
    @IgnoredOnParcel
    val svg: SVG? = null,
    private val meta: ResourceMeta? = null
) : BaseNote(title, description, content, meta), Parcelable