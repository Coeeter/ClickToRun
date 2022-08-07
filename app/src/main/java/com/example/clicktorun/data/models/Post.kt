package com.example.clicktorun.data.models

import androidx.lifecycle.LiveData

data class Post(
    val run: Run,
    val route: List<List<Position>>,
    val username: String,
    val isCurrentUser: Boolean,
    val lightModeImage: LiveData<String?>? = null,
    val darkModeImage: LiveData<String?>? = null,
    val profileImage: LiveData<String?>? = null,
)