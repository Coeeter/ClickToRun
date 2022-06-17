package com.example.clicktorun.ui.tracking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicktorun.db.Run
import com.example.clicktorun.repositories.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel() {
    val runList = runRepository.runList

    fun saveRun(run: Run) {
        viewModelScope.launch {
            runRepository.insertRunToLocal(run)
        }
    }

    fun deleteAllRuns() {
        viewModelScope.launch {
            runRepository.deleteAllRunsFromLocal()
        }
    }
}