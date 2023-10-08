package dev.arkbuilders.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import dev.arkbuilders.arkfilepicker.onArkPathPicked
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.files.FilePicker
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.ui.activities.MainActivity
import dev.arkbuilders.arkmemo.ui.views.PathPreference

class SettingsFragment : PreferenceFragmentCompat() {

    private val activity: MainActivity by lazy{
        requireActivity() as MainActivity
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pathKey = getString(R.string.path_pref_key)
        val pathPref: PathPreference? = findPreference(pathKey)
        activity.title = getString(R.string.settings)
        activity.showSettingsButton(false)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pathPref?.setOnPreferenceClickListener {
            FilePicker.show(activity, parentFragmentManager)
            true
        }

        parentFragmentManager.onArkPathPicked(viewLifecycleOwner) {
            val pathString = it.toString()
            MemoPreferences.getInstance(requireContext()).storePath(pathString)
            pathPref?.setPath(pathString)
        }
    }

    override fun onResume(){
        super.onResume()
        activity.fragment = this
    }

    companion object{
        const val TAG = "Settings"
    }
}