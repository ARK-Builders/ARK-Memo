package dev.arkbuilders.arkmemo.models

import android.os.Parcelable
import dev.arkbuilders.arkmemo.data.ResourceMeta
import dev.arkbuilders.arkmemo.utils.SVG
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class GraphicNote(
    val title: String,
    val description: String = "",
    val content: Content,
    @IgnoredOnParcel
    val svg: SVG? = null,
    private val meta: ResourceMeta? = null
) : BaseNote(title, description, content, meta), Parcelable