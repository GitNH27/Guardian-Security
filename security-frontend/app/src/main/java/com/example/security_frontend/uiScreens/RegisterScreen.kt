package com.example.security_frontend.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.utils.Resource
import com.example.security_frontend.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val registerState by authViewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Registration successful!")
                }
                onRegisterSuccess()
            }
            is Resource.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        (registerState as Resource.Error).message
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Register", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") })
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("All fields are required.")
                            }
                        }
                        !isValidEmail(email) -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Invalid email format.")
                            }
                        }
                        password.length < 6 -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Password must be at least 6 characters.")
                            }
                        }
                        else -> {
                            authViewModel.register(
                                RegisterRequest(
                                    email = email,
                                    password = password,
                                    firstName = firstName,
                                    lastName = lastName
                                )
                            )
                        }
                    }
                },
                enabled = registerState !is Resource.Loading
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Login")
            }

            if (registerState is Resource.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    return pattern.matcher(email).matches()
}
