package com.example.clicktorun.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Position
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.math.roundToInt

fun Activity.startActivityWithAnimation(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.exit_animation)
}

fun Long.toTimeString(): String {
    val seconds = formatTime((this / 1000 % 60).toInt())
    val minutes = formatTime((this / 1000 / 60 % 60).toInt())
    val hours = formatTime((this / 1000 / 60 / 60).toInt())
    return "$hours:$minutes:$seconds"
}

fun Long.getDate(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@getDate
    }
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)
    return "$day/$month/$year"
}

fun Long.getTime(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@getTime
    }
    var hour = formatTime(calendar.get(Calendar.HOUR_OF_DAY))
    val minute = formatTime(calendar.get(Calendar.MINUTE))
    val second = formatTime(calendar.get(Calendar.SECOND))
    var units = "am"
    if (hour.toInt() >= 12) {
        if (hour.toInt() != 12) hour = formatTime(hour.toInt() - 12)
        units = "pm"
    }
    return "$hour:$minute:$second$units"
}

private fun formatTime(time: Int): String {
    if (time < 10) return "0$time"
    return time.toString()
}

fun Int.formatDistance(): String {
    val metres = this % 1000
    val kilometres = this / 1000
    if (kilometres == 0) return "${metres}m"
    if (metres == 0) return "${kilometres}km"
    return "${kilometres}km ${metres}m"
}

fun List<List<LatLng>>.getDistance(): Int {
    var totalDistance = 0f
    this.forEach { list ->
        if (list.isEmpty()) return@forEach
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
    snackBar.view.findViewById<TextView>(
        com.google.android.material.R.id.snackbar_text
    ).maxLines = 5
    return snackBar
}

fun Activity.hideKeyboard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
}

fun FragmentActivity.setActionToolbar(toolbar: Toolbar) {
    (this as AppCompatActivity).setSupportActionBar(toolbar)
}

fun Context.isDeviceInLandscape() =
    this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun List<List<Position>>.convertToLatLng(): List<List<LatLng>> {
    return map {
        it.map { pos ->
            pos.getLatLng()
        }
    }
}