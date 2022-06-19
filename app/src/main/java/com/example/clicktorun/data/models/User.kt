package com.example.clicktorun.data.models

import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val username: String,
    val heightInMetres: Double,
    val weightInKilograms: Double,
) {
    constructor(map: DocumentSnapshot) : this(
        map.getString("username")!!,
        map.getDouble("heightInCentimetres")!! / 100.0,
        map.getDouble("weightInKilograms")!!
    )
}