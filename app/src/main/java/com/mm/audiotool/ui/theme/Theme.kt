package com.mm.audiotool.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Colour tokens ─────────────────────────────────────────────────────────────

private val PrimaryBlue   = Color(0xFF1565C0)
private val SecondaryTeal = Color(0xFF00897B)
private val TertiaryGreen = Color(0xFF2E7D32)

private val LightColors = lightColorScheme(
    primary          = PrimaryBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    secondary        = SecondaryTeal,
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    tertiary         = TertiaryGreen,
    onTertiary       = Color.White,
    background       = Color(0xFFF8F9FA),
    surface          = Color.White,
    onBackground     = Color(0xFF1A1C1E),
    onSurface        = Color(0xFF1A1C1E)
)

private val DarkColors = darkColorScheme(
    primary          = Color(0xFF90CAF9),
    onPrimary        = Color(0xFF003258),
    primaryContainer = Color(0xFF004880),
    secondary        = Color(0xFF80CBC4),
    onSecondary      = Color(0xFF003731),
    secondaryContainer = Color(0xFF004F47),
    tertiary         = Color(0xFFA5D6A7),
    onTertiary       = Color(0xFF003909),
    background       = Color(0xFF1A1C1E),
    surface          = Color(0xFF1A1C1E),
    onBackground     = Color(0xFFE2E2E6),
    onSurface        = Color(0xFFE2E2E6)
)

// ── Theme composable ──────────────────────────────────────────────────────────

@Composable
fun MM_Audio_ToolTheme(
    darkTheme : Boolean = false,
    content   : @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
