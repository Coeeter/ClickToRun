package com.example.clicktorun.di

import com.example.clicktorun.data.daos.UserDao
import com.example.clicktorun.data.daos.UserDaoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    fun providesFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    fun providesFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    fun providesFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()

    @Singleton
    @Provides
    fun providesUserDao(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ): UserDao = UserDaoImpl(firebaseAuth, firebaseFirestore, firebaseStorage)
}