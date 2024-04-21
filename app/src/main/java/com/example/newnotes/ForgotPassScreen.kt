package com.example.newnotes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ForgetPassScreen(navController: NavHostController) {
    val emailState = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier =Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Forget Password", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(50.dp))

            TextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                label = { Text("Email") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val email = emailState.value
                    if (email.isNotBlank()) {
                        isLoading = true
                        // Call function to send password reset email
                        checkAndSendPasswordResetEmail(email, snackbarHostState,navController) {
                            isLoading = false
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Please enter your email")
                        }
                    }
                },
                shape = RectangleShape
            ) {
                Text(text = "Reset Password")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Go back to ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "Login Screen",
                modifier = Modifier.clickable { navController.navigate("signin") },
                color = Color.Red,
                fontSize = 14.sp
            )


        }
        SnackbarHost(
            snackbarHostState,

            )
    }
}


private fun checkAndSendPasswordResetEmail(
    email: String,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    onComplete: () -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(email)
            val userDocSnapshot = userDoc.get().await()
            if (userDocSnapshot.exists()) {
                // User exists, send password reset email
                sendPasswordResetEmail(email, snackbarHostState,navController) {
                    onComplete()
                }
            } else {
                // User does not exist
                snackbarHostState.showSnackbar("Email not found in database")
                onComplete()
                navController.navigate("signin")

            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                snackbarHostState.showSnackbar("Error: ${e.message}")
            }
            onComplete()
        }
    }
}

private fun sendPasswordResetEmail(
    email: String,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    onComplete: () -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Password reset email sent to $email")
                            navController.navigate("signin")
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Failed to send password reset email: ${task.exception?.message}")
                        }

                    }
                    onComplete()
                }
        } catch (e: FirebaseAuthException) {
            snackbarHostState.showSnackbar("Authentication failed: ${e.message}")
            onComplete()
        }
    }
}




@Preview(showBackground = true)
@Composable
fun pre() {
    ForgetPassScreen(navController = rememberNavController())
}
