package com.example.clicktorun.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "positions")
data class Position(
    var runId: String? = null,
    var polylineId: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timeReachedPosition: Long = 0,
    var caloriesBurnt: Double = 0.0,
    var speedInMetresPerSecond: Float = 0f,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    fun getLatLng() = LatLng(latitude, longitude)
}