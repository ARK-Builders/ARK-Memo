package dev.arkbuilders.arkmemo.graphics.models

import android.os.Parcelable
import dev.arkbuilders.arklib.data.index.Resource
import dev.arkbuilders.arkmemo.graphics.SVG
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class GraphicNote(
    override val title: String = "",
    override val description: String = "",
    @IgnoredOnParcel
    val svg: SVG? = null,
    @IgnoredOnParcel
    override var resource: Resource? = null
) : Note, Parcelable