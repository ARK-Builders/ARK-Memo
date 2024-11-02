package dev.arkbuilders.arkmemo.ui.views.presentation.main

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.arkbuilders.arkmemo.ui.views.data.Resolution
import dev.arkbuilders.arkmemo.ui.views.presentation.utils.PermissionsHelper
import dev.arkbuilders.arkmemo.ui.views.presentation.edit.EditScreen
import dev.arkbuilders.arkmemo.ui.views.presentation.utils.isWritePermGranted
import dev.arkbuilders.arkmemo.ui.views.presentation.picker.PickerScreen
import dev.arkbuilders.arkmemo.ui.views.presentation.theme.ARKRetouchTheme
import kotlin.io.path.Path

private const val REAL_PATH_KEY = "real_file_path_2"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ARKRetouchTheme {
                MainScreen(
                    supportFragmentManager,
                    uri = intent.data?.toString(),
                    realPath = intent.getStringExtra(REAL_PATH_KEY),
                    launchedFromIntent = intent.data != null,
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    fragmentManager: FragmentManager,
    uri: String?,
    realPath: String?,
    launchedFromIntent: Boolean = false,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    var maxResolution by remember { mutableStateOf(Resolution(0, 0)) }
    val startScreen =
        if ((uri != null || realPath != null) && context.isWritePermGranted())
            NavHelper.editRoute
        else
            NavHelper.pickerRoute

    val launcher = rememberLauncherForActivityResult(
        contract = PermissionsHelper.writePermContract()
    ) { isGranted ->
        if (!isGranted) return@rememberLauncherForActivityResult
        if (launchedFromIntent) {
            navController.navigate(
                NavHelper.parseEditArgs(realPath, uri, true)
            )
        }
    }

    SideEffect {
        if (!context.isWritePermGranted())
            PermissionsHelper.launchWritePerm(launcher)
    }

    NavHost(
        navController = navController,
        startDestination = startScreen
    ) {
        composable(NavHelper.pickerRoute) {
            PickerScreen(
                fragmentManager,
                onNavigateToEdit = { path, resolution ->
                    maxResolution = resolution
                    navController.navigate(
                        NavHelper.parseEditArgs(
                            path?.toString(),
                            uri = null,
                            launchedFromIntent = false,
                        )
                    )
                },
            )
        }
        composable(
            route = NavHelper.editRoute,
            arguments = listOf(
                navArgument("path") {
                    type = NavType.StringType
                    defaultValue = realPath
                    nullable = true
                },
                navArgument("uri") {
                    type = NavType.StringType
                    defaultValue = uri
                    nullable = true
                },
                navArgument("launchedFromIntent") {
                    type = NavType.BoolType
                    defaultValue = launchedFromIntent
                },
            )
        ) { entry ->
            EditScreen(
                entry.arguments?.getString("path")?.let { Path(it) },
                entry.arguments?.getString("uri"),
                fragmentManager,
                navigateBack = { navController.popBackStack() },
                entry.arguments?.getBoolean("launchedFromIntent")!!,
                maxResolution,
                {}
            )
        }
    }
}

private object NavHelper {
    const val editRoute =
        "edit?path={path}&uri={uri}&launchedFromIntent={launchedFromIntent}"

    const val pickerRoute = "picker"

    fun parseEditArgs(
        path: String?,
        uri: String?,
        launchedFromIntent: Boolean,
    ): String {
        val screen = if (path != null) {
            "edit?path=$path&launchedFromIntent=$launchedFromIntent"
        } else if (uri != null) {
            "edit?uri=$uri&launchedFromIntent=$launchedFromIntent"
        } else {
            "edit"
        }
        return screen
    }
}
