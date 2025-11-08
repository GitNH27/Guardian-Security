package com.example.security_frontend.uiScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Keep this as Material3
import androidx.compose.material3.OutlinedTextFieldDefaults // <-- NEW IMPORT REQUIRED
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security_frontend.R
import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.ui.theme.GoldAccent
import com.example.security_frontend.ui.theme.TextColor
import com.example.security_frontend.ui.theme.HintColor
import com.example.security_frontend.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

import com.example.security_frontend.util.Resource

// Import Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock

@OptIn(ExperimentalMaterial3Api::class) // Still good to keep this for now, though OutlinedTextField is stable
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by authViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Login successful!")
                }
                onLoginSuccess()
            }
            is Resource.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        (loginState as Resource.Error).message ?: "An unknown error occurred."
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.guardian_security_logo),
                contentDescription = "Guardian Security Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "LOG IN",
                style = MaterialTheme.typography.titleLarge,
                color = TextColor
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = HintColor) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                // --- CORRECTED MATERIAL 3 COLORS ---
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = HintColor,
                    cursorColor = GoldAccent,
                    focusedTextColor = TextColor,      // Text color when focused
                    unfocusedTextColor = TextColor,    // Text color when unfocused
                    focusedContainerColor = Color.Transparent, // Corrected parameter name
                    unfocusedContainerColor = Color.Transparent, // Corrected parameter name
                    disabledContainerColor = Color.Transparent, // Corrected parameter name if you have disabled state
                    errorContainerColor = Color.Transparent, // Corrected parameter name for error state
                    focusedLabelColor = GoldAccent,     // Label color when focused
                    unfocusedLabelColor = HintColor,    // Label color when unfocused
                    focusedLeadingIconColor = GoldAccent,
                    unfocusedLeadingIconColor = HintColor,
                ),
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email Icon", tint = GoldAccent) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = HintColor) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                // --- CORRECTED MATERIAL 3 COLORS ---
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = HintColor,
                    cursorColor = GoldAccent,
                    focusedTextColor = TextColor,
                    unfocusedTextColor = TextColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = HintColor,
                    focusedLeadingIconColor = GoldAccent,
                    unfocusedLeadingIconColor = HintColor,
                ),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock Icon", tint = GoldAccent) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        email.isBlank() || password.isBlank() -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Email and password are required.")
                            }
                        }
                        else -> {
                            authViewModel.login(LoginRequest(email, password))
                        }
                    }
                },
                enabled = loginState !is Resource.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "LOG IN",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ",
                    color = TextColor.copy(alpha = 0.7f)
                )
                TextButton(onClick = onRegisterClick) {
                    Text(
                        "Register Now",
                        color = GoldAccent,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (loginState is Resource.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = GoldAccent)
            }
        }
    }
}