package com.example.clicktorun.data.models

import com.google.firebase.firestore.DocumentSnapshot

class FollowLink(
    val userFollowingEmail: String,
    val userBeingFollowedEmail: String,
) {
    constructor(document: DocumentSnapshot) : this(
        document.getString("userFollowing")!!,
        document.getString("userBeingFollowed")!!,
    )

    fun toMap() = hashMapOf(
        "userFollowing" to userFollowingEmail,
        "userBeingFollowed" to userBeingFollowedEmail
    )
}