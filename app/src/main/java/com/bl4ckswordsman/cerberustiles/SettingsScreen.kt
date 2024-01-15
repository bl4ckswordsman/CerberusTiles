package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon


/**
 * The settings screen of the app.
 */
@Composable
fun SettingsScreen(paddingValues: PaddingValues) {


    val showDialog = remember { mutableStateOf(false) }
    val releaseInfo = remember { mutableStateOf(ReleaseInfo("", "", "")) }

    val context = LocalContext.current

    // Fetch the latest release info when the screen is composed
    LaunchedEffect(Unit) {
        releaseInfo.value = fetchLatestReleaseInfo(context)
    }


    val isUpdateAvailable =
        releaseInfo.value.currentVersion < releaseInfo.value.latestVersion.removePrefix("v")

    Column(modifier = Modifier.padding(paddingValues)) {
        ListItem(modifier = Modifier.clickable { showDialog.value = true },
            headlineContent = { Text("App Version") },
            supportingContent = { Text("Click to view release notes") })
    }

    if (showDialog.value) {
        AlertDialog(onDismissRequest = { showDialog.value = false }, icon = {
            if (isUpdateAvailable) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_new_releases_24),
                    contentDescription = "Update available"
                )
            }
        }, title = {
            if (isUpdateAvailable) {
                Text("New update available")
            } else {
                Text("Release Information")
            }
        }, text = {
            Column {
                Divider()
                Text("Current Version: v${releaseInfo.value.currentVersion}")
                Text("Latest Version: ${releaseInfo.value.latestVersion}")
                Divider()
                Spacer(modifier = Modifier.padding(8.dp))

                Text("Latest Release Notes:")
                MarkdownText(releaseInfo.value.releaseNotes)
            }
        }, confirmButton = {
            Button(onClick = { showDialog.value = false }) {
                Text("Close")
            }
        },
            // Add an update button if an update is available
            dismissButton = {
                if (isUpdateAvailable) {
                    Button(onClick = { /* Handle update */ }) {
                        Text("Update")
                    }
                }
            })
    }
}

@Composable
fun MarkdownText(markdown: String) {
    val context = LocalContext.current
    val markwon = remember { Markwon.create(context) }

    AndroidView(factory = { context ->
        TextView(context).apply {
            movementMethod = LinkMovementMethod.getInstance()
        }
    }, update = { view ->
        markwon.setMarkdown(view, markdown)
    })
}

data class ReleaseInfo(
    val currentVersion: String, val latestVersion: String, val releaseNotes: String
)

suspend fun fetchLatestReleaseInfo(context: Context): ReleaseInfo {
    val versionManager = VersionManager()
    val appVersion = versionManager.getCurrentAppVersion(context)
    val latestReleaseVersion = versionManager.getLatestReleaseVersion()
    val releaseNotes = versionManager.getLatestReleaseNotes()

    return ReleaseInfo(appVersion, latestReleaseVersion, releaseNotes)
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(PaddingValues(0.dp))
}

