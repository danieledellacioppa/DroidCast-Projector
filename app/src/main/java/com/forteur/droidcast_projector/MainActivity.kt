package com.forteur.droidcast_projector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.forteur.droidcast_projector.ui.theme.DroidCastProjectorTheme
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : ComponentActivity() {

    // Stato condiviso per l'indirizzo IP, utile sia per l'input manuale che per il risultato della scansione QR
    private val ipAddressState = mutableStateOf("")
    private var quality: Int = 50 // Default quality

    private val startScreenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(this, ScreenCaptureActivity::class.java).apply {
                putExtra("data", result.data)
                putExtra("resultCode", result.resultCode)
                putExtra("ipAddress", ipAddressState.value) // Pass the IP address
                putExtra("quality", quality) // Pass the selected quality
            }
            startActivity(intent)
        } else {
            // Handle permission denial
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DroidCastProjectorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        ipAddress = ipAddressState.value,
                        onIpAddressChange = { nuovoIp -> ipAddressState.value = nuovoIp },
                        onScanQrCode = { scanQrCode() },
                        startScreenCasting = { ip, q ->
                            ipAddressState.value = ip
                            quality = q
                            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                            startScreenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Avvia l'attività di scansione QR utilizzando ZXing.
     */
    private fun scanQrCode() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // Scansiona solo codici QR
        integrator.setPrompt("Scansiona il codice QR")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    /**
     * Gestisce il risultato della scansione QR.
     * Il metodo IntentIntegrator.parseActivityResult() interpreta i dati restituiti dalla scansione.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Prova a interpretare il risultato della scansione QR
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val scannedResult = result.contents
                // Supponendo che il formato sia sempre "http://<INDIRIZZO-IP>:8080"
                val ip = if (scannedResult.startsWith("http://") && scannedResult.contains(":")) {
                    scannedResult.removePrefix("http://").substringBefore(":")
                } else {
                    // In caso il formato non corrisponda, usiamo il risultato così com'è
                    scannedResult
                }
                // Aggiorna lo stato con il solo indirizzo IP
                ipAddressState.value = ip
            } else {
                // La scansione è stata annullata
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

@Composable
fun MainScreen(
    ipAddress: String,
    onIpAddressChange: (String) -> Unit,
    onScanQrCode: () -> Unit,
    startScreenCasting: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
//    var ipAddress by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf(50) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Screen Casting App")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = ipAddress,
            onValueChange = onIpAddressChange,
            label = { Text(text = "Enter The Receiver IP Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onScanQrCode) {
            Text(text = "Scansiona codice QR")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Select Quality")
        Slider(
            value = quality.toFloat(),
            onValueChange = { quality = it.toInt() },
            valueRange = 1f..100f,
            steps = 98
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { startScreenCasting(ipAddress, quality) }) {
            Text(text = "Start Screen Casting")
        }
    }
}
