package com.bl4ckswordsman.cerberustiles.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.SettingsUtils
import kotlin.math.ln

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
 * A slider for brightness settings.
 */
@Composable
fun BrightnessSlider(context: Context) {
    var sliderPosition by remember { mutableFloatStateOf((ln(
        SettingsUtils.Brightness.getScreenBrightness(context).toDouble()) / ln(255.0)).toFloat()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = {
            sliderPosition = 0f
            SettingsUtils.Brightness.setScreenBrightness(context, sliderPosition)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_brightness_empty_24),
                contentDescription = "Minimize brightness"
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    SettingsUtils.Brightness.setScreenBrightness(context, it)
                },
                valueRange = 0f..1f
            )
        }

        IconButton(onClick = {
            sliderPosition = 1f
            SettingsUtils.Brightness.setScreenBrightness(context, sliderPosition)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.rounded_brightness_high_24),
                contentDescription = "Maximize brightness"
            )
        }
    }
}