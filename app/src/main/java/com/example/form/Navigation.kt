package com.example.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.form.pages.CameraPreview
import com.example.form.pages.HomePage
import com.example.form.pages.Login
import com.example.form.pages.SignUp

@Composable
fun Navigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            Login(modifier, navController, authViewModel)
        }
        composable("signup") {
            SignUp(modifier, navController, authViewModel)
        }
        composable("homepage") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("camera") {
            CameraScreen(authViewModel)
        }
    })
}