package com.example.crattendance.ui.screens

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val KEEP_PACKAGE = "com.google.android.keep"

/**
 * Quick note composer. RollDesk does not store notes; the text is handed to
 * Google Keep through a share intent so the user can save it there.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesComposerSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(350)
        runCatching { focusRequester.requestFocus() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                "Quick note",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Saved through Google Keep",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Note") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { openKeep(context) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Open Keep", maxLines = 1)
                }
                Button(
                    onClick = {
                        sendToKeep(context, title, body)
                        onDismiss()
                    },
                    enabled = title.isNotBlank() || body.isNotBlank(),
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save to Keep", maxLines = 1)
                }
            }
        }
    }
}

private fun sendToKeep(context: Context, title: String, body: String) {
    val cleanTitle = title.trim()
    val cleanBody = body.trim()
    val text = buildString {
        if (cleanTitle.isNotEmpty()) {
            append(cleanTitle)
            if (cleanBody.isNotEmpty()) append("\n\n")
        }
        if (cleanBody.isNotEmpty()) append(cleanBody)
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, cleanTitle)
        putExtra(Intent.EXTRA_TEXT, text)
    }

    val keepIntent = Intent(shareIntent).apply { setPackage(KEEP_PACKAGE) }
    if (keepIntent.resolveActivity(context.packageManager) != null) {
        try {
            context.startActivity(keepIntent)
            Toast.makeText(context, "Note handed to Keep", Toast.LENGTH_SHORT).show()
            return
        } catch (_: ActivityNotFoundException) {
            // Fall through to the chooser below.
        }
    }

    val handlers = context.packageManager.queryIntentActivities(shareIntent, 0)
    if (handlers.isNotEmpty()) {
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Save note"))
            return
        } catch (_: ActivityNotFoundException) {
            // Fall through to the clipboard below.
        }
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Note", text))
    Toast.makeText(context, "Google Keep not found. Note copied to clipboard.", Toast.LENGTH_LONG).show()
}

private fun openKeep(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(KEEP_PACKAGE)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
        return
    }

    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$KEEP_PACKAGE"))
        )
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$KEEP_PACKAGE")
                )
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Google Keep is not installed", Toast.LENGTH_SHORT).show()
        }
    }
}
