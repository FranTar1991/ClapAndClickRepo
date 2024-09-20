package com.example.clapandclick

import android.Manifest.permission.RECORD_AUDIO
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import android.text.TextUtils.SimpleStringSplitter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout);
        val clickButton = findViewById<Button>(R.id.click_button)

        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), 1)
        }
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 100)
        }


        clickButton.setOnClickListener {
            val intent = Intent(this, SoundListenerService::class.java)
            startService(intent)
        }

//        clickButton.setOnClickListener {
//            if (!isAccessibilityServiceEnabled()) {
//                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//                startActivity(intent)
//            } else {
//
//
//                sendCommandToService("MyCommand")
//
//
//            }
//        }

    }


    override fun onResume() {
        super.onResume()
        if (!isAccessibilityServiceEnabled()) {
            promptEnableAccessibilityService()
        }
    }

    private fun promptEnableAccessibilityService() {
        Toast.makeText(this, "Please enable the accessibility service", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(this,  MyAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        enabledServicesSetting?.let {
            colonSplitter.setString(enabledServicesSetting)
        }

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName.flattenToString(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }

}

