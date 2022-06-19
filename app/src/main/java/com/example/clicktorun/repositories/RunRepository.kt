package com.example.clicktorun.repositories

import com.example.clicktorun.data.daos.RunDao
import com.example.clicktorun.data.models.Run
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepository @Inject constructor(
    private val runDao: RunDao
) {
    fun getRunList(email: String) = runDao.getAllRuns(email)

    suspend fun insertRunToLocal(run: Run) {
        withContext(Dispatchers.IO) {
            runDao.insertRun(run)
        }
    }

    suspend fun deleteRunFromLocal(run: Run) {
        withContext(Dispatchers.IO) {
            runDao.deleteRun(run)
        }
    }

    suspend fun deleteAllRunsFromLocal(email: String) {
        withContext(Dispatchers.IO) {
            runDao.deleteAllRuns(email)
        }
    }
}
