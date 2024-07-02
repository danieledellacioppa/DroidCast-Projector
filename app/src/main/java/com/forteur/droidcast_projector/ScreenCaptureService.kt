package com.forteur.droidcast_projector


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.Socket

class ScreenCaptureService : Service() {

    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var imageReader: ImageReader
    private var ipAddress: String = ""

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_OK) ?: Activity.RESULT_OK
        val data: Intent? = intent?.getParcelableExtra("data")
        ipAddress = intent?.getStringExtra("ipAddress") ?: "" // Extract the IP address
        if (data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            mediaProjection.registerCallback(MediaProjectionCallback(), null)
            startScreenCapture()
        }

        return START_NOT_STICKY
    }

    private fun startScreenCapture() {
        val displayMetrics = DisplayMetrics()
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val density = displayMetrics.densityDpi
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, null
        )

        Thread {
            while (true) {
                val image = imageReader.acquireLatestImage() ?: continue
                sendImageData(image)
                image.close()
            }
        }.start()
    }

    private fun sendImageData(image: Image) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val originalBitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
        originalBitmap.copyPixelsFromBuffer(buffer)

        // Resize the bitmap
        val newWidth = originalBitmap.width / 2
        val newHeight = originalBitmap.height / 2
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

        // Compress the bitmap to JPEG
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        try {
            val socket = Socket(ipAddress, PORT) // Use the IP address from the intent
            val outputStream = DataOutputStream(socket.getOutputStream())
            outputStream.writeInt(byteArray.size)
            outputStream.write(byteArray)
            outputStream.flush()
            outputStream.close()
            socket.close()
            Log.d("ScreenCaptureService", "Sent image data of size: ${byteArray.size}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ScreenCaptureService", "Error sending image data", e)
        }
    }

    private fun startForegroundService() {
        val notificationChannelId = "SCREEN_CAPTURE_CHANNEL"

        val channel = NotificationChannel(
            notificationChannelId,
            "Screen Capture Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Screen Capture Service")
            .setContentText("Capturing screen...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            // Handle cleanup if needed when the projection stops
            stopSelf()
        }
    }

    companion object {
        const val PORT = 12345 // Replace with your desired port number
    }
}
