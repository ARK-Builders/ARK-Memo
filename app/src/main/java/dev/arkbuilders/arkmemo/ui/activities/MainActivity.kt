package dev.arkbuilders.arkmemo.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkfilepicker.presentation.onArkPathPicked
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.contracts.PermissionContract
import dev.arkbuilders.arkmemo.databinding.ActivityMainBinding
import dev.arkbuilders.arkmemo.ui.dialogs.FilePickerDialog
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.SettingsFragment
import dev.arkbuilders.arkmemo.ui.fragments.NotesFragment
import dev.arkbuilders.arkmemo.utils.replaceFragment
import dev.arkbuilders.arkmemo.utils.resumeFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::bind)

    @Inject
    lateinit var memoPreferences: MemoPreferences

    @IdRes
    private val fragContainer = R.id.container

    private var menu: Menu? = null

    var fragment: Fragment = NotesFragment()

    init {
        FilePickerDialog.readPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) FilePickerDialog.show()
                else finish()
            }

        FilePickerDialog.readPermLauncher_SDK_R =
            registerForActivityResult(PermissionContract()) { isGranted ->
                if (isGranted) FilePickerDialog.show()
                else finish()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d("tuancoltech", "onCreate " + this)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fun showFragment() {
            val textDataFromIntent = intent?.getStringExtra(Intent.EXTRA_TEXT)
            if (textDataFromIntent != null) {
                fragment = EditTextNotesFragment.newInstance(textDataFromIntent)
                supportFragmentManager.beginTransaction().apply {
                    replace(fragContainer, fragment, EditTextNotesFragment.TAG)
                    commit()
                }
            } else {
                if (savedInstanceState == null)
                    supportFragmentManager.beginTransaction().apply {
                        add(fragContainer, fragment, NotesFragment.TAG)
                        commit()
                    }
                else {
                    supportFragmentManager.apply {
                        val tag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG)!!
                        fragment = findFragmentByTag(tag)!!
                        if (!fragment.isInLayout)
                            resumeFragment(fragment)
                    }
                }
            }
        }

        if (memoPreferences.getPath().isEmpty()) {
            FilePickerDialog.show(this, supportFragmentManager)

            supportFragmentManager.onArkPathPicked(this) {
                memoPreferences.storePath(it.toString())
                showFragment()
            }
        }
        else showFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        if(fragment.tag != NotesFragment.TAG)
            showSettingsButton(false)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_FRAGMENT_TAG, fragment.tag)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                fragment = SettingsFragment()
                replaceFragment(fragment, SettingsFragment.TAG)
            }
        }
        return true
    }

    fun showSettingsButton(show: Boolean = true){
        if(menu != null) {
            val settingsItem = menu?.findItem(R.id.settings)
            settingsItem?.isVisible = show
        }
    }

    fun showProgressBar(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    companion object{
        private const val CURRENT_FRAGMENT_TAG = "current fragment tag"
    }
}
