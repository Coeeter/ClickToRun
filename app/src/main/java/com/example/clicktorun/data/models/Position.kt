package com.example.clicktorun.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "positions")
data class Position(
    var email: String? = null,
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

    constructor(map: Map<String, Any>, polylineId: String, runId: String?, email: String) : this(
        email,
        runId,
        polylineId,
        map["latitude"] as Double,
        map["longitude"] as Double,
        map["timeReached"] as Long,
        map["caloriesBurnt"] as Double,
        (map["speed"] as Double).toFloat(),
    )

    fun toMap() = hashMapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "timeReached" to timeReachedPosition,
        "caloriesBurnt" to caloriesBurnt,
        "speed" to speedInMetresPerSecond,
    )
}