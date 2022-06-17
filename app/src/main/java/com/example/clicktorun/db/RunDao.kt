package com.example.clicktorun.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {
    @Query("SELECT * FROM runs")
    fun getAllRuns(): LiveData<List<Run>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRun(run: Run)

    @Delete
    fun deleteRun(run: Run)

    @Query("DELETE FROM runs")
    fun deleteAllRuns()
}