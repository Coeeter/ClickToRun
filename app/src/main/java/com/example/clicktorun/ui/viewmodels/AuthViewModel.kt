package com.example.clicktorun.ui.viewmodels

import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicktorun.data.models.User
import com.example.clicktorun.repositories.AuthRepository
import com.example.clicktorun.repositories.RunRepository
import com.example.clicktorun.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val runRepository: RunRepository,
) : ViewModel() {
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState>
        get() = _authState

    var email: String? = null
    var password: String? = null
    var confirmPassword: String? = null
    var username: String? = null
    var weight: String? = null
    var height: String? = null
    var uri: Uri? = null

    val currentUser = authRepository.getAuthUser()

    fun logIn() {
        viewModelScope.launch {
            val emailCheck = !validateEmailFields(email)
            val passwordCheck = !validatePasswordFields(password, null)
            if (emailCheck || passwordCheck) return@launch
            _authState.value = AuthState.Loading
            try {
                authRepository.login(email!!, password!!).await()
                _authState.value = AuthState.Success
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.FireBaseFailure(e.message)
                _authState.value = AuthState.Idle
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            val emailCheck = !validateEmailFields(email)
            val passwordCheck = !validatePasswordFields(password, confirmPassword, true)
            if (emailCheck || passwordCheck) return@launch
            _authState.value = AuthState.Loading
            try {
                authRepository.signUp(email!!, password!!).await()
                _authState.value = AuthState.Success
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.FireBaseFailure(e.message)
                _authState.value = AuthState.Idle
            }
        }
    }

    fun sendPasswordResetLinkToEmail() {
        if (!validateEmailFields(email)) return
        _authState.postValue(AuthState.Loading)
        authRepository.sendPasswordResetLink(email!!)
        _authState.postValue(AuthState.Success)
    }

    fun insertUser() {
        val checkUsername = !validateUsername(username)
        val checkWeight = !validateWeight(weight)
        val checkHeight = !validateHeight(height)
        if (checkUsername || checkWeight || checkHeight) return
        val user = User(
            username!!,
            authRepository.getAuthUser()!!.email!!,
            height!!.toDouble() / 100,
            weight!!.toDouble()
        )
        _authState.value = (AuthState.Loading)
        viewModelScope.launch {
            if (userRepository.insertUser(user))
                return@launch _authState.postValue(AuthState.Success)
            _authState.postValue(AuthState.FireBaseFailure())
        }
    }

    fun updateUser(validate: Boolean = false) {
        val checkUsername = !validateUsername(username)
        val checkWeight = !validateWeight(weight)
        val checkHeight = !validateHeight(height)
        if ((checkUsername || checkWeight || checkHeight) && !validate) return
        val hashMap = HashMap<String, Any>()
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val currentUser = userRepository.getUser()
            if (!validate && username != currentUser!!.username)
                hashMap["username"] = username!!
            if (!validate && height!!.toDouble() != currentUser!!.heightInMetres * 100)
                hashMap["heightInCentimetres"] = height!!.toDouble()
            if (!validate && weight!!.toDouble() != currentUser!!.weightInKilograms)
                hashMap["weightInKilograms"] = weight!!.toDouble()
            if (hashMap.isEmpty() && uri == null)
                return@launch _authState.postValue(AuthState.FireBaseFailure("No data has been changed!"))
            if (userRepository.updateUser(hashMap, uri)) {
                uri = null
                return@launch _authState.setValue(AuthState.Success).run {
                    _authState.value = AuthState.Idle
                }
            }
            _authState.setValue(AuthState.FireBaseFailure()).run {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun deleteImage() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            if (userRepository.deleteImage(authRepository.getAuthUser()!!.email!!))
                return@launch _authState.setValue(AuthState.Success).run {
                    _authState.value = AuthState.Idle
                }
            _authState.setValue(AuthState.FireBaseFailure()).run {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun deleteUserAccount() {
        if (!validatePasswordFields(password, null)) return
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val email = authRepository.getAuthUser()!!.email!!
                authRepository.login(email, password!!).await()
                runRepository.deleteAllRunsFromLocal(email)
                val deleteUserResult = userRepository.deleteUser(email)
                if (!deleteUserResult)
                    return@launch _authState.setValue(AuthState.FireBaseFailure())
                authRepository.getAuthUser()?.delete()?.await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.FireBaseFailure(
                    e.message
                )
            }
        }
    }

    fun getCurrentUserState() {
        viewModelScope.launch {
            val fireStoreUser = userRepository.getUser()
            if (fireStoreUser == null) {
                _authState.value = AuthState.FireStoreUserNotFound
                return@launch
            }
            _authState.value = AuthState.FirestoreUserFound
        }
    }

    fun signOut() = authRepository.signOut()

    private fun validateEmailFields(email: String?): Boolean {
        if (email.isNullOrEmpty()) return _authState.run {
            value = AuthState.InvalidEmail("Email address is required!")
            false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return _authState.run {
            value = AuthState.InvalidEmail("Please input a valid email address!")
            false
        }
        return true
    }

    private fun validatePasswordFields(
        password: String?,
        confirmPassword: String?,
        checkLength: Boolean = false
    ): Boolean {
        if (password.isNullOrEmpty()) return _authState.run {
            value = AuthState.InvalidPassword("Password is required!")
            false
        }
        if (!checkLength) return true
        if (password.length <= 6) return _authState.run {
            value = AuthState.InvalidPassword("Password must at least be 7 characters long!")
            false
        }
        if (password != confirmPassword) return _authState.run {
            value = AuthState.InvalidConfirmPassword("Passwords do not match!")
            false
        }
        return true
    }

    private fun validateUsername(
        username: String?,
    ): Boolean {
        if (username.isNullOrEmpty())
            return _authState.run {
                value = AuthState.InvalidUsername("Username required!")
                false
            }
        return true
    }

    private fun validateWeight(weight: String?): Boolean {
        if (weight.isNullOrEmpty())
            return _authState.run {
                value = AuthState.InvalidWeight("Weight required!")
                false
            }
        if (weight.toDoubleOrNull() == null)
            return _authState.run {
                value = AuthState.InvalidWeight("Weight must be a number!")
                false
            }
        return true
    }

    private fun validateHeight(height: String?): Boolean {
        if (height.isNullOrEmpty())
            return _authState.run {
                value = AuthState.InvalidHeight("Height required!")
                false
            }
        if (height.toDoubleOrNull() == null)
            return _authState.run {
                value = AuthState.InvalidHeight("Height must be a number!")
                false
            }
        return true
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        object FireStoreUserNotFound : AuthState()
        object FirestoreUserFound : AuthState()
        class InvalidEmail(val message: String) : AuthState()
        class InvalidPassword(val message: String) : AuthState()
        class InvalidConfirmPassword(val message: String) : AuthState()
        class InvalidUsername(val message: String) : AuthState()
        class InvalidWeight(val message: String) : AuthState()
        class InvalidHeight(val message: String) : AuthState()
        class FireBaseFailure(val message: String? = null) : AuthState()
    }
}