package com.mm.audiotool.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuccessScreen(
    savedPath : String,
    onDone    : () -> Unit
) {
    // Bounce-in animation for the check-mark
    val scale by animateFloatAsState(
        targetValue  = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    // Trigger animation on first composition
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { animTarget = 1f }

    val animatedScale by animateFloatAsState(
        targetValue   = animTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "bounceIn"
    )

    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {

            // Large animated check-mark
            Icon(
                imageVector        = Icons.Default.CheckCircle,
                contentDescription = "Success",
                modifier           = Modifier
                    .size(120.dp)
                    .scale(animatedScale),
                tint               = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text       = "Repack Complete!",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text      = "File saved to:",
                fontSize  = 14.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Saved path card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text      = savedPath,
                    modifier  = Modifier.padding(16.dp),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Internal Storage › MM_Audio-Tool",
                fontSize  = 12.sp,
                color     = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            Button(
                onClick  = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Back to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
