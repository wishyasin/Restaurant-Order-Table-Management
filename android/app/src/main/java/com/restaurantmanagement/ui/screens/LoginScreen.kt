package com.restaurantmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.restaurantmanagement.navigation.Screen
import com.restaurantmanagement.data.remote.RetrofitClient
import com.restaurantmanagement.data.remote.LoginRequest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Restaurant Management",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Sign in to your account",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(value = username,
            onValueChange = {
                username = it
                errorMessage = ""
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (username.isBlank() || password.isBlank()) {
                errorMessage = "Please fill all fields"
                return@Button
            }

            isLoading = true
            scope.launch {
                try {
                    val response = RetrofitClient.apiService.login(LoginRequest(username,
                        password))
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {

                            navController.navigate("${Screen.Tables.route}/${user.role}") {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    } else {
                        errorMessage = "Invalid username or password"
                    }
                } catch (e: Exception) {
                    errorMessage = "Connection error: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
                .height(50.dp)) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White,
                    modifier = Modifier.size(24.dp))
            } else {
                Text("Sign In")
            }
        }
    }
}