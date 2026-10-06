package org.rasitech.openwacompanion

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.rasitech.openwacompanion.ui.navigation.OpenWaNavHost
import org.rasitech.openwacompanion.ui.theme.OpenWaTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as OpenWaApp
        val lockEnabled = app.vault.isAppLockEnabled() && !app.vault.isUnlockedSession()
        setContent {
            var unlocked by remember { mutableStateOf(!lockEnabled) }
            OpenWaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) {
                    if (unlocked) {
                        OpenWaNavHost()
                    } else {
                        androidx.compose.material3.Text("Unlocking…")
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            promptUnlock { ok ->
                                if (ok) {
                                    app.vault.setUnlockedSession(true)
                                    unlocked = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun promptUnlock(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(true)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(false)
                }
                override fun onAuthenticationFailed() {
                    onResult(false)
                }
            },
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock OpenWA Companion")
            .setSubtitle("Local app lock")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build()
        prompt.authenticate(info)
    }
}