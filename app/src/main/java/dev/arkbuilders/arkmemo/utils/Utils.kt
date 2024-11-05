package dev.arkbuilders.arkmemo.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.ui.views.toast
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.forEachLine

fun Fragment.observeSaveResult(result: LiveData<SaveNoteResult>) {
    result.observe(viewLifecycleOwner) {
        if (!isResumed) return@observe

        if (it == SaveNoteResult.SUCCESS_NEW || it == SaveNoteResult.SUCCESS_UPDATED) {
            context?.let { ctx ->
                toast(ctx, getString(R.string.ark_memo_note_saved))
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        } else {
            context?.let { ctx -> toast(ctx, getString(R.string.ark_memo_note_existing)) }
        }
    }
}

fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    tag: String,
) {
    supportFragmentManager.beginTransaction().apply {
        val backStackName = fragment.javaClass.name
        val popBackStack = supportFragmentManager.popBackStackImmediate(backStackName, 0)
        if (!popBackStack) {
            replace(R.id.container, fragment, tag)
            addToBackStack(backStackName)
        } else {
            show(fragment)
        }
        commit()
    }
}

fun AppCompatActivity.resumeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().apply {
        show(fragment)
        commit()
    }
}

fun Context.getTextFromClipBoard(
    view: View?,
    onSuccess: (text: String?) -> Unit,
) {
    view?.post {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        onSuccess.invoke(clipboardManager.primaryClip?.getItemAt(0)?.text?.toString())
    } ?: return
}

fun Context.copyToClipboard(
    label: String,
    text: String,
) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
}

fun <R> Path.listFiles(
    extension: String,
    process: (Path) -> R,
): List<R> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        Files.list(this).toList().filter { it.extension == extension }.map {
            process(it)
        }
    } else {
        File(this.toString()).listFiles()?.filter { it.extension == extension }?.map { it ->
            process(it.toPath())
        } ?: emptyList()
    }

fun <R> Path.readLines(useLines: (String) -> R): R {
    val dataBuilder = StringBuilder()
    forEachLine {
        dataBuilder.appendLine(it)
    }
    return useLines(dataBuilder.removeSuffix("\n").toString())
}

fun tenthSecondsToString(duration: Long): String {
    val seconds = duration / 10
    val remainingMilliSeconds = duration % 10
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${
        if (minutes <= 9) "0$minutes" else minutes
    }:${
        if (remainingSeconds <= 9) "0$remainingSeconds" else remainingSeconds
    }:0$remainingMilliSeconds"
}

fun millisToString(duration: Long): String {
    val seconds = duration / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${
        if (minutes <= 9) "0$minutes" else minutes
    }:${
        if (remainingSeconds <= 9) "0$remainingSeconds" else remainingSeconds
    }"
}

/**
 * Extract duration of a media file and return it in human-readable format
 */
fun extractDuration(path: String): String {
    return try {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(path)
        val duration =
            metadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION,
            )?.toLong() ?: 0L
        millisToString(duration)
    } catch (e: Exception) {
        Log.e("ExtractDuration", "extractDuration exception: " + e.message)
        ""
    }
}
