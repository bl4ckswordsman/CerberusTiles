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
import com.bl4ckswordsman.cerberustiles.Constants.TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION
import com.bl4ckswordsman.cerberustiles.ui.theme.CustomTilesTheme
import kotlinx.coroutines.launch

/** Main activity of the app. */
@RequiresApi(Build.VERSION_CODES.N_MR1)
class MainActivity : ComponentActivity(), LifecycleObserver {
    /** TODO: Use viewModelScope for LiveData instead of using MutableLiveData, this ensures that
     * the LiveData is cleared when the ViewModel is cleared.**/
    private val shortcutManager by lazy { getSystemService(ShortcutManager::class.java) }
    private val shortcutHelper by lazy { ShortcutHelper(this, shortcutManager) }

    private val _canWrite = MutableLiveData<Boolean>()
    private val canWrite: LiveData<Boolean> get() = _canWrite

    private val _isAdaptive = MutableLiveData<Boolean>()
    private val isAdaptive: LiveData<Boolean> get() = _isAdaptive

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            shortcutHelper.createAdaptiveBrightnessShortcut()
        }
    }

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
                    openPermissionSettings = ::openPermissionSettings
                )
            }
        }

        // Handle the intent action from the shortcut
        if (intent?.action == TOGGLE_ADAPTIVE_BRIGHTNESS_ACTION) {
            toggleAdaptiveBrightness()
        }
    }


    private fun toggleAdaptiveBrightness() {
        SettingsUtils.Brightness.toggleAdaptiveBrightness(this)
        // Update _isAdaptive after changing the setting
        _isAdaptive.value = !(_isAdaptive.value ?: false)
    }


    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${this@MainActivity.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}