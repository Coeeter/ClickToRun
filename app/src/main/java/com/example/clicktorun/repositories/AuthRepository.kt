package com.example.clicktorun.repositories

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun getCurrentUser() =
        firebaseAuth.currentUser

    fun signOutUser() =
        firebaseAuth.signOut()

    fun login(email: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun signUp(email: String, password: String) =
        firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun sendPasswordResetLink(email: String) =
        firebaseAuth.sendPasswordResetEmail(email)
}