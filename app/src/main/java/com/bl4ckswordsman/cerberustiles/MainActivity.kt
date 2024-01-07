package com.bl4ckswordsman.cerberustiles

import android.content.Intent
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bl4ckswordsman.cerberustiles.ui.theme.CustomTilesTheme
import kotlinx.coroutines.launch

/** Main activity of the app. */
@RequiresApi(Build.VERSION_CODES.N_MR1)
class MainActivity : ComponentActivity(), LifecycleObserver {
    private val shortcutManager by lazy { getSystemService(ShortcutManager::class.java) }
    private val shortcutHelper by lazy { ShortcutHelper(this, shortcutManager) }
    companion object {
        const val HOME_SCREEN = "Home"
        const val SETTINGS_SCREEN = "Settings"
    }

    private val _canWrite = MutableLiveData<Boolean>()
    private val canWrite: LiveData<Boolean> get() = _canWrite

    private val _isAdaptive = MutableLiveData<Boolean>()
    private val isAdaptive: LiveData<Boolean> get() = _isAdaptive

    override fun onResume() {
        super.onResume()
        _canWrite.value = Settings.System.canWrite(this)
        _isAdaptive.value = SettingsUtils.Brightness.isAdaptiveBrightnessEnabled(this)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
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

        lifecycleScope.launch {
            shortcutHelper.createAdaptiveBrightnessShortcut()
        }

        // Handle the intent action from the shortcut
        if (intent?.action == "com.bl4ckswordsman.cerberustiles.TOGGLE_ADAPTIVE_BRIGHTNESS") {
            toggleAdaptiveBrightness()
        }
    }


    private fun toggleAdaptiveBrightness() {
        SettingsUtils.Brightness.toggleAdaptiveBrightness(this)
        // Update _isAdaptive after changing the setting
        _isAdaptive.value = !(_isAdaptive.value ?: false)
    }


    private fun openSettingsScreen() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${this@MainActivity.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}