package com.example.clicktorun.data.models

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs")
data class Run(
    var email: String? = null,
    var distanceRanInMetres: Int = 0,
    var timeEnded: Long = 0,
    var timeTakenInMilliseconds: Long = 0,
    var averageSpeedInKilometersPerHour: Double? = null,
    var caloriesBurnt: Double? = null,
    var lightModeImage: Bitmap? = null,
    var darkModeImage: Bitmap? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
