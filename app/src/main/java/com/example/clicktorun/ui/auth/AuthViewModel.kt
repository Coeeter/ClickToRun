package com.example.clicktorun.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicktorun.data.models.User
import com.example.clicktorun.repositories.AuthRepository
import com.example.clicktorun.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
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

    fun logIn() {
        val emailCheck = !validateEmailFields(email)
        val passwordCheck = !validatePasswordFields(password, null)
        if (emailCheck || passwordCheck) return
        _authState.postValue(AuthState.Loading)
        authRepository.login(email!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    return@addOnCompleteListener _authState.postValue(AuthState.Success)
                task.exception?.run {
                    _authState.postValue(AuthState.FireBaseFailure(message))
                }
            }
    }

    fun signUp() {
        val emailCheck = !validateEmailFields(email)
        val passwordCheck = !validatePasswordFields(password, confirmPassword, true)
        if (emailCheck || passwordCheck) return
        _authState.postValue(AuthState.Loading)
        authRepository.signUp(email!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    return@addOnCompleteListener _authState.postValue(AuthState.Success)
                task.exception?.run {
                    _authState.postValue(AuthState.FireBaseFailure(message))
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
        val user = User(username!!, height!!.toDouble() / 100, weight!!.toDouble())
        _authState.value = (AuthState.Loading)
        viewModelScope.launch {
            if (userRepository.insertUser(user))
                return@launch _authState.postValue(AuthState.Success)
            _authState.postValue(AuthState.FireBaseFailure())
        }
    }

    suspend fun getCurrentUser() =
        listOf(authRepository.getAuthUser(), userRepository.getCurrentUser())

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
        class InvalidEmail(val message: String) : AuthState()
        class InvalidPassword(val message: String) : AuthState()
        class InvalidConfirmPassword(val message: String) : AuthState()
        class InvalidUsername(val message: String) : AuthState()
        class InvalidWeight(val message: String) : AuthState()
        class InvalidHeight(val message: String) : AuthState()
        class FireBaseFailure(val message: String? = null) : AuthState()
    }
}