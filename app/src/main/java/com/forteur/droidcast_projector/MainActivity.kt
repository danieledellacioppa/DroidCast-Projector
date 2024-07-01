package com.forteur.droidcast_projector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import com.forteur.droidcast_projector.ui.theme.DroidCastProjectorTheme

class MainActivity : ComponentActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    private lateinit var startProjectionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        startProjectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
                startScreenCapture(mediaProjection)
            }
        }

        setContent {
            DroidCastProjectorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        startScreenCasting = {
                            startProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun startScreenCapture(mediaProjection: MediaProjection?) {
        // Implement the screen capture and network streaming logic here
        // This function will start capturing the screen and streaming it to the receiver
    }
}

@Composable
fun MainScreen(startScreenCasting: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Screen Casting App")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = startScreenCasting) {
            Text(text = "Start Screen Casting")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DroidCastProjectorTheme {
        MainScreen(startScreenCasting = {})
    }
}
