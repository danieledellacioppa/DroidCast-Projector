package com.forteur.droidcast_projector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ScreenCaptureActivity : AppCompatActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_OK)
        val data: Intent? = intent.getParcelableExtra("data")
        val ipAddress = intent.getStringExtra("ipAddress") // Extract the IP address
        if (resultCode == Activity.RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
                putExtra("ipAddress", ipAddress) // Pass the IP address to the service
            }
            startForegroundService(serviceIntent)
            finish()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

