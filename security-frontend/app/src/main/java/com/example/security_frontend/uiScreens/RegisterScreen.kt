package com.example.security_frontend.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.ui.theme.GoldAccent
import com.example.security_frontend.ui.theme.HintColor
import com.example.security_frontend.ui.theme.TextColor
import com.example.security_frontend.util.Resource
import com.example.security_frontend.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onCodeSent: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val sendCodeState by authViewModel.sendCodeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(sendCodeState) {
        when (val state = sendCodeState) {
            is Resource.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Verification code sent to your email!")
                }
                onCodeSent()
            }
            is Resource.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
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
            Text("CREATE ACCOUNT", style = MaterialTheme.typography.titleLarge, color = TextColor)
            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = HintColor,
                cursorColor = GoldAccent,
                focusedTextColor = TextColor,
                unfocusedTextColor = TextColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = GoldAccent,
                unfocusedLabelColor = HintColor,
            )

            OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name", color = HintColor) }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, leadingIcon = { Icon(Icons.Filled.Person, "", tint = GoldAccent) })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name", color = HintColor) }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, leadingIcon = { Icon(Icons.Filled.Person, "", tint = GoldAccent) })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email", color = HintColor) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, leadingIcon = { Icon(Icons.Filled.Email, "", tint = GoldAccent) })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(password, { password = it }, label = { Text("Password", color = HintColor) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = textFieldColors, leadingIcon = { Icon(Icons.Filled.Lock, "", tint = GoldAccent) })

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() -> coroutineScope.launch { snackbarHostState.showSnackbar("All fields are required.") }
                        !isValidEmail(email) -> coroutineScope.launch { snackbarHostState.showSnackbar("Invalid email format.") }
                        password.length < 6 -> coroutineScope.launch { snackbarHostState.showSnackbar("Password must be at least 6 characters.") }
                        else -> {
                            authViewModel.sendVerificationCode(
                                RegisterRequest(email, password, firstName, lastName)
                            )
                        }
                    }
                },
                enabled = sendCodeState !is Resource.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("GET VERIFICATION CODE", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Login", color = GoldAccent)
            }

            if (sendCodeState is Resource.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = GoldAccent)
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    return pattern.matcher(email).matches()
}
