package com.example.security_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.security_frontend.uiScreens.VerificationScreen
import com.example.security_frontend.viewmodel.AuthViewModel
import com.example.security_frontend.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(applicationContext)
        val apiService = RetrofitInstance.api
        val authRepository = AuthRepository(apiService)
        val authViewModelFactory = AuthViewModelFactory(authRepository, sessionManager)

        setContent {
            SecurityfrontendTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

                    val token by sessionManager.token.collectAsState(initial = null)
                    val startDestination = if (token != null && token!!.isNotBlank()) "home" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            // This ensures states are cleared ONLY when the screen is first composed
                            LaunchedEffect(Unit) {
                                authViewModel.clearStates()
                            }
                            LoginScreen(
                                authViewModel = authViewModel,
                                onLoginSuccess = {
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                },
                                onRegisterClick = { navController.navigate("register") }
                            )
                        }

                        composable("register") {
                            // This ensures states are cleared ONLY when the screen is first composed
                            LaunchedEffect(Unit) {
                                authViewModel.clearStates()
                            }
                            RegisterScreen(
                                authViewModel = authViewModel,
                                onCodeSent = { navController.navigate("verification") },
                                onBackToLogin = { navController.navigateUp() }
                            )
                        }

                        composable("verification") {
                            VerificationScreen(
                                authViewModel = authViewModel,
                                onVerificationSuccess = {
                                    navController.navigate("login") { popUpTo("login") { inclusive = true } }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") { popUpTo("home") { inclusive = true } }
                            })
                        }
                    }
                }
            }
        }
    }
}
