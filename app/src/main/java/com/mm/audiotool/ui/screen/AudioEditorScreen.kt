package com.mm.audiotool.ui.screen

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mm.audiotool.model.AudioItem
import com.mm.audiotool.viewmodel.AudioEditorViewModel
import com.mm.audiotool.viewmodel.AudioEditorViewModelFactory
import com.mm.audiotool.viewmodel.EditorUiState
import com.mm.audiotool.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEditorScreen(
    ckbUri            : String,
    settingsViewModel : SettingsViewModel,
    onNavigateToProcessing : () -> Unit,
    onBack            : () -> Unit
) {
    val context     = LocalContext.current
    val storageType by settingsViewModel.storageType.collectAsState()

    val editorViewModel: AudioEditorViewModel = viewModel(
        factory = AudioEditorViewModelFactory(
            application = context.applicationContext as Application,
            ckbUri      = ckbUri,
            storageType = storageType
        )
    )

    val uiState     by editorViewModel.uiState.collectAsState()
    val currentPage by editorViewModel.currentPage.collectAsState()

    // Track which item is waiting for a replacement file pick
    var pendingReplaceItem by remember { mutableStateOf<AudioItem?>(null) }

    // File picker for replacement audio
    val replacePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && pendingReplaceItem != null) {
            editorViewModel.replaceAudio(pendingReplaceItem!!.id, uri)
            pendingReplaceItem = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // NEXT button — triggers repacking
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick  = onNavigateToProcessing,
                    enabled  = uiState is EditorUiState.Ready,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) {
                    Text("NEXT  →  Repack", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is EditorUiState.Idle, EditorUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is EditorUiState.Error -> {
                    Text(
                        text      = "Error: ${state.message}",
                        color     = MaterialTheme.colorScheme.error,
                        modifier  = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }

                is EditorUiState.Ready -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Page indicator
                        Text(
                            text     = "Page ${currentPage + 1} / ${editorViewModel.totalPages}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            textAlign = TextAlign.End,
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        // Audio item list (6 per page)
                        LazyColumn(
                            modifier       = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                AudioItemRow(
                                    item        = item,
                                    onPlayPause = { editorViewModel.togglePlayback(item) },
                                    onReplace   = {
                                        pendingReplaceItem = item
                                        replacePicker.launch(arrayOf("audio/*"))
                                    },
                                    onSave      = { editorViewModel.saveItem(item.id) }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                            }
                        }

                        // Pagination controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick  = { editorViewModel.prevPage() },
                                enabled  = currentPage > 0
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous page")
                            }
                            IconButton(
                                onClick  = { editorViewModel.nextPage() },
                                enabled  = currentPage < editorViewModel.totalPages - 1
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next page")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Single audio row (Compose equivalent of the RecyclerView row) ─────────────

@Composable
private fun AudioItemRow(
    item        : AudioItem,
    onPlayPause : () -> Unit,
    onReplace   : () -> Unit,
    onSave      : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play / Pause circle button
        FilledIconButton(
            onClick  = onPlayPause,
            modifier = Modifier.size(44.dp),
            colors   = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (item.isPlaying)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector        = if (item.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (item.isPlaying) "Pause" else "Play",
                modifier           = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        // File name + replacement label
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = item.originalName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style    = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (item.replacedFile != null) {
                Text(
                    text  = "↳ ${item.replacedFile!!.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(6.dp))

        // Replace button
        OutlinedButton(
            onClick      = onReplace,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("Replace", fontSize = 12.sp)
        }

        Spacer(Modifier.width(6.dp))

        // Save button
        Button(
            onClick  = onSave,
            enabled  = item.replacedFile != null,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors   = if (item.isSaved)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            else
                ButtonDefaults.buttonColors()
        ) {
            Text(if (item.isSaved) "Saved" else "Save", fontSize = 12.sp)
        }
    }
}
