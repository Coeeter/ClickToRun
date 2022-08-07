package com.example.clicktorun.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Post
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.data.models.User
import com.example.clicktorun.repositories.FollowRepository
import com.example.clicktorun.repositories.PostRepository
import com.example.clicktorun.repositories.RunRepository
import com.example.clicktorun.repositories.UserRepository
import com.example.clicktorun.utils.getDistance
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val followRepository: FollowRepository,
) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _runRoute = MutableLiveData<List<List<Position>>>()
    val runRoute: LiveData<List<List<Position>>>
        get() = _runRoute

    private val _postState = MutableLiveData<PostState>()
    val postState: LiveData<PostState>
        get() = _postState

    private val _followingState = MutableLiveData<FollowingState>()
    val followingState: LiveData<FollowingState>
        get() = _followingState

    private val _selectedRun = MutableLiveData<Run>()
    val selectedRun: LiveData<Run>
        get() = _selectedRun

    private val _selectedRoute = MutableLiveData<List<List<Position>>>()
    val selectedRoute: LiveData<List<List<Position>>>
        get() = _selectedRoute

    fun setSelectedRun(run: Run) {
        _selectedRun.value = run
    }

    fun setSelectedRoute(route: List<List<Position>>) {
        _selectedRoute.value = route
    }

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

    fun deleteAllPositions() {
        viewModelScope.launch {
            runRepository.deleteAllPositions(userRepository.getCurrentUser()!!.email)
        }
    }

    fun getAllPosts() = postRepository.getAllPosts()

    fun insertPost(run: Run, route: List<List<Position>>) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            val result = postRepository.insertPost(
                Post(
                    run,
                    route,
                    userRepository.getCurrentUser()!!.username,
                    true
                )
            )
            if (!result) return@launch _postState.setValue(
                PostState.Failure(
                    "Unable to post this run, try " +
                            "checking your internet connection or try again later"
                )
            )
            _postState.value = PostState.Success(
                "Successfully posted your run",
                true
            )
            _postState.value = PostState.Idle
        }
    }

    fun removePost(runId: String) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            val result = postRepository.deletePost(runId)
            if (!result) return@launch _postState.setValue(
                PostState.Failure(
                    "Unable to hide this post, try " +
                            "checking your internet connection or try again later"
                )
            )
            _postState.value = PostState.Success(
                "Successfully hidden your post",
                false
            )
            _postState.value = PostState.Idle
        }
    }

    fun deleteAllPosts() {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            val result = postRepository.deleteAllPosts()
            if (!result) return@launch _postState.setValue(
                PostState.Failure(
                    "Unable to delete all posts, try " +
                            "checking your internet connection or try again later"
                )
            )
            _postState.value = PostState.Success(
                "Successfully deleted all your posts",
                false
            )
            _postState.value = PostState.Idle
        }
    }

    fun getEntryList(run: Run, positionList: List<Position>, graphType: GraphType): List<Entry> {
        val timeStarted = run.timeEnded - run.timeTakenInMilliseconds
        return positionList.mapIndexed { i, position ->
            val time = (position.timeReachedPosition - timeStarted).toFloat()
            when (graphType) {
                GraphType.Distance -> {
                    val latLngList = positionList.subList(0, i + 1).map { pos ->
                        pos.getLatLng()
                    }
                    val distanceRan = listOf(latLngList)
                        .getDistance()
                        .toFloat()
                    Entry(
                        time,
                        distanceRan
                    )
                }
                GraphType.AverageSpeed -> {
                    Entry(
                        time,
                        (position.speedInMetresPerSecond * 3.6).toFloat()
                    )
                }
                GraphType.CaloriesBurnt -> {
                    Entry(
                        time,
                        position.caloriesBurnt.toFloat()
                    )
                }
            }
        }
    }

    fun getFollowers(email: String) = followRepository.getAllFollowers(email)
    fun getFollowing(email: String) = followRepository.getAllFollowing(email)

    fun addFollow(userBeingFollowedEmail: String) {
        viewModelScope.launch {
            _followingState.value = FollowingState.Loading
            val insertResults = followRepository.insertFollow(
                userBeingFollowedEmail,
                userRepository.getCurrentUser()!!.email
            )
            if (!insertResults) return@launch _followingState.setValue(
                FollowingState.Failure(
                    "Unable to follow this user, try " +
                            "checking your internet connection or try again later"
                )
            )
            _followingState.value = FollowingState.Success
            _followingState.value = FollowingState.Idle
        }
    }

    fun unfollowUser(userToUnfollow: String) {
        viewModelScope.launch {
            _followingState.value = FollowingState.Loading
            val removeResult = followRepository.deleteFollow(
                userToUnfollow,
                userRepository.getCurrentUser()!!.email
            )
            if (!removeResult) return@launch _followingState.setValue(
                FollowingState.Failure(
                    "Unable to unfollow this user, try checking" +
                            " your internet connection or try again later"
                )
            )
            _followingState.value = FollowingState.Success
            _followingState.value = FollowingState.Idle
        }
    }

    fun deleteAllFollowsAndFollowing() {
        viewModelScope.launch {
            _followingState.value = FollowingState.Loading
            val removeResult = followRepository.deleteAllFollowLinksRelatedToUser(
                userRepository.getCurrentUser()!!.email
            )
            if (!removeResult) return@launch _followingState.setValue(
                FollowingState.Failure(
                    "Unable to delete all follows and following of this account," +
                            " try checking your internet connection or try again later"
                )
            )
            _followingState.value = FollowingState.SuccessInRemovingEverything
            _followingState.value = FollowingState.Idle
        }
    }

    sealed class FollowingState {
        object Idle : FollowingState()
        object Loading : FollowingState()
        object SuccessInRemovingEverything : FollowingState()
        object Success : FollowingState()
        class Failure(val message: String) : FollowingState()
    }

    sealed class PostState {
        object Idle : PostState()
        object Loading : PostState()
        class Success(val message: String, val isInserted: Boolean) : PostState()
        class Failure(val message: String) : PostState()
    }

    enum class GraphType {
        Distance,
        AverageSpeed,
        CaloriesBurnt,
    }
}