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

    fun logIn(email: String?, password: String?) {
        if (!validateLoginFields(email, password)) return
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

    fun signUp(email: String?, password: String?, confirmPassword: String?) {
        if (!validateSignUpFields(email, password, confirmPassword)) return
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

    fun sendPasswordResetLinkToEmail(email: String?) {
        _authState.postValue(AuthState.Loading)
        if (!validateForgetPasswordFields(email)) return
        FirebaseAuth.getInstance().sendPasswordResetEmail(email!!)
        _authState.postValue(AuthState.Success)
    }

    private fun validateForgetPasswordFields(email: String?): Boolean {
        if (email.isNullOrEmpty()) return _authState.run {
            postValue(AuthState.InvalidEmail("Email address is required!"))
            false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return _authState.run {
            postValue(AuthState.InvalidEmail("Please input a valid email address!"))
            false
        }
        return true
    }

    private fun validateLoginFields(email: String?, password: String?): Boolean {
        if (!validateForgetPasswordFields(email)) return false
        if (password.isNullOrEmpty()) return _authState.run {
            postValue(AuthState.InvalidPassword("Password is required!"))
            false
        }
        return true
    }

    private fun validateSignUpFields(
        email: String?,
        password: String?,
        confirmPassword: String?
    ): Boolean {
        if (!validateLoginFields(email, password)) return false
        if (password!!.length <= 6) return _authState.run {
            postValue(AuthState.InvalidPassword("Password length must be greater than 6 characters!"))
            false
        }
        if (password != confirmPassword) return _authState.run {
            postValue(AuthState.InvalidConfirmPassword("Passwords do not match!"))
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