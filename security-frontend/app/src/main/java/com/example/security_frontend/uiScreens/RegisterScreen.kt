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
import com.example.security_frontend.viewmodel.AuthViewModel
import com.example.security_frontend.utils.Resource

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(firstName, { firstName = it }, label = { Text("First Name") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(lastName, { lastName = it }, label = { Text("Last Name") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(password, { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )
            )
        }, enabled = registerState !is Resource.Loading) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (registerState) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> LaunchedEffect(Unit) { onRegisterSuccess() }
            is Resource.Error -> Text((registerState as Resource.Error).message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}
