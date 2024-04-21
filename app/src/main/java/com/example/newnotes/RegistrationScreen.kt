import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults.shape
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

import kotlinx.coroutines.tasks.await

@Composable
fun RegistrationScreen(navController: NavHostController) {
    val email1 = remember { mutableStateOf("") }
    val pass1 = remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState1 = remember { SnackbarHostState() }
    var isLoading1 by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Register User", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(50.dp))

            TextField(
                value = email1.value,
                onValueChange = { email1.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                label = { Text("Email") }
            )

            val visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            TextField(
                value = pass1.value,
                onValueChange = { pass1.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                visualTransformation = visualTransformation,
                label = { Text("Password") },
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = "Toggle password visibility")
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            if (isLoading1) {
                CircularProgressIndicator() // Show progress indicator when loading
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onClick = {
                        if (email1.value.isNotEmpty() && pass1.value.isNotEmpty()) {
                            isLoading1 = true
                            registerUser(email1.value, pass1.value, snackbarHostState1, navController) {
                                isLoading1 = false
                            }
                        }
                        else {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState1.showSnackbar("Enter all Credentials")
                            }
                        }
                    },
                    shape = RectangleShape // Set button shape to RectangleShape
                ) {
                    Text(text = "Signup")
                }
            }

            Text(text = "Forgot Password",color=Color.Black, fontWeight = FontWeight.W500,
                modifier=Modifier.clickable {  })

            Spacer(modifier = Modifier.height(30.dp))

        }
        Row(verticalAlignment = Alignment.CenterVertically,modifier=Modifier.padding(bottom = 20.dp)) {
            Text(text = "Click here to ")
            Text(
                text = "Login!",
                modifier = Modifier.clickable { navController.navigate("signin") },
                color = Color.Red,
                fontSize = 15.sp
            )
        }

        SnackbarHost(snackbarHostState1)
    }
}



fun registerUser(
    email: String,
    password: String,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    onComplete: () -> Unit
) {
    // Start a coroutine to handle asynchronous operations
    CoroutineScope(Dispatchers.Main).launch {
        var isUserCreated = false
        try {
            // Perform authentication operation
            // Assuming you have access to FirebaseAuth instance
            val auth = FirebaseAuth.getInstance()
            val result = auth.createUserWithEmailAndPassword(email, password).await()


            // Sign in success, update UI with the signed-in user's information
            val user = result.user
            user?.sendEmailVerification()?.await()
            isUserCreated = true

            snackbarHostState.showSnackbar("Registration successful!")

            // Navigate to home screen upon successful registration
            navController.navigate("signin")
        } catch (e: FirebaseAuthUserCollisionException) {
            snackbarHostState.showSnackbar("Email is already registered")
        } catch (e: FirebaseAuthException) {
            snackbarHostState.showSnackbar("Authentication failed: ${e.message}")
        } finally {
            if (!isUserCreated) {
                onComplete() // Callback to stop loading indicator
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
fun Preview() {
    RegistrationScreen(rememberNavController())
}
