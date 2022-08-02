package com.example.clicktorun.repositories

import com.example.clicktorun.data.daos.PositionDao
import com.example.clicktorun.data.daos.RunDao
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Run
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepository @Inject constructor(
    private val runDao: RunDao,
    private val positionDao: PositionDao,
) {
    fun getRunList(email: String) = runDao.getAllRuns(email)

    fun getPositionList(run: Run) = positionDao.getPositionList(run.id)

    suspend fun insertPosition(positionList: List<Position>) {
        withContext(Dispatchers.IO) {
            for (position in positionList)
                positionDao.insertPosition(position)
        }
    }

    suspend fun deletePositions(runId: String) {
        withContext(Dispatchers.IO) {
            positionDao.deletePositionList(runId)
        }
    }

    suspend fun insertRunToLocal(run: Run) {
        withContext(Dispatchers.IO) {
            runDao.insertRun(run)
        }
    }

    suspend fun deleteRunFromLocal(listOfId: List<String>) {
        withContext(Dispatchers.IO) {
            runDao.deleteRun(listOfId)
        }
    }

    suspend fun deleteAllRunsFromLocal(email: String) {
        withContext(Dispatchers.IO) {
            runDao.deleteAllRuns(email)
        }
    }
}
