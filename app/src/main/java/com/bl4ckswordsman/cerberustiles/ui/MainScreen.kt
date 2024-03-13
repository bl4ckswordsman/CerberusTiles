package com.bl4ckswordsman.cerberustiles.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bl4ckswordsman.cerberustiles.navbar.BottomNavBar
import com.bl4ckswordsman.cerberustiles.navbar.Screen
import com.bl4ckswordsman.cerberustiles.Constants as label

/**
 * Parameters for the main screen scaffold.
 */
data class MainScreenScaffoldParams(
    val navController: NavHostController,
    val selectedScreen: Screen,
    val canWriteState: Boolean,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean
)

/**
 * Parameters for the main screen.
 */
data class MainScreenParams(
    val canWrite: LiveData<Boolean>,
    val isAdaptive: LiveData<Boolean>,
    val toggleAdaptiveBrightness: () -> Unit,
    val isVibrationMode: LiveData<Boolean>,
    val toggleVibrationMode: () -> Boolean,
    val openPermissionSettings: () -> Unit
)

/**
 * Parameters for the main screen nav host.
 */
data class MainScreenNavHostParams(
    val navController: NavHostController,
    val innerPadding: PaddingValues,
    val canWriteState: Boolean,
    val isSwitchedOn: Boolean,
    val setSwitchedOn: (Boolean) -> Unit,
    val toggleAdaptiveBrightness: () -> Unit,
    val openPermissionSettings: () -> Unit,
    val isVibrationModeOn: Boolean,
    val setVibrationMode: (Boolean) -> Unit,
    val toggleVibrationMode: () -> Boolean
)

/**
 * The main screen scaffold that contains the top bar, bottom bar, and the main screen navigation host.
 *
 * @param params The parameters for the main screen scaffold.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(params: MainScreenScaffoldParams) {
    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(

        ), title = {
            Text(
                text = when (params.selectedScreen) {
                    is Screen.Home -> label.HOME_SCREEN
                    is Screen.Settings -> label.SETTINGS_SCREEN
                }
            )
        })
    }, bottomBar = { BottomNavBar(params.navController) }) { innerPadding ->
        MainScreenNavHost(
            MainScreenNavHostParams(
                params.navController,
                innerPadding,
                params.canWriteState,
                params.isSwitchedOn,
                params.setSwitchedOn,
                params.toggleAdaptiveBrightness,
                params.openPermissionSettings,
                params.isVibrationModeOn,
                params.setVibrationMode,
                params.toggleVibrationMode
            )
        )
    }
}

/**
 * The main screen navigation host that handles the navigation between screens.
 *
 * @param params The parameters for the main screen nav host.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreenNavHost(params: MainScreenNavHostParams) {

    val enterTrans: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        { fadeIn() }
    val exitTrans: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        { fadeOut() }

    NavHost(params.navController, startDestination = Screen.Home.route) {
        composable(
            Screen.Home.route, enterTransition = enterTrans, exitTransition = exitTrans
        ) {
            Column(
                modifier = Modifier.padding(params.innerPadding)
            ) {
                val settingsCompParams = SettingsComponentsParams(
                    canWriteState = params.canWriteState,
                    isSwitchedOn = params.isSwitchedOn,
                    setSwitchedOn = params.setSwitchedOn,
                    toggleAdaptiveBrightness = params.toggleAdaptiveBrightness,
                    openPermissionSettings = params.openPermissionSettings,
                    isVibrationModeOn = params.isVibrationModeOn,
                    setVibrationMode = params.setVibrationMode,
                    toggleVibrationMode = params.toggleVibrationMode
                )
                SettingsComponents(settingsCompParams)
            }
        }
        composable(
            Screen.Settings.route,
            enterTransition = enterTrans,
            exitTransition = exitTrans
        )
        {
            val sharedParams = createSharedParams()
            val settingsScreenParams = SettingsScreenParams(
                paddingValues = params.innerPadding,
                sharedParams = sharedParams
            )

            SettingsScreen(settingsScreenParams)
        }
    }
}

/**
 * The main screen of the app.
 *
 * @param params The parameters for the main screen.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreen(params: MainScreenParams) {
    val navController = rememberNavController()
    val canWriteState by params.canWrite.observeAsState(initial = false)
    val isAdaptiveState by params.isAdaptive.observeAsState(initial = false)
    val isVibrationModeState by params.isVibrationMode.observeAsState(initial = false)

    val (isSwitchedOn, setSwitchedOn) = remember { mutableStateOf(isAdaptiveState) }
    val (isVibrationModeOn, setVibrationMode) = remember { mutableStateOf(isVibrationModeState) }

    SideEffect {
        setSwitchedOn(isAdaptiveState)
        setVibrationMode(isVibrationModeState)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedScreen = when (navBackStackEntry?.destination?.route) {
        Screen.Home.route -> Screen.Home
        Screen.Settings.route -> Screen.Settings
        else -> Screen.Home
    }

    MainScreenScaffold(
        MainScreenScaffoldParams(
            navController,
            selectedScreen,
            canWriteState,
            isSwitchedOn,
            setSwitchedOn,
            params.toggleAdaptiveBrightness,
            params.openPermissionSettings,
            isVibrationModeOn,
            setVibrationMode,
            params.toggleVibrationMode
        )
    )
}

/**
 * A preview of the main screen.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        MainScreenParams(canWrite = MutableLiveData(true),
            isAdaptive = MutableLiveData(true),
            toggleAdaptiveBrightness = {},
            isVibrationMode = MutableLiveData(true),
            toggleVibrationMode = { true },
            openPermissionSettings = {})
    )
}
