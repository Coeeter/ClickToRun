package com.example.clicktorun.di

import android.content.Context
import androidx.room.Room
import com.example.clicktorun.data.daos.PositionDao
import com.example.clicktorun.data.daos.RunDao
import com.example.clicktorun.data.databases.RunDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun providesRunDatabase(
        @ApplicationContext context: Context
    ): RunDatabase = Room.databaseBuilder(
        context,
        RunDatabase::class.java,
        "runDatabase"
    ).build()

    @Singleton
    @Provides
    fun providesRunDao(
        db: RunDatabase
    ): RunDao = db.getRunDao()

    @Singleton
    @Provides
    fun providesPositionDao(
        db: RunDatabase
    ): PositionDao = db.getPositionDao()

}