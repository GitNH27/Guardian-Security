package com.example.security_frontend.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.viewmodel.AuthViewModel
import com.example.security_frontend.utils.Resource

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit // callback to navigate to register screen
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by authViewModel.loginState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Login", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { authViewModel.login(LoginRequest(email, password)) }, enabled = loginState !is Resource.Loading) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (loginState) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> LaunchedEffect(Unit) { onLoginSuccess() }
            is Resource.Error -> Text((loginState as Resource.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}
