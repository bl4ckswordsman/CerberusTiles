package com.bl4ckswordsman.customtiles

import MainScreen
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bl4ckswordsman.customtiles.ui.theme.CustomTilesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomTilesTheme {
                val isAdaptive = Settings.System.getInt(
                    contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE
                ) == 1
                MainScreen(
                    canWrite = Settings.System.canWrite(this@MainActivity),
                    isAdaptive = isAdaptive,
                    toggleAdaptiveBrightness = ::toggleAdaptiveBrightness
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
    }
}