package com.bl4ckswordsman.customtiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bl4ckswordsman.customtiles.ui.theme.CustomTilesTheme

/** Main activity of the app. */
class MainActivity : ComponentActivity(), LifecycleObserver {
    companion object {
        const val HOME_SCREEN = "Home"
        const val SETTINGS_SCREEN = "Settings"
    }
    private val _canWrite = MutableLiveData<Boolean>()
    private val canWrite: LiveData<Boolean> get() = _canWrite

    private val _isAdaptive = MutableLiveData<Boolean>()
    val isAdaptive: LiveData<Boolean> get() = _isAdaptive

    override fun onResume() {
        super.onResume()
        _canWrite.value = Settings.System.canWrite(this)
        _isAdaptive.value = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        setContent {
            CustomTilesTheme {
                MainScreen(
                    canWrite = canWrite,
                    isAdaptive = isAdaptive,
                    toggleAdaptiveBrightness = ::toggleAdaptiveBrightness,
                    openSettingsScreen = ::openSettingsScreen
                )
            }
        }
    }

    /**
     * Toggles adaptive brightness. It needs the WRITE_SETTINGS permission.
     */
    private fun toggleAdaptiveBrightness() {
        val isAdaptive =
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == 1
        Settings.System.putInt(
            contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, if (isAdaptive) 0 else 1
        )
        // Update _isAdaptive after changing the setting
        _isAdaptive.value = !isAdaptive
    }

    /**
     * Opens the settings screen where the user can grant the WRITE_SETTINGS permission.
     */
    private fun openSettingsScreen() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${this@MainActivity.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}