package dev.arkbuilders.arkmemo.models

import android.os.Parcelable
import dev.arkbuilders.arkmemo.data.ResourceMeta
import kotlinx.parcelize.Parcelize

@Parcelize
open class BaseNote(
    val resourceTitle: String,
    val resourceDesc: String = "",
    val resourceContent: Content,
    var resourceMeta: ResourceMeta? = null
) : Parcelable

@Parcelize
data class Content(
    val data: String
): Parcelable
