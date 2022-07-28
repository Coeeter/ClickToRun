package com.example.clicktorun.data.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.clicktorun.data.converters.RunConverter
import com.example.clicktorun.data.daos.PositionDao
import com.example.clicktorun.data.daos.RunDao
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Run

@Database(
    entities = [Run::class, Position::class],
    version = 1
)
@TypeConverters(RunConverter::class)
abstract class RunDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDao
    abstract fun getPositionDao(): PositionDao
}