package com.example.clicktorun.data.models

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot

@Entity(tableName = "runs")
data class Run(
    @PrimaryKey var id: String,
    var email: String? = null,
    var distanceRanInMetres: Int = 0,
    var timeEnded: Long = 0,
    var timeTakenInMilliseconds: Long = 0,
    var averageSpeedInKilometersPerHour: Double? = null,
    var caloriesBurnt: Double? = null,
    var lightModeImage: Bitmap? = null,
    var darkModeImage: Bitmap? = null
) {
    constructor(doc: DocumentSnapshot) : this(
        doc.id,
        doc.getString("email"),
        doc.getDouble("distanceRan")?.toInt() ?: 0,
        doc.getLong("timeEnded") ?: 0,
        doc.getLong("timeTaken") ?: 0,
        doc.getDouble("averageSpeed"),
        doc.getDouble("caloriesBurnt"),
    )

    fun toMap() = hashMapOf(
        "email" to email,
        "distanceRan" to distanceRanInMetres,
        "timeEnded" to timeEnded,
        "timeTaken" to timeTakenInMilliseconds,
        "averageSpeed" to averageSpeedInKilometersPerHour,
        "caloriesBurnt" to caloriesBurnt,
        "lightModeImage" to "light-${id}",
        "darkModeImage" to "dark-${id}"
    )
}