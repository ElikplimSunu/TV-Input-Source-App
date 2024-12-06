package com.sunueric.simulatetvremote

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class TvRemoteAccessibilityService : AccessibilityService()  {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
    }

    override fun onInterrupt() {
        // Handle interruptions
    }

    // Simulate a DPAD UP action using gestures
    fun simulateDpadUp() {
        val path = Path().apply {
            moveTo(500f, 500f) // Coordinates of the gesture start point
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d("AccessibilityService", "DPAD UP simulated successfully.")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.e("AccessibilityService", "DPAD UP simulation canceled.")
            }
        }, null)
    }
}