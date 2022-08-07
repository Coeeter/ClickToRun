package com.example.clicktorun.data.models

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot

data class User constructor(
    val username: String,
    val email: String,
    val heightInMetres: Double,
    val weightInKilograms: Double,
    val profileImage: LiveData<String?>? = null,
) {
    constructor(doc: DocumentSnapshot, path: LiveData<String?>? = null) : this(
        doc.getString("username")!!,
        doc.id,
        doc.getDouble("heightInCentimetres")!! / 100.0,
        doc.getDouble("weightInKilograms")!!,
        path
    )
}