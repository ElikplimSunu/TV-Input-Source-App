package com.sunueric.simulatetvremote

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.tv.TvInputManager
import android.net.Uri
import android.os.IBinder
import android.util.Log

class InputSwitchService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start switching logic in a new thread
        Thread {
            switchToHdmi() // Logic for switching to HDMI
            Thread.sleep(5000) // Wait for a set amount of time (e.g., 5 seconds)
            switchBackToApp() // Logic to return to app's UI
        }.start()

        // Make this a foreground service
//        val notification = createNotification()
//        startForeground(1, notification)

        return START_NOT_STICKY
    }

    private fun switchToHdmi() {
        val tvInputManager = getSystemService(Context.TV_INPUT_SERVICE) as TvInputManager

        // Replace with the actual HDMI input ID for your device
        val inputId = "com.tcl.tvpassthrough/.TvPassThroughService/HW1413744128"
        val inputUri = Uri.parse("tvinput://$inputId")

        try {
//            tvInputManager.tune(inputId, inputUri)
        } catch (e: Exception) {
            Log.e("InputSwitchService", "Failed to switch to HDMI input", e)
        }
    }


    private fun switchBackToApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent) // Brings the app back to the foreground
    }

//    private fun createNotification(): Notification {
//        return NotificationCompat.Builder(this, "inputSwitchChannel")
//            .setContentTitle("Input Switch")
//            .setContentText("Switching between inputs...")
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .build()
//    }

    override fun onBind(intent: Intent?): IBinder? = null

}
