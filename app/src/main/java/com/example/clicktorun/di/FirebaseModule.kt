package com.example.clicktorun.di

import com.example.clicktorun.data.daos.FollowDao
import com.example.clicktorun.data.daos.PostDao
import com.example.clicktorun.data.daos.UserDao
import com.example.clicktorun.data.impls.FollowDaoImpl
import com.example.clicktorun.data.impls.PostDaoImpl
import com.example.clicktorun.data.impls.UserDaoImpl
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

    @Singleton
    @Provides
    fun providesPostDao(
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage,
        firebaseAuth: FirebaseAuth,
    ): PostDao = PostDaoImpl(firebaseFirestore, firebaseStorage, firebaseAuth)

    @Singleton
    @Provides
    fun providesFollowDao(
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ): FollowDao = FollowDaoImpl(firebaseFirestore, firebaseStorage)
}