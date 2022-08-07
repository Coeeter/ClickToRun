package com.example.clicktorun.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.clicktorun.data.models.Position

@Dao
interface PositionDao {

    @Query("SELECT * FROM positions WHERE runId = :runId")
    fun getPositionList(runId: String?): List<Position>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosition(position: Position)

    @Query("DELETE FROM positions WHERE runId = :runId")
    fun deletePositionList(runId: String?)

    @Query("DELETE FROM positions WHERE email = :email")
    fun deleteAllPositions(email: String)

}