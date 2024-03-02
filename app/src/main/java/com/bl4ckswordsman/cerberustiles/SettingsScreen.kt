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
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

/**
 * The dialog parameters.
 */
data class DialogParams(
    val showDialog: MutableState<Boolean>,
    val titleText: String,
    val content: @Composable () -> Unit,
    val confirmButtonText: String,
    val onConfirmButtonClick: () -> Unit,
    val dismissButtonText: String? = null,
    val onDismissButtonClick: (() -> Unit)? = null
)

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
                    DownloadManager.EXTRA_DOWNLOAD_ID, -1L
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
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
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


    val versionManager = remember { VersionManager() }

    val coroutineScope = rememberCoroutineScope()



    Column(modifier = Modifier.padding(paddingValues)) {
        CreateSettingsListItem(headlineText = "App version",
            supportingText = "Click to view release notes",
            onClick = {
                coroutineScope.launch {
                    val (updateAvailable, info) = versionManager.fetchAndParseVersionInfo(
                        context, versionManager
                    )
                    isUpdateAvailable.value = updateAvailable
                    releaseInfo.value = info
                    showDialog.value = true
                }
            })
    }

    if (showDialog.value) {
        val dialogParams = DialogParams(showDialog = showDialog,
            titleText = if (isUpdateAvailable.value) "New update available" else "Release Information",
            content = {
                Column {
                    HorizontalDivider()
                    Text("Current Version: v${releaseInfo.value.currentVersion}")
                    Text("Latest Version: ${releaseInfo.value.latestVersion}")
                    HorizontalDivider()
                    Spacer(modifier = Modifier.padding(8.dp))

                    Text("Latest Release Notes:")
                    MarkdownText(releaseInfo.value.releaseNotes)
                }
            },
            confirmButtonText = "Close",
            onConfirmButtonClick = { showDialog.value = false },
            dismissButtonText = if (isUpdateAvailable.value) "Download update" else null,
            onDismissButtonClick = if (isUpdateAvailable.value) {
                {
                    coroutineScope.launch {
                        val url = versionManager.getLatestReleaseApkUrl()
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            val request = DownloadManager.Request(Uri.parse(url))
                            request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, "CerberusTiles-update.apk"
                            )
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            downloadId.longValue = downloadManager.enqueue(request)
                        } else {
                            Log.d("Download Error", "Invalid URL: $url")
                        }
                    }
                }
            } else null)
        CreateDialog(dialogParams)
    }
}

/**
 * Pads the list with the specified value to the specified size.
 */
fun List<Int>.padEnd(size: Int, value: Int = 0): List<Int> {
    return if (size > this.size) this + List(size - this.size) { value } else this
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

/**
 * Creates a settings list item.
 * @param headlineText The headline text of the item.
 * @param supportingText The supporting text of the item.
 * @param onClick The action to perform when the item is clicked.
 */
@Composable
fun CreateSettingsListItem(
    headlineText: String, supportingText: String, onClick: () -> Unit
) {
    ListItem(modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(headlineText) },
        supportingContent = { Text(supportingText) })
}

/**
 * Creates a dialog.
 * @param params The dialog parameters.
 */
@Composable
fun CreateDialog(params: DialogParams) {
    if (params.showDialog.value) {
        AlertDialog(onDismissRequest = { params.showDialog.value = false },
            title = { Text(params.titleText) },
            text = { params.content() },
            confirmButton = {
                Button(onClick = params.onConfirmButtonClick) {
                    Text(params.confirmButtonText)
                }
            },
            dismissButton = {
                if (params.dismissButtonText != null && params.onDismissButtonClick != null) {
                    Button(onClick = params.onDismissButtonClick) {
                        Text(params.dismissButtonText)
                    }
                }
            })
    }
}


/** A preview of the settings screen. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(PaddingValues(0.dp))
}

