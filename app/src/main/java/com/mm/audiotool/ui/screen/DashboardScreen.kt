package com.mm.audiotool.ui.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mm.audiotool.util.PermissionHelper
import com.mm.audiotool.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToEditor   : (String) -> Unit,
    onNavigateToSettings : () -> Unit,
    settingsViewModel    : SettingsViewModel
) {
    val context = LocalContext.current

    var selectedUri   by remember { mutableStateOf<Uri?>(null) }
    var selectedName  by remember { mutableStateOf("") }
    var showPermDialog by remember { mutableStateOf(false) }

    // ── Permission launcher ───────────────────────────────────────────────────
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* result handled via hasStoragePermissions check */ }

    // ── File picker launcher ──────────────────────────────────────────────────
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist read permission across restarts
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedUri  = uri
            selectedName = uri.lastPathSegment?.substringAfterLast('/') ?: uri.toString()
        }
    }

    // ── Permission dialog ─────────────────────────────────────────────────────
    if (showPermDialog) {
        AlertDialog(
            onDismissRequest = { showPermDialog = false },
            title   = { Text("Storage Permission Required") },
            text    = { Text("MM Audio Tool needs storage access to read and write .ckb files. Please grant the permission.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermDialog = false
                    permLauncher.launch(PermissionHelper.requiredPermissions())
                }) { Text("Grant") }
            },
            dismissButton = {
                TextButton(onClick = { showPermDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "MM Audio Tool",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector        = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector        = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier           = Modifier.size(80.dp),
                tint               = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text      = "Select a .ckb file to begin",
                fontSize  = 18.sp,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // ── Pick File button ──────────────────────────────────────────────
            Button(
                onClick = {
                    if (!PermissionHelper.hasStoragePermissions(context)) {
                        showPermDialog = true
                    } else {
                        filePicker.launch(arrayOf("*/*"))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Pick .ckb File", fontSize = 16.sp)
            }

            // ── Selected file chip ────────────────────────────────────────────
            if (selectedName.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = "Selected: $selectedName",
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            color    = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Extract button (only visible after file selected) ─────────
                Button(
                    onClick = { onNavigateToEditor(selectedUri.toString()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Extract", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
