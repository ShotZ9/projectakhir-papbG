package com.example.form

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.form.ui.theme.FormTheme

class MainActivity : ComponentActivity() {

    // Inisialisasi AuthViewModel menggunakan Factory
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        val authViewModel : AuthViewModel by viewModels()
        authViewModel = ViewModelProvider(this, AuthViewModelFactory(applicationContext))
            .get(AuthViewModel::class.java)
        setContent {
            FormTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Navigation(modifier = Modifier, authViewModel = authViewModel)
                }
            }
        }
    }
}