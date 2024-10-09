package dev.arkbuilders.arkmemo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(val value: String) : Parcelable
