package com.example.massger.feature.auth.signin

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.massger.R

@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: SignInViewModel = hiltViewModel()
    val uistate by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(key1 = uistate) {
        when (uistate) {
            is SignInState.Success -> {
                navController.navigate("home")
            }
            is SignInState.Error -> {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(Modifier.fillMaxSize()) {
        Box (Modifier.fillMaxSize()) {
            if(uistate is SignInState.Loading) {
                Box(contentAlignment = Alignment.Center,modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
                    .alpha(.5f)) {
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
//                        .background(Color.White)
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
                Spacer(modifier = Modifier.size(25.dp))
                Button(
                    onClick = { viewModel.signIn(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank() && (uistate == SignInState.Noting || uistate == SignInState.Error)
                ) {
                    Text(text = "Login")
                }
                TextButton(onClick = { navController.navigate("signup") }) {
                    Text(text = "Do you have an account? Sign Up")
                }
            }
        }
    }
}


@Preview
@Composable
private fun loginprev() {
    LoginScreen(navController = rememberNavController())
}