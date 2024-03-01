package com.bl4ckswordsman.cerberustiles

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bl4ckswordsman.cerberustiles.Constants.TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION
import com.bl4ckswordsman.cerberustiles.Constants.TOGGLE_VIBRATION_MODE_ACTION
import com.bl4ckswordsman.cerberustiles.ui.theme.CustomTilesTheme
import kotlinx.coroutines.launch

/** Main activity of the app. */
@RequiresApi(Build.VERSION_CODES.N_MR1)
class MainActivity : ComponentActivity(), LifecycleObserver {
    /** TODO: Use viewModelScope for LiveData instead of using MutableLiveData, this ensures that
     * the LiveData is cleared when the ViewModel is cleared.**/

    
    private val shortcutHelper by lazy { ShortcutHelper(this) }

    private val _canWrite = MutableLiveData<Boolean>()
    private val canWrite: LiveData<Boolean> get() = _canWrite

    private val _isAdaptive = MutableLiveData<Boolean>()
    private val isAdaptive: LiveData<Boolean> get() = _isAdaptive
    private val _isVibrationMode = MutableLiveData<Boolean>()
    private val isVibrationMode: LiveData<Boolean> get() = _isVibrationMode


    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            shortcutHelper.createAllShortcuts()
        }
    }

    override fun onResume() {
        super.onResume()
        _canWrite.value = Settings.System.canWrite(this)
        _isAdaptive.value = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(this)
        _isVibrationMode.value = SettingsUtils.Vibration.isVibrationModeEnabled(this)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        setContent {
            CustomTilesTheme {
                MainScreen(
                    MainScreenParams(
                        canWrite = canWrite,
                        isAdaptive = isAdaptive,
                        toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                        isVibrationMode = isVibrationMode,
                        toggleVibrationMode = ::toggleVibrationMode,
                        openPermissionSettings = ::openPermissionSettings
                    )
                )
            }
        }

        // Handle the intent action from the shortcut
        if (intent?.action == TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION) {
            toggleAdaptiveBrightness()
        }
        if (intent?.action == TOGGLE_VIBRATION_MODE_ACTION) {
            toggleVibrationMode()
        }
    }


    private fun toggleAdaptiveBrightness() {
        SettingsUtils.Brightness.toggleAdaptiveBrightness(this)
        // Update _isAdaptive after changing the setting
        _isAdaptive.value = !(_isAdaptive.value ?: false)
    }

    private fun toggleVibrationMode(): Boolean {
        val isToggled = SettingsUtils.Vibration.toggleVibrationMode(this) { newValue ->
            _isVibrationMode.value = newValue
        }
        return isToggled
    }


    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${this@MainActivity.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}