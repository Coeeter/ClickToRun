package com.example.clicktorun.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.data.models.User
import com.example.clicktorun.repositories.RunRepository
import com.example.clicktorun.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _runRoute = MutableLiveData<List<List<Position>>>()
    val runRoute: LiveData<List<List<Position>>>
        get() = _runRoute

    fun getRunList(email: String) = runRepository.getRunList(email)

    fun getCurrentUser() {
        viewModelScope.launch {
            _user.postValue(userRepository.getCurrentUser())
        }
    }

    fun saveRun(run: Run): String {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            run.id = id
            runRepository.insertRunToLocal(run)
        }
        return id
    }

    fun deleteAllRuns(email: String) {
        viewModelScope.launch {
            runRepository.deleteAllRunsFromLocal(email)
        }
    }

    fun deleteRun(listOfRuns: List<Run>) {
        viewModelScope.launch {
            runRepository.deleteRunFromLocal(listOfRuns.map { it.id!! })
        }
    }

    fun getPositionList(run: Run) {
        viewModelScope.launch(Dispatchers.IO) {
            val positionList = runRepository.getPositionList(run)
            val listOfPolylineId = mutableListOf<String>()
            positionList.forEach {
                if (it.polylineId in listOfPolylineId) return@forEach
                listOfPolylineId.add(it.polylineId!!)
            }
            val route = mutableListOf<List<Position>>()
            listOfPolylineId.forEach {
                route.add(positionList.filter { position ->
                    position.polylineId == it
                })
            }
            _runRoute.postValue(route)
        }
    }

    fun insertPositionList(positionList: List<List<Position>>) {
        viewModelScope.launch {
            val insertList = mutableListOf<Position>()
            for (list in positionList) {
                val id = UUID.randomUUID()
                insertList.addAll(list.map {
                    it.apply {
                        polylineId = id.toString()
                    }
                })
            }
            runRepository.insertPosition(insertList)
        }
    }

    fun deletePositionList(runList: List<Run>) {
        val idList = runList.map { it.id }
        viewModelScope.launch {
            for (id in idList) runRepository.deletePositions(id)
        }
    }
}