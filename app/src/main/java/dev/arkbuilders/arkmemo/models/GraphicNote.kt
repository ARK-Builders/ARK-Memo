package dev.arkbuilders.arkmemo.models

import android.graphics.Bitmap
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
    override var resource: Resource? = null,
    override var pendingForDelete: Boolean = false,
    var thumb: Bitmap? = null,
    override var selected: Boolean = false,
) : Note, Parcelable
