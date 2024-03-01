package com.bl4ckswordsman.cerberustiles

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val toggleVibrationMode: () -> Unit
)

/**
 * Parameters for the main screen.
 */
data class MainScreenParams(
    val canWrite: LiveData<Boolean>,
    val isAdaptive: LiveData<Boolean>,
    val toggleAdaptiveBrightness: () -> Unit,
    val isVibrationMode: LiveData<Boolean>,
    val toggleVibrationMode: () -> Unit,
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
    val toggleVibrationMode: () -> Unit
)


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenScaffold(params: MainScreenScaffoldParams) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(

                ),
                title = {
                    Text(
                        text = when (params.selectedScreen) {
                            is Screen.Home -> label.HOME_SCREEN
                            is Screen.Settings -> label.SETTINGS_SCREEN
                        }
                    )
                }
            )
        },
        bottomBar = { BottomNavBar(params.navController) }
    ) { innerPadding ->
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainScreenNavHost(params: MainScreenNavHostParams) {

    val enterTrans : AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
            = { fadeIn() }
    val exitTrans : AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
            = { fadeOut() }

    NavHost(params.navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route,
            enterTransition = enterTrans,
            exitTransition = exitTrans)
        {
            Column(
                modifier = Modifier
                    .padding(params.innerPadding)
            ) {
                SwitchWithLabel(
                    isSwitchedOn = params.isSwitchedOn,
                    onCheckedChange = {
                        params.setSwitchedOn(it)
                        if (params.canWriteState) {
                            params.toggleAdaptiveBrightness()
                        } else {
                            params.openPermissionSettings()
                        }
                    },
                    label = if (params.isSwitchedOn) "Adaptive Brightness is ON" else "Adaptive Brightness is OFF"
                )
                SwitchWithLabel(
                    isSwitchedOn = params.isVibrationModeOn,
                    onCheckedChange = {
                        params.setVibrationMode(it)
                        if (params.canWriteState) {
                            params.toggleVibrationMode()
                        } else {
                            params.openPermissionSettings()
                        }
                    },
                    label = if (params.isVibrationModeOn) "Vibration Mode is ON" else "Vibration Mode is OFF"
                )
            }
        }

        composable(Screen.Settings.route,
            enterTransition = enterTrans,
            exitTransition = exitTrans)
        {
            SettingsScreen(params.innerPadding)
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
 * A switch with a label. The label is clickable and toggles the switch.
 */
@Composable
fun SwitchWithLabel(isSwitchedOn: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isSwitchedOn) }
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label)
            Switch(checked = isSwitchedOn,
                onCheckedChange = { onCheckedChange(it) },
                thumbContent = if (isSwitchedOn) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                })
        }
    }
}

/**
 * A preview of the main screen.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(MainScreenParams(
        canWrite = MutableLiveData(true),
        isAdaptive = MutableLiveData(true),
        toggleAdaptiveBrightness = {},
        isVibrationMode = MutableLiveData(true),
        toggleVibrationMode = {},
        openPermissionSettings = {}
    ))
}
