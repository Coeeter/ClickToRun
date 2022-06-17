package com.example.clicktorun.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs")
data class Run(
    var distanceRanInMetres: Int = 0,
    var timeEnded: Long = 0,
    var timeTaken: Long = 0,
    var bitmap: Bitmap? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
