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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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

    val downloadId = remember { mutableStateOf(-1L) }
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, downloadIntent: Intent) {
            Log.d("Download", "Download complete")
            if (downloadId.value == downloadIntent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID,
                    -1L
                )
            ) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId.value)
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


    fun parseVersion(version: String): List<Int> {
        return if (version.isNotEmpty() && version.all { it.isDigit() || it == '.' }) {
            version.removePrefix("v").split(".").map { it.toInt() }
        } else {
            listOf(0)
        }
    }

    val currentVersionNumbers = parseVersion(releaseInfo.value.currentVersion)
    val latestVersionNumbers = parseVersion(releaseInfo.value.latestVersion)
    val isUpdateAvailable = currentVersionNumbers.zip(latestVersionNumbers).any { (current, latest) -> current < latest }

    val versionManager = remember { VersionManager() }

    val coroutineScope = rememberCoroutineScope()



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
                                downloadId.value = downloadManager.enqueue(request)
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


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(PaddingValues(0.dp))
}

