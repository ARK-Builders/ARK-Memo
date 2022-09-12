package space.taran.arkmemo.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import space.taran.arkfilepicker.onArkPathPicked
import space.taran.arkmemo.R
import space.taran.arkmemo.contracts.PermissionContract
import space.taran.arkmemo.databinding.ActivityMainBinding
import space.taran.arkmemo.files.FilePicker
import space.taran.arkmemo.preferences.MemoPreferences
import space.taran.arkmemo.ui.fragments.TextNotes

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() =  _binding!!
    @IdRes
    private val fragContainer = R.id.container

    init{
        FilePicker.readPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted) FilePicker.show()
            else finish()
        }

        FilePicker.readPermLauncher_SDK_R = registerForActivityResult(PermissionContract()){ isGranted ->
            if(isGranted) FilePicker.show()
            else finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null){
            _binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setSupportActionBar(binding.toolbar)

            if(MemoPreferences.getInstance(this).getPath() == null)
                FilePicker.show(this, supportFragmentManager)

            supportFragmentManager.beginTransaction().apply{
                add(fragContainer, TextNotes(), TextNotes.TAG)
                commit()
            }

            supportFragmentManager.onArkPathPicked(this){
                MemoPreferences.getInstance(this).storePath(it.toString())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings ->{}
        }
        return true
    }

    override fun onDestroy(){
        super.onDestroy()
        _binding = null
    }
}