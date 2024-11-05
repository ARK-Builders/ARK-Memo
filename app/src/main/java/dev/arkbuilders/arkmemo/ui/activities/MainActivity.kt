package dev.arkbuilders.arkmemo.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.arkbuilders.arkfilepicker.presentation.onArkPathPicked
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.contracts.PermissionContract
import dev.arkbuilders.arkmemo.databinding.ActivityMainBinding
import dev.arkbuilders.arkmemo.preferences.MemoPreferences
import dev.arkbuilders.arkmemo.ui.dialogs.FilePickerDialog
import dev.arkbuilders.arkmemo.ui.fragments.BaseFragment
import dev.arkbuilders.arkmemo.ui.fragments.EditTextNotesFragment
import dev.arkbuilders.arkmemo.ui.fragments.NotesFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind)

    @Inject
    lateinit var memoPreferences: MemoPreferences

    @IdRes
    private val fragContainer = R.id.container

    var fragment: Fragment = NotesFragment()

    init {
        FilePickerDialog.readPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    FilePickerDialog.show()
                } else {
                    finish()
                }
            }

        FilePickerDialog.readPermLauncherSdkR =
            registerForActivityResult(PermissionContract()) { isGranted ->
                if (isGranted) {
                    FilePickerDialog.show()
                } else {
                    finish()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setStatusBarColor(ContextCompat.getColor(this, R.color.white), true)
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
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction().apply {
                        add(fragContainer, fragment, NotesFragment.TAG)
                        commit()
                    }
                } else {
                    supportFragmentManager.apply {
                        val tag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG)
                        findFragmentByTag(tag)?.let {
                            fragment = it
                            if (!fragment.isInLayout) {
                                resumeFragment(fragment)
                            }
                        }
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
        } else {
            showFragment()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_FRAGMENT_TAG, fragment.tag)
        super.onSaveInstanceState(outState)
    }

    fun showProgressBar(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    fun initEditUI() {
        title = getString(R.string.edit_note)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setStatusBarColor(
        color: Int,
        isLight: Boolean,
    ) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLight
    }

    override fun onBackPressed() {
        if (fragment is BaseFragment) {
            (fragment as BaseFragment).onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val CURRENT_FRAGMENT_TAG = "current fragment tag"
    }
}

fun AppCompatActivity.resumeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().apply {
        show(fragment)
        commit()
    }
}
