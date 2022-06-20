package com.example.clicktorun.ui.tracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.data.models.User
import com.example.clicktorun.repositories.RunRepository
import com.example.clicktorun.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    fun getRunList(email: String) = runRepository.getRunList(email)

    fun getCurrentUser() {
        viewModelScope.launch {
            _user.postValue(userRepository.getCurrentUser())
        }
    }

    fun saveRun(run: Run) {
        viewModelScope.launch {
            runRepository.insertRunToLocal(run)
        }
    }

    fun deleteAllRuns(email: String) {
        viewModelScope.launch {
            runRepository.deleteAllRunsFromLocal(email)
        }
    }
}