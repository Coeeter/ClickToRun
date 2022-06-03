package com.example.clicktorun.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState>
        get() = _authState

    var email: String? = null
    var password: String? = null
    var confirmPassword: String? = null

    fun logIn() {
        val emailCheck = !validateEmailFields(email)
        val passwordCheck = !validatePasswordFields(password, null)
        if (emailCheck || passwordCheck) return
        _authState.postValue(AuthState.Loading)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    return@addOnCompleteListener _authState.postValue(AuthState.Success)
                task.exception?.run {
                    _authState.postValue(AuthState.Failure(message))
                }
            }
    }

    fun signUp() {
        val emailCheck = !validateEmailFields(email)
        val passwordCheck = !validatePasswordFields(password, confirmPassword, true)
        if (emailCheck || passwordCheck) return
        _authState.postValue(AuthState.Loading)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    return@addOnCompleteListener _authState.postValue(AuthState.Success)
                task.exception?.run {
                    _authState.postValue(AuthState.Failure(message))
                }
            }
    }

    fun sendPasswordResetLinkToEmail() {
        if (!validateEmailFields(email)) return
        _authState.postValue(AuthState.Loading)
        FirebaseAuth.getInstance().sendPasswordResetEmail(email!!)
        _authState.postValue(AuthState.Success)
    }

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

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        class Failure(val message: String?) : AuthState()
        class InvalidPassword(val message: String) : AuthState()
        class InvalidEmail(val message: String) : AuthState()
        class InvalidConfirmPassword(val message: String) : AuthState()
    }
}