package dev.arkbuilders.arkmemo.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openLink(url: String) {
    startActivity(
        Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
}