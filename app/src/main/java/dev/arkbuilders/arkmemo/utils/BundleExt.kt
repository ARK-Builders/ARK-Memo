package dev.arkbuilders.arkmemo.utils

import android.os.Build
import android.os.Bundle

fun <T> Bundle?.getParcelableCompat(
    key: String,
    clazz: Class<T>,
): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        this?.getParcelable(key)
    }
}
