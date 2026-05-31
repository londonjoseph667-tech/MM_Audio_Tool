package com.mm.audiotool.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mm.audiotool.data.SettingsDataStore.Companion.STORAGE_EXTERNAL
import com.mm.audiotool.data.SettingsDataStore.Companion.STORAGE_INTERNAL
import com.mm.audiotool.data.SettingsDataStore.Companion.THEME_AUTO
import com.mm.audiotool.data.SettingsDataStore.Companion.THEME_DARK
import com.mm.audiotool.data.SettingsDataStore.Companion.THEME_WHITE
import com.mm.audiotool.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel : SettingsViewModel,
    onBack            : () -> Unit
) {
    val themeMode   by settingsViewModel.themeMode.collectAsState()
    val storageType by settingsViewModel.storageType.collectAsState()

    val themeOptions   = listOf(THEME_AUTO, THEME_WHITE, THEME_DARK)
    val storageOptions = listOf(STORAGE_INTERNAL, STORAGE_EXTERNAL)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {

            // ── Theme section ─────────────────────────────────────────────────
            SectionHeader("Theme")

            Column(modifier = Modifier.selectableGroup()) {
                themeOptions.forEach { option ->
                    ThemeOptionRow(
                        label     = option,
                        selected  = themeMode == option,
                        onSelect  = { settingsViewModel.setThemeMode(option) }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))

            // ── Storage section ───────────────────────────────────────────────
            SectionHeader("Storage Location")

            Text(
                text  = "Root directory: MM_Audio-Tool",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = if (storageType == STORAGE_INTERNAL) "Internal Storage" else "External Storage",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked         = storageType == STORAGE_EXTERNAL,
                    onCheckedChange = { isExternal ->
                        settingsViewModel.setStorageType(
                            if (isExternal) STORAGE_EXTERNAL else STORAGE_INTERNAL
                        )
                    }
                )
            }

            Text(
                text  = if (storageType == STORAGE_INTERNAL)
                    "Files saved to: Internal Storage › MM_Audio-Tool"
                else
                    "Files saved to: External Storage › MM_Audio-Tool",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ThemeOptionRow(
    label    : String,
    selected : Boolean,
    onSelect : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick  = onSelect,
                role     = Role.RadioButton
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick  = null   // handled by selectable modifier
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
