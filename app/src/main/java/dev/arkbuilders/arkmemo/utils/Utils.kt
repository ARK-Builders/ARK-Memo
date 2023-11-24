package dev.arkbuilders.arkmemo.utils

import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.models.SaveNoteResult
import dev.arkbuilders.arkmemo.ui.views.toast

fun Fragment.observeSaveResult(result: LiveData<SaveNoteResult>) {
    result.observe(this) {
        if (!isResumed) return@observe

        if (it == SaveNoteResult.SUCCESS) {
            toast(requireContext(), getString(R.string.ark_memo_note_saved))
            activity?.onBackPressedDispatcher?.onBackPressed()
        } else {
            toast(requireContext(), getString(R.string.ark_memo_note_existing))
        }
    }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, tag: String) {
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

fun AppCompatActivity.resumeFragment(fragment: Fragment){
    supportFragmentManager.beginTransaction().apply{
        show(fragment)
        commit()
    }
}

fun Context.getTextFromClipBoard(): String?{
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
}
