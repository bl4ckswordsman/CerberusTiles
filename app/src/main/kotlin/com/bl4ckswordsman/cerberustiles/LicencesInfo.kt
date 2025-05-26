package com.bl4ckswordsman.cerberustiles

import android.webkit.WebView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * The open source licenses dialog.
 *
 * @param showDialog The state of the dialog.
 */
@Composable
fun OpenSourceLicensesDialog(showDialog: MutableState<Boolean>) {
    if (showDialog.value) {
        LocalContext.current
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Open Source Licenses") },
            text = {
                AndroidView(factory = { ctx ->
                    WebView(ctx).apply {
                        loadUrl("file:///android_asset/open_source_licenses.html")
                    }
                })
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Close")
                }
            }
        )
    }
}
