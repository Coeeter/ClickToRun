package com.example.clicktorun.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.clicktorun.data.models.Run

@Dao
interface RunDao {
    @Query("SELECT * FROM runs WHERE email = :email")
    fun getAllRuns(email: String): LiveData<List<Run>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRun(run: Run)

    @Query("DELETE FROM runs WHERE id IN (:idList)")
    fun deleteRun(idList: List<String>)

    @Query("DELETE FROM runs WHERE email = :email")
    fun deleteAllRuns(email: String)
}