package com.example.form

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(private val context: Context) : ViewModel(){
//class AuthViewModel : ViewModel(){

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val SESSION_KEY = "session_key"
        private const val SESSION_DURATION = 60 * 60 * 1000 // 1 hour in milliseconds
    }

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus(){
        val sessionTime = sharedPreferences.getLong(SESSION_KEY, 0L)
        val currentTime = System.currentTimeMillis()

        // Check if the user is logged in and if the session has expired
        if (auth.currentUser == null || sessionTime == 0L || currentTime > sessionTime + SESSION_DURATION) {
            // Session expired or no user is logged in
            signout() // Force signout
            _authState.value = AuthState.UnAuthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
//        if (auth.currentUser == null) {
//            _authState.value = AuthState.UnAuthenticated
//        } else {
//            _authState.value = AuthState.Authenticated
//        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("ga boleh kosong")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val token = tokenTask.result?.token
                            if (token != null) {
                                _authState.value = AuthState.Authenticated

                                // Save the session time for internal expiration tracking
                                saveSessionTime()

                                // Save Firebase token with expiration (24 hours from now)
                                saveToken(token, System.currentTimeMillis() + (15 * 60 * 1000)) // Token expires in 24 hours
                            }
                        } else {
                            _authState.value = AuthState.Error("Failed to get token: ${tokenTask.exception?.message}")
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login Gagal")
                }
            }
    }

    fun signup(email: String, password: String, nama: String, nim: String) {
        if (email.isEmpty() || password.isEmpty() || nama.isEmpty() || nim.isEmpty()) {
            _authState.value = AuthState.Error("Semua kolom harus diisi.")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val token = tokenTask.result?.token
                            if (token != null) {
                                _authState.value = AuthState.Authenticated

                                // Save the session time for internal expiration tracking
                                saveSessionTime()

                                // Save Firebase token with expiration (24 hours from now)
                                saveToken(token, System.currentTimeMillis() + (15 * 60 * 1000)) // Token expires in 24 hours
                            }
                        } else {
                            _authState.value = AuthState.Error("Failed to get token: ${tokenTask.exception?.message}")
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign Up Gagal")
                }
            }
    }

    fun signout(){
        auth.signOut()
        clearSessionTime()
        _authState.value = AuthState.UnAuthenticated
    }

    private fun saveSessionTime() {
        val editor = sharedPreferences.edit()
        editor.putLong(SESSION_KEY, System.currentTimeMillis())
        editor.apply()
    }

    private fun clearSessionTime() {
        val editor = sharedPreferences.edit()
        editor.remove(SESSION_KEY)
        editor.apply()
    }
    // Save token and expiration time
    private fun saveToken(token: String, expiresAt: Long) {
        with(sharedPreferences.edit()) {
            putString("auth_token", token)
            putLong("expires_at", expiresAt)
            apply() // Make sure this is called to save the values
        }
    }

    // Retrieve the expiration time
    fun getExpirationTime(): Long {
        return sharedPreferences.getLong("expires_at", 0L) // Default is 0 if not set
    }

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value += bitmap
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object UnAuthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()

}