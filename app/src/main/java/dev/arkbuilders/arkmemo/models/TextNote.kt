package dev.arkbuilders.arkmemo.models

import android.os.Parcelable
import dev.arkbuilders.arklib.data.index.Resource
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextNote (
    override val title: String = "",
    override val description: String = "",
    val text: String = "",
    @IgnoredOnParcel
    override var resource: Resource? = null,
    override var selected: Boolean = false
): Note, Parcelable
