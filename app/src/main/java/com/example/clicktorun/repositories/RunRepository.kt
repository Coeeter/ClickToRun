package com.example.clicktorun.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.clicktorun.db.Run
import com.example.clicktorun.db.RunDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepository @Inject constructor(
    private val runDao: RunDao
) {
    val runList = runDao.getAllRuns()

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

    suspend fun deleteAllRunsFromLocal() {
        withContext(Dispatchers.IO) {
            runDao.deleteAllRuns()
        }
    }
}
