package space.taran.arkmemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import space.taran.arkfilepicker.presentation.onArkPathPicked
import space.taran.arkmemo.R
import space.taran.arkmemo.files.FilePicker
import space.taran.arkmemo.preferences.MemoPreferences
import space.taran.arkmemo.ui.activities.hideSettingsButton
import space.taran.arkmemo.ui.views.PathPreference

class Settings : PreferenceFragmentCompat() {

    private val activity: AppCompatActivity by lazy{
        requireActivity() as AppCompatActivity
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (savedInstanceState == null) {
            activity.title = getString(R.string.settings)
            hideSettingsButton()
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(savedInstanceState == null) {
            val pathKey = getString(R.string.path_pref_key)
            val pathPref: PathPreference? = findPreference(pathKey)

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
    }

    companion object{
        const val TAG = "Settings"
    }
}