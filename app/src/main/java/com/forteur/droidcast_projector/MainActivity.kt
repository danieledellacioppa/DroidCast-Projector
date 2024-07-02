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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.forteur.droidcast_projector.ui.theme.DroidCastProjectorTheme

class MainActivity : ComponentActivity() {
    private val startScreenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(this, ScreenCaptureActivity::class.java).apply {
                putExtra("data", result.data)
                putExtra("resultCode", result.resultCode)
                putExtra("ipAddress", ipAddress) // Pass the IP address
                putExtra("quality", quality) // Pass the selected quality
            }
            startActivity(intent)
        } else {
            // Handle permission denial
        }
    }

    private var ipAddress: String = ""
    private var quality: Int = 50 // Default quality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DroidCastProjectorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        startScreenCasting = { ip, q ->
                            ipAddress = ip
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
}

@Composable
fun MainScreen(startScreenCasting: (String, Int) -> Unit, modifier: Modifier = Modifier) {
    var ipAddress by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf(50) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Screen Casting App")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text(text = "Enter Receiver IP Address") },
            singleLine = true
        )
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DroidCastProjectorTheme {
        MainScreen(startScreenCasting = { _, _ -> })
    }
}
