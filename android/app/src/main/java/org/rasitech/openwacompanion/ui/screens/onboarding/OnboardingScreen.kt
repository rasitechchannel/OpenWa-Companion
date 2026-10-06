package org.rasitech.openwacompanion.ui.screens.onboarding

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import org.rasitech.openwacompanion.BuildConfig
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.engine.EngineForegroundService
import org.rasitech.openwacompanion.engine.EngineStatus
import org.rasitech.openwacompanion.engine.NodeBridge
import org.rasitech.openwacompanion.ui.theme.WaColor
import org.rasitech.openwacompanion.ui.theme.WaTheme

/**
 * WhatsApp-like "agree and continue / link device" flow.
 * No fake WhatsApp registration — actions only pair a companion session.
 */
@Composable
fun OnboardingScreen(onConnected: () -> Unit, onSkipToHome: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { OpenWaRepository(context) }
    var status by remember { mutableStateOf<EngineStatus?>(null) }
    var phone by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) } // 0 welcome, 1 link
    var started by remember { mutableStateOf(false) }
    val wa = WaTheme.colors

    LaunchedEffect(Unit) {
        repo.ensureAccount("default")
        while (true) {
            status = EngineStatus.read(NodeBridge.statusFile(context))
            if (status?.connection == "open") onConnected()
            delay(750)
        }
    }

    LaunchedEffect(step) {
        if (step == 1 && !started) {
            started = true
            ContextCompat.startForegroundService(
                context,
                Intent(context, EngineForegroundService::class.java),
            )
            NodeBridge.startEngineAsync(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (step == 0) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "Welcome to OpenWA Companion",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "An unofficial open-source companion that links to your WhatsApp account locally. " +
                    "Your messages stay on this device — OpenWA does not operate a message server.",
                color = wa.secondaryText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Not affiliated with WhatsApp LLC or Meta Platforms.",
                color = wa.secondaryText,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = { step = 1 },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaColor.Accent, contentColor = Color.Black),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text("Agree and continue", fontWeight = FontWeight.SemiBold)
            }
            if (BuildConfig.DEBUG) {
                TextButton(onClick = onSkipToHome) { Text("Debug: continue without linking") }
            }
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Text(
                "Link with QR code",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "1. Open WhatsApp on your phone\n2. Tap Menu or Settings and select Linked devices\n3. Tap Link a device\n4. Point your phone at this screen to scan the code",
                color = wa.secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(24.dp))

            val qrBmp = remember(status?.qrDataUrl) {
                status?.qrDataUrl?.substringAfter("base64,")?.let { b64 ->
                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (qrBmp != null) {
                    Image(
                        bitmap = qrBmp.asImageBitmap(),
                        contentDescription = "QR code",
                        modifier = Modifier.size(260.dp),
                    )
                } else {
                    CircularProgressIndicator(color = WaColor.Accent)
                }
            }

            status?.pairingCode?.let { code ->
                Spacer(modifier = Modifier.height(20.dp))
                Text("Link with phone number instead", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    code.chunked(4).joinToString("  "),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaColor.Accent,
                    letterSpacing = 2.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number with country code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WaColor.Accent,
                    cursorColor = WaColor.Accent,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { repo.requestPairingCode(phone) },
                enabled = phone.filter { it.isDigit() }.length >= 8,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WaColor.Accent, contentColor = Color.Black),
            ) {
                Text("Get linking code")
            }

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "dbg " + (status?.connection ?: "idle"),
                    color = wa.secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
