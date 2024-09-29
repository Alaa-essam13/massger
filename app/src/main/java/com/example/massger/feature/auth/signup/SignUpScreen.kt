package com.example.massger.feature.auth.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.massger.R
import com.example.massger.feature.auth.signin.SignInState

@Composable
fun SignUpScreen(navController: NavHostController) {
    val viewModel: SignUpViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            is SignUpState.Success -> {
                navController.navigate("home")
            }

            is SignUpState.Error -> {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }
    Scaffold(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            if (uiState is SignUpState.Loading) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                        .alpha(.5f)
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)

                )
                OutlinedTextField(
                    value = userName, onValueChange = { userName = it },
                    label = {
                        Text(
                            text = "Full Name",
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = {
                        Text(
                            text = "Email",
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = {
                        Text(
                            text = "Password",
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it },
                    label = {
                        Text(
                            text = "Password",
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = password != confirm && password.isNotBlank() && confirm.isNotBlank()
                )
                Spacer(modifier = Modifier.size(25.dp))
                Button(
                    onClick = { viewModel.signUp(userName, email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = userName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirm.isNotBlank() && password == confirm
                ) {
                    Text(text = "Login")
                }
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(text = "Already have an account? Sign In")
                }
            }
        }
    }
}