package com.example.clapandclick

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

class SoundListenerService : Service() {

    private var isListening = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()  // Create the channel (if needed)

        val notification = createNotification()  // Create a valid notification
        startForeground(1, notification)

        isListening = true
        thread {
            listenForClap()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "channel_id"
            val channelName = "Clap Detection Service"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        // Ensure you have a valid notification channel for Android 8.0+
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "channel_id" // The same ID used in createNotificationChannel()
        } else {
            ""
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Clap Detection Service")
            .setContentText("Listening for claps...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Ensure you have a valid icon
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)  // Avoid unnecessary high-priority notifications
            .build()
    }



    private fun listenForClap() {
        val bufferSize = AudioRecord.getMinBufferSize(
            44100,
            android.media.AudioFormat.CHANNEL_IN_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT
        )

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            android.media.AudioFormat.CHANNEL_IN_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val buffer = ShortArray(bufferSize)
        audioRecord.startRecording()

        while (isListening) {
            val readSize = audioRecord.read(buffer, 0, bufferSize)
            if (readSize > 0) {
                val amplitude = detectAmplitude(buffer)

                if (amplitude > 30000) {  // Adjust this threshold based on clap detection testing

                    sendBroadcast()
                }
            }
        }

        audioRecord.stop()
        audioRecord.release()
    }

    private fun sendBroadcast(){
        val intent = Intent("com.example.ACCESSIBILITY_COMMUNICATION")
        intent.putExtra("data", "Some data")
        sendBroadcast(intent)
    }
    private fun detectAmplitude(buffer: ShortArray): Int {
        return buffer.maxOrNull()?.toInt() ?: 0
    }

    override fun onDestroy() {
        super.onDestroy()
        isListening = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
