package com.mm.audiotool.ui.screen

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.mm.audiotool.viewmodel.AudioEditorViewModel
import com.mm.audiotool.viewmodel.AudioEditorViewModelFactory
import com.mm.audiotool.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun ProcessingScreen(
    settingsViewModel    : SettingsViewModel,
    editorBackStackEntry : NavBackStackEntry,
    onComplete           : (String) -> Unit
) {
    val context     = LocalContext.current
    val storageType by settingsViewModel.storageType.collectAsState()

    // Reuse the same ViewModel instance that was created on the editor screen
    val editorViewModel: AudioEditorViewModel = viewModel(
        viewModelStoreOwner = editorBackStackEntry,
        factory = AudioEditorViewModelFactory(
            application = context.applicationContext as Application,
            ckbUri      = "",          // already initialised; factory won't re-create
            storageType = storageType
        )
    )

    var progress  by remember { mutableFloatStateOf(0f) }
    var statusMsg by remember { mutableStateOf("Preparing…") }
    val scope     = rememberCoroutineScope()

    // Kick off repacking once
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                statusMsg = "Repacking audio files…"
                val savedPath = editorViewModel.repack(context) { p ->
                    progress  = p
                    statusMsg = "Repacking… ${(p * 100).toInt()}%"
                }
                statusMsg = "Done!"
                onComplete(savedPath)
            } catch (e: Exception) {
                statusMsg = "Error: ${e.message}"
            }
        }
    }

    Box(
        modifier          = Modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Text(
                text       = "Repacking…",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(32.dp))

            // Horizontal progress bar
            LinearProgressIndicator(
                progress       = { progress },
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                trackColor     = MaterialTheme.colorScheme.surfaceVariant,
                color          = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text      = statusMsg,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Spinner while in progress
            if (progress < 1f) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            }
        }
    }
}
