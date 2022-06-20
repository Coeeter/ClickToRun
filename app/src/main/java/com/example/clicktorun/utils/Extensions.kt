package com.example.clicktorun.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.view.View
import com.example.clicktorun.R
import com.example.clicktorun.services.Line
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

fun Activity.startActivityWithAnimation(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.exit_animation)
}

fun Activity.endActivityWithAnimation() {
    overridePendingTransition(R.anim.enter_animation, R.anim.slide_out_right)
}

fun Long.toTimeString(): String {
    val seconds = (this / 1000 % 60).run {
        if (this < 10) return@run "0$this"
        this.toString()
    }
    val minutes = (this / 1000 / 60 % 60).run {
        if (this < 10) return@run "0$this"
        this.toString()
    }
    val hours = (this / 1000 / 60 / 60).run {
        if (this < 10) return@run "0$this"
        this.toString()
    }
    return "$hours:$minutes:$seconds"
}

fun Int.formatDistance(): String {
    val metres = this % 1000
    val kilometres = this / 1000
    if (metres == 0) return "${kilometres}km"
    if (kilometres == 0) return "${metres}m"
    return "${kilometres}km ${metres}m"
}

fun MutableList<Line>.getDistance(): Int {
    var totalDistance = 0f
    this.forEach { list ->
        if (list.size == 0) return@forEach
        for (i in 0 until list.lastIndex) {
            totalDistance += FloatArray(1).apply {
                Location.distanceBetween(
                    list[i].latitude,
                    list[i].longitude,
                    list[i + 1].latitude,
                    list[i + 1].longitude,
                    this
                )
            }[0]
        }
    }
    return totalDistance.roundToInt()
}

fun Context.isNightModeEnabled(): Boolean =
    resources.configuration.uiMode.and(
        Configuration.UI_MODE_NIGHT_MASK
    ) == Configuration.UI_MODE_NIGHT_YES

fun View.createSnackBar(
    message: String,
    length: Int = Snackbar.LENGTH_SHORT,
    okayAction: Boolean = false
): Snackbar {
    val snackBar = Snackbar.make(this, message, length)
    if (okayAction) snackBar.apply {
        setAction("Okay") { dismiss() }
    }
    return snackBar
}