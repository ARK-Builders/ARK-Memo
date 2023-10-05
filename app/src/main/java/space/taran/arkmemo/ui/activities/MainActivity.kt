package space.taran.arkmemo.ui.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkfilepicker.presentation.onArkPathPicked
import space.taran.arkmemo.R
import space.taran.arkmemo.contracts.PermissionContract
import space.taran.arkmemo.databinding.ActivityMainBinding
import space.taran.arkmemo.files.FilePicker
import space.taran.arkmemo.preferences.MemoPreferences
import space.taran.arkmemo.ui.fragments.EditTextNotesFragment
import space.taran.arkmemo.ui.fragments.SettingsFragment
import space.taran.arkmemo.ui.fragments.TextNotesFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::bind)

    @IdRes
    private val fragContainer = R.id.container

    private var menu: Menu? = null

    var fragment: Fragment = TextNotesFragment()

    init {
        FilePicker.readPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) FilePicker.show()
                else finish()
            }

        FilePicker.readPermLauncher_SDK_R =
            registerForActivityResult(PermissionContract()) { isGranted ->
                if (isGranted) FilePicker.show()
                else finish()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("arklib")

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        fun showFragment(){
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
                        add(fragContainer, fragment, TextNotesFragment.TAG)
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

        if (MemoPreferences.getInstance(this).getPath() == null) {
            FilePicker.show(this, supportFragmentManager)

            supportFragmentManager.onArkPathPicked(this) {
                MemoPreferences.getInstance(this).storePath(it.toString())
                showFragment()
            }
        }
        else showFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        if(fragment.tag != TextNotesFragment.TAG)
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

    fun showFragment(){

    }

    companion object{
        private const val CURRENT_FRAGMENT_TAG = "current fragment tag"
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
    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    return clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
}
