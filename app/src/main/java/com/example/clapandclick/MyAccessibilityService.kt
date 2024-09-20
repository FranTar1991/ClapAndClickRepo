package com.example.clapandclick

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible

class MyAccessibilityService: AccessibilityService() {


    private lateinit var displayMetrics: DisplayMetrics
    private var density: Float = 1f

    private lateinit var windowManager: WindowManager
    private lateinit var floatingWidget: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var pointerImg: ImageView

    private var initialX = 0
    private var initialY = 0
    private var offsetX = 0
    private var offsetY = 0

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val statusBarHeight = getStatusBarHeight()
            val navigationBarHeight = getNavigationBarHeight()
            val toCenterPoint = ((48f * density)/ 2).toInt()

            pointerImg.post {
                setIsClickable(false, pointerImg)

                // Perform global click
                val clickPerformed = performGlobalClick(
                    layoutParams.x - (toCenterPoint / 2),
                    layoutParams.y + navigationBarHeight - (toCenterPoint / 2)
                )

                if (clickPerformed) {
                    // Re-enable the ImageView after the click
                    pointerImg.postDelayed({
                        setIsClickable(true, pointerImg)
                    }, 500) // Delay the re-enabling to allow time for the click to complete
                }
            }
        }
    }

    // Function to perform click
    fun performGlobalClick(x: Int, y: Int): Boolean {

        val clickPath = Path()
        clickPath.moveTo(x.toFloat(), y.toFloat()) // Coordinates for the click
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(StrokeDescription(clickPath, 0, 100))
       return dispatchGesture(gestureBuilder.build(), null, null)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onServiceConnected() {
        super.onServiceConnected()
        val intentFilter = IntentFilter("com.example.ACCESSIBILITY_COMMUNICATION")
        registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED)
        displayMetrics = resources.displayMetrics
        density = displayMetrics.density
        createFloatingWidget()
    }

    private fun createFloatingWidget() {
        // Inflate your widget layout
        floatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null)
        pointerImg = floatingWidget.findViewById(R.id.pointer_Img)

        // Configure the layout parameters for the widget
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // Initial position
        layoutParams.gravity = Gravity.TOP or Gravity.LEFT
        layoutParams.x = 0
        layoutParams.y = 0

        // Get WindowManager to add the view to the screen
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingWidget, layoutParams)

        // Implement touch listener to drag and move the widget
        floatingWidget.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    offsetX = event.rawX.toInt()
                    offsetY = event.rawY.toInt()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = (initialX + (event.rawX.toInt() - offsetX))
                    layoutParams.y = (initialY + (event.rawY.toInt() - offsetY))
                    windowManager.updateViewLayout(floatingWidget, layoutParams)


                    true
                }
                else -> false
            }
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }
    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

}

fun setIsClickable(isClickable: Boolean, imageView: ImageView) {
    imageView.isVisible = isClickable

    // Optional: Provide visual feedback
    imageView.alpha = if (isClickable) 1.0f else 0.5f
    println("FrankLogs: setIsClickable ${imageView.isClickable} ${imageView.isEnabled}")
}
