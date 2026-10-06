package org.rasitech.openwacompanion.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.rasitech.openwacompanion.engine.NodeBridge
import org.rasitech.openwacompanion.ui.theme.WaColor
import org.rasitech.openwacompanion.ui.theme.WaTheme
import androidx.compose.ui.platform.LocalContext

@Composable
fun SplashScreen(onReady: () -> Unit) {
    val context = LocalContext.current
    val wa = WaTheme.colors
    LaunchedEffect(Unit) {
        runCatching { NodeBridge.ensureLibraryLoaded() }
        // If already linked, go straight toward home via onboarding which auto-routes on open.
        delay(900)
        onReady()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (wa.isDark) WaColor.DarkBg else WaColor.LightBg)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(WaColor.Accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("OW", color = Color.Black, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "OpenWA Companion",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Unofficial open-source companion client",
                color = wa.secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            CircularProgressIndicator(color = WaColor.Accent, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
        }
        Text(
            text = "Not affiliated with WhatsApp LLC or Meta Platforms.",
            color = wa.secondaryText,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
        )
    }
}
