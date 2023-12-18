package dev.arkbuilders.arkmemo.models

import android.os.Parcelable
import dev.arkbuilders.arklib.data.index.Resource

interface Note: Parcelable {
    val title: String
    val description: String
    var resource: Resource?
    var isForked: Boolean
}