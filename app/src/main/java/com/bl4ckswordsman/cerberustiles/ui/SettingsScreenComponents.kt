package com.bl4ckswordsman.cerberustiles.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bl4ckswordsman.cerberustiles.VersionManager
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch

/**
 * The settings list item parameters.
 */
data class SettingsListItemParams(
    val sharedParams: SharedParams
)

/**
 * The dialog creation parameters.
 */
data class DialogCreationParams(
    val sharedParams: SharedParams
)

/**
 * The shared parameters between the settings screen components.
 */
@Composable
fun createSharedParams(): SharedParams {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val releaseInfo = remember { mutableStateOf(ReleaseInfo("", "", "")) }
    val downloadId = remember { mutableLongStateOf(-1L) }
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val isUpdateAvailable = remember { mutableStateOf(false) }
    val versionManager = remember { VersionManager() }
    val coroutineScope = rememberCoroutineScope()

    return SharedParams(
        context = context,
        coroutineScope = coroutineScope,
        showDialog = showDialog,
        isUpdateAvailable = isUpdateAvailable,
        releaseInfo = releaseInfo,
        downloadId = downloadId,
        downloadManager = downloadManager,
        versionManager = versionManager
    )
}

/**
 * Creates the list items of the settings screen.
 * @param params The parameters of the list items.
 */
@Composable
fun CreateSettingsListItem(params: SettingsListItemParams) {
    CreateSettingsListItem(headlineText = "App version",
        supportingText = "Click to view release notes",
        onClick = {
            params.sharedParams.coroutineScope.launch {
                val (updateAvailable, info) = params.sharedParams.versionManager.fetchAndParseVersionInfo(
                    params.sharedParams.context, params.sharedParams.versionManager
                )
                params.sharedParams.isUpdateAvailable.value = updateAvailable
                params.sharedParams.releaseInfo.value = info
                params.sharedParams.showDialog.value = true
            }
        })
}

/**
 * Creates the dialog for the settings screen.
 * @param params The parameters of the dialog.
 */
@Composable
fun CreateDialog(params: DialogCreationParams) {
    val dialogParams = DialogParams(showDialog = params.sharedParams.showDialog,
        titleText = if (params.sharedParams.isUpdateAvailable.value) "New update available" else "Release Information",
        content = {
            Column {
                HorizontalDivider()
                Text("Current Version: v${params.sharedParams.releaseInfo.value.currentVersion}")
                Text("Latest Version: ${params.sharedParams.releaseInfo.value.latestVersion}")
                HorizontalDivider()
                Spacer(modifier = Modifier.padding(8.dp))
                Text("Latest Release Notes:")
                MarkdownText(params.sharedParams.releaseInfo.value.releaseNotes)
            }
        },
        confirmButtonText = "Close",
        onConfirmButtonClick = { params.sharedParams.showDialog.value = false },
        dismissButtonText = if (params.sharedParams.isUpdateAvailable.value) "Download update" else null,
        onDismissButtonClick = if (params.sharedParams.isUpdateAvailable.value) {
            {
                params.sharedParams.coroutineScope.launch {
                    val url = params.sharedParams.versionManager.getLatestReleaseApkUrl()
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        val request = DownloadManager.Request(Uri.parse(url))
                        request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS, "CerberusTiles-update.apk"
                        )
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        params.sharedParams.downloadId.value =
                            params.sharedParams.downloadManager.enqueue(request)
                    } else {
                        Log.d("Download Error", "Invalid URL: $url")
                    }
                }
            }
        } else null)
    CreateDialog(dialogParams)
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