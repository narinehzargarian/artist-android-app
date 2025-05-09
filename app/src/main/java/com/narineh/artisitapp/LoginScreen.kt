package com.narineh.artisitapp

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.narineh.artisitapp.network.LoginRequest
import com.narineh.artisitapp.network.RetrofitClient
import com.narineh.artisitapp.network.UserData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (UserData) -> Unit,
    navController: NavController
) {
    val snackbarState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isInProgress by remember { mutableStateOf(false) }
    var serverError by remember { mutableStateOf<String?>(null) }
    var validation by remember { mutableStateOf(ValidationResult(false, false, false)) }

    var emailTouched by remember { mutableStateOf(false) }
    var emailSubmitted by remember { mutableStateOf(false) }
    var passTouched by remember { mutableStateOf(false) }

    validation.emailBlankError = email.isBlank() && emailTouched
    validation.emailFormatError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            && emailSubmitted
            && email.isNotBlank()
    validation.passError = password.isBlank()

    val emailError = validation.emailBlankError || validation.emailFormatError
    val scope = rememberCoroutineScope()

    Scaffold (
        snackbarHost = { SnackbarHost(hostState = snackbarState) },
        topBar = {
            TopAppBar(
                title = {Text("Login")},
                navigationIcon = {
                    IconButton(
                        onClick = {navController.popBackStack()}
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Screen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer))
        },
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(12.dp)
            .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                isError = emailError,
                supportingText = {
                    when {
                        validation.emailBlankError ->
                            Text("Email cannot be empty", color = MaterialTheme.colorScheme.error)
                        validation.emailFormatError ->
                            Text("Invalid email format", color = MaterialTheme.colorScheme.error)
                    }
                },
                maxLines = 1,
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged{ state ->
                        if (state.isFocused) emailTouched = true
                    }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                isError = passTouched && validation.passError,
                supportingText = {
                    if (passTouched && validation.passError) {
                        Text(text  = "Password cannot be empty", color = MaterialTheme.colorScheme.error)
                    }
                },
                maxLines = 1,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged{ state ->
                        if (state.isFocused) passTouched = true
                    }
            )
            Button(
                onClick = {
                    emailSubmitted = true
                    validation.emailFormatError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()

                    // Check the format of the email
                    if (validation.emailFormatError) {
                        // Clear error messages
                        serverError = null
                        return@Button
                    }
                    isInProgress = true
                    serverError = null
                    scope.launch {
                        Log.d("LoginScreen", "Logging in...")
                        val request = LoginRequest(email, password)
                        val result = RetrofitClient.authService.login(request)
                        Log.d("LoginScreen", "Login result: ${result.body()}")

                        isInProgress = false
                        if (result.isSuccessful) {
                            // Show snackbar
                            snackbarState.showSnackbar(
                                "Logged in successfully",
                                duration = SnackbarDuration.Short)
                            // Navigate to home
                            val user = result.body()!!.user
                            onLogin(user)
                        }
                        else {
                            serverError = "Username or password is incorrect"
                            Log.e("LoginScreen", "Login failed: ${result.errorBody()?.string()}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = !isInProgress
                        && !emailError
                        && !validation.passError,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if(isInProgress) { // Progress circle
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp)
                }
                else {
                    Text("Login")
                }
            }
            if (serverError != null) {
                Text(
                    text = serverError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                        .padding(vertical = 4.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account yet?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                )
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickable{ navController.navigate("register")}
                )
            }
        }
    }
}

data class ValidationResult (
    var emailBlankError: Boolean,
    var emailFormatError: Boolean,
    var passError: Boolean
)

//fun validateCredentials(email: String, password: String): ValidationResult {
//    // Validate email
//    val emailFormatError =  !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()
//    val emailBlankError = email.isBlank()
//    // Validate pass
//    val passError = password.isBlank()
//
//    return ValidationResult(emailBlankError, emailFormatError, passError)
//}