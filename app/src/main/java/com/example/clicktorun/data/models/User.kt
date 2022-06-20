package com.example.clicktorun.data.models

import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val username: String,
    val email: String,
    val heightInMetres: Double,
    val weightInKilograms: Double,
) {
    constructor(doc: DocumentSnapshot) : this(
        doc.getString("username")!!,
        doc.id,
        doc.getDouble("heightInCentimetres")!! / 100.0,
        doc.getDouble("weightInKilograms")!!
    )
}