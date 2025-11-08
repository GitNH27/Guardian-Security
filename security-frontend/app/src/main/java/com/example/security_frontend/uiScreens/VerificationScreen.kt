package com.example.security_frontend.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.security_frontend.ui.theme.GoldAccent
import com.example.security_frontend.ui.theme.HintColor
import com.example.security_frontend.ui.theme.TextColor
import com.example.security_frontend.util.Resource
import com.example.security_frontend.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun VerificationScreen(
    authViewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val verificationState by authViewModel.verifyRegistrationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(verificationState) {
        when (val state = verificationState) {
            is Resource.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Registration successful! Please log in.")
                }
                onVerificationSuccess()
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
            Text(
                "VERIFY ACCOUNT",
                style = MaterialTheme.typography.titleLarge,
                color = TextColor
            )
            Text(
                "Enter the code sent to your email",
                style = MaterialTheme.typography.bodyMedium,
                color = HintColor,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Verification Code", color = HintColor) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = HintColor,
                    cursorColor = GoldAccent,
                    focusedTextColor = TextColor,
                    unfocusedTextColor = TextColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = HintColor,
                    focusedLeadingIconColor = GoldAccent,
                    unfocusedLeadingIconColor = HintColor,
                ),
                leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = "Code Icon", tint = GoldAccent) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.verifyRegistration(code) },
                enabled = verificationState !is Resource.Loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "VERIFY & REGISTER",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (verificationState is Resource.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = GoldAccent)
            }
        }
    }
}