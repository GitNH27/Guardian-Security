package com.example.security_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.security_frontend.jwtData.SessionManager
import com.example.security_frontend.repository.AuthRepository
import com.example.security_frontend.ui.theme.SecurityfrontendTheme
import com.example.security_frontend.uiScreens.HomeScreen
import com.example.security_frontend.uiScreens.LoginScreen
import com.example.security_frontend.uiScreens.RegisterScreen
import com.example.security_frontend.viewmodel.AuthViewModel
import com.example.security_frontend.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Dependency Injection Setup ---
        // In a real app, you would use a library like Hilt for this.
        val sessionManager = SessionManager(applicationContext)
        val apiService = RetrofitInstance.api
        val authRepository = AuthRepository(apiService)
        val authViewModelFactory = AuthViewModelFactory(authRepository, sessionManager)

        setContent {
            SecurityfrontendTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

                val token by sessionManager.token.collectAsState(initial = null)
                val startDestination = if (token != null) "home" else "login"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("home") { // Navigate to home
                                    popUpTo("login") { inclusive = true } // Clear login from back stack
                                }
                            },
                            onRegisterClick = { navController.navigate("register") } // Navigate to register
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            authViewModel = authViewModel,
                            onRegisterSuccess = {
                                navController.navigate("login") { // Go to login after successful registration
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onBackToLogin = { navController.navigateUp() } // Go back to the previous screen
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
