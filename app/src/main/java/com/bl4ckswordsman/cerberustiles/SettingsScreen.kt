package com.bl4ckswordsman.cerberustiles

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch


/**
 * The settings screen of the app.
 * @param paddingValues The padding values of the screen.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SettingsScreen(paddingValues: PaddingValues) {


    val showDialog = remember { mutableStateOf(false) }
    val releaseInfo = remember { mutableStateOf(ReleaseInfo("", "", "")) }

    val context = LocalContext.current

    val downloadId = remember { mutableLongStateOf(-1L) }
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val isUpdateAvailable = remember { mutableStateOf(false) }


    val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, downloadIntent: Intent) {
            Log.d("Download", "Download complete")
            if (downloadId.longValue == downloadIntent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID,
                    -1L
                )
            ) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId.longValue)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (columnIndex != -1 && DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(
                            columnIndex
                        )
                    ) {
                        Log.d("Download", "Download successful")
                    }
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        context.registerReceiver(
            onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )


    }

    DisposableEffect(Unit) {
        onDispose {
            context.unregisterReceiver(onDownloadComplete)
        }
    }

    // Fetch the latest release info when the screen is composed
    LaunchedEffect(Unit) {
        releaseInfo.value = fetchLatestReleaseInfo(context)
    }

    /**
     * Parses the version string into a list of integers.
     */
    fun parseVersion(version: String): List<Int> {
        return if (version.isNotEmpty() && version.all { it.isDigit() || it == '.' || it == 'v' }) {
            val versionWithoutPrefix = if (version.startsWith("v")) version.removePrefix("v") else version
            if (versionWithoutPrefix.contains(".")) {
                versionWithoutPrefix.split(".").map { it.toInt() }
            } else {
                listOf(versionWithoutPrefix.toInt())
            }
        } else {
            Log.d("Parse Error", "Invalid version: $version")
            listOf(0)
        }
    }

    val versionManager = remember { VersionManager() }

    val coroutineScope = rememberCoroutineScope()



    Column(modifier = Modifier.padding(paddingValues)) {
        ListItem(modifier = Modifier.clickable {
            coroutineScope.launch {
                // Fetch the latest release info when the ListItem is clicked
                releaseInfo.value = fetchLatestReleaseInfo(context)

                // Parse the version numbers
                val currentVersionNumbers = parseVersion(releaseInfo.value.currentVersion)
                val latestVersionNumbers = parseVersion(releaseInfo.value.latestVersion)

                /**
                 * Pads the list with the specified value to the specified size.
                 */
                fun List<Int>.padEnd(size: Int, value: Int = 0): List<Int> {
                    return if (size > this.size) this + List(size - this.size) { value } else this
                }

                val maxLength = maxOf(currentVersionNumbers.size, latestVersionNumbers.size)
                val paddedCurrentVersionNumbers = currentVersionNumbers.padEnd(maxLength, 0)
                val paddedLatestVersionNumbers = latestVersionNumbers.padEnd(maxLength, 0)
                isUpdateAvailable.value = paddedCurrentVersionNumbers.zip(paddedLatestVersionNumbers).any { (current: Int, latest: Int) -> current < latest }

                /*Log.d("Update", "Update available: ${isUpdateAvailable.value}")
                Log.d("Current Version", "Current version: $currentVersionNumbers")
                Log.d("Latest Version", "Latest version: $latestVersionNumbers")*/ //TODO: Remove logs

                // Show the dialog
                showDialog.value = true
            }
        },
            headlineContent = { Text("App Version") },
            supportingContent = { Text("Click to view release notes") })
    }

    if (showDialog.value) {
        AlertDialog(onDismissRequest = { showDialog.value = false }, icon = {
            if (isUpdateAvailable.value) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_new_releases_24),
                    contentDescription = "Update available"
                )
            }
        }, title = {
            if (isUpdateAvailable.value) {
                Text("New update available")
            } else {
                Text("Release Information")
            }
        }, text = {
            Column {
                HorizontalDivider()
                Text("Current Version: v${releaseInfo.value.currentVersion}")
                Text("Latest Version: ${releaseInfo.value.latestVersion}")
                HorizontalDivider()
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
                if (isUpdateAvailable.value) {
                    Button(onClick = {
                        coroutineScope.launch {
                            val url = versionManager.getLatestReleaseApkUrl()
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                val request = DownloadManager.Request(Uri.parse(url))
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    "CerberusTiles-update.apk"
                                )
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                downloadId.longValue = downloadManager.enqueue(request)
                            } else {
                                Log.d("Download Error", "Invalid URL: $url")
                            }
                        }
                    }) {
                        Text("Download update")
                    }
                }
            })
    }
}

/**
 * A composable that displays markdown text using Markwon.
 * @param markdown The markdown text to display.
 */
@Composable
fun MarkdownText(markdown: String) {
    val markdownContext = LocalContext.current
    val markwon = remember { Markwon.create(markdownContext) }

    AndroidView(factory = { context ->
        TextView(context).apply {
            movementMethod = LinkMovementMethod.getInstance()
        }
    }, update = { view ->
        markwon.setMarkdown(view, markdown)
    })
}

/**
 * The release info of the app.
 * @param currentVersion The current version of the app.
 * @param latestVersion The latest version of the app.
 * @param releaseNotes The release notes of the latest version.
 */
data class ReleaseInfo(
    val currentVersion: String, val latestVersion: String, val releaseNotes: String
)

/**
 * Fetches the latest release info from the server.
 * @param context The context of the app.
 * @return The latest release info.
 */
suspend fun fetchLatestReleaseInfo(context: Context): ReleaseInfo {
    val versionManager = VersionManager()
    val appVersion = versionManager.getCurrentAppVersion(context)
    val latestReleaseVersion = versionManager.getLatestReleaseVersion()
    val releaseNotes = versionManager.getLatestReleaseNotes()

    return ReleaseInfo(appVersion, latestReleaseVersion, releaseNotes)
}


/** A preview of the settings screen. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(PaddingValues(0.dp))
}

