package com.example.alcoolougasolina.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

fun vibrar(context: Context) {
    val buzzer = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val pattern = longArrayOf(0, 200, 100, 300)
    buzzer?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            it.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(pattern, -1)
        }
    }
}