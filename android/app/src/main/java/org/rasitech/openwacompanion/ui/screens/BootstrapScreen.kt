package org.rasitech.openwacompanion.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.rasitech.openwacompanion.BuildConfig
import org.rasitech.openwacompanion.R
import org.rasitech.openwacompanion.engine.EngineStatus
import org.rasitech.openwacompanion.engine.NodeBridge

@Composable
fun BootstrapScreen() {
    val context = LocalContext.current
    var nodeVersion by remember { mutableStateOf<String?>(null) }
    var engineStatus by remember { mutableStateOf("idle") }
    var status by remember { mutableStateOf<EngineStatus?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(engineStatus) {
        while (engineStatus == "running" || engineStatus.startsWith("exited").not() && engineStatus != "idle" && engineStatus != "failed") {
            status = EngineStatus.read(NodeBridge.statusFile(context))
            delay(750)
            if (engineStatus == "idle" || engineStatus == "failed") break
            if (engineStatus.startsWith("exited")) break
        }
        // Keep polling while marked running.
        while (engineStatus == "running") {
            status = EngineStatus.read(NodeBridge.statusFile(context))
            delay(750)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.unofficial_disclaimer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Engine / Pairing POC", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("App " + BuildConfig.VERSION_NAME)
        Text("Baileys: " + BuildConfig.BAILEYS_PIN)
        Text("Node pin: " + BuildConfig.EMBEDDED_NODE_PIN)
        Text("Compile-time Node: " + (nodeVersion ?: "not loaded"))
        Text("Local engine thread: " + engineStatus)
        Text("Connection: " + (status?.connection ?: "-"))
        Text("Runtime Node: " + (status?.node ?: "-"))
        Text("Me: " + (status?.me ?: "-"))
        status?.pairingCode?.let { code -> Text("Pairing code: " + code) }
        status?.lastError?.let { err ->
            Text("Error: " + err, color = MaterialTheme.colorScheme.error)
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        val qrBmp = remember(status?.qrDataUrl) {
            status?.qrDataUrl?.substringAfter("base64,", missingDelimiterValue = "")
                ?.takeIf { it.isNotBlank() }
                ?.let { b64 ->
                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
        }
        qrBmp?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Pairing QR",
                modifier = Modifier.size(240.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            runCatching {
                nodeVersion = NodeBridge.compileTimeNodeVersion()
                error = null
            }.onFailure { error = it.message }
        }) { Text(stringResource(R.string.action_probe_node)) }

        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            engineStatus = "starting"
            error = null
            runCatching {
                NodeBridge.startEngineAsync(context) { code ->
                    engineStatus = "exited:$code"
                }
                engineStatus = "running"
            }.onFailure {
                engineStatus = "failed"
                error = it.message
            }
        }) { Text(stringResource(R.string.action_start_engine)) }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone for pairing code (optional)") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val payload =
                "{\"type\":\"request-pairing-code\",\"phone\":\"" + phone + "\"}"
            NodeBridge.writeCommand(context, payload)
        }) { Text("Request pairing code") }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            NodeBridge.writeCommand(context, """{"type":"logout"}""")
        }) { Text("Logout / unlink") }
    }
}