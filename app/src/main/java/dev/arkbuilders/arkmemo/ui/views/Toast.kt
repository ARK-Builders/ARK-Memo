package dev.arkbuilders.arkmemo.ui.views

import android.content.Context
import android.widget.Toast

fun toast(context: Context, string: String) {
    Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
}