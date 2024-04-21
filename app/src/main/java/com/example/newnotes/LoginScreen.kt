import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun LoginScreen(navController: NavHostController) {
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
            Text(text = "Login User", fontSize = 40.sp)
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

            Spacer(modifier = Modifier.height(0.dp))
            if (isLoading1) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onClick = {
                        if (email1.value.isNotEmpty() && pass1.value.isNotEmpty()) {
                            isLoading1 = true
                           signInUser(email1.value, pass1.value, navController,snackbarHostState1) {
                                isLoading1 = false
                            }
                        }
                    },
                    shape = RectangleShape // Set button shape to RectangleShape
                ) {
                    Text(text = "SignIn")
                }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Forgot Password",color=Color.Red, fontWeight = FontWeight.W500,
                modifier=Modifier.clickable {  navController.navigate("forgotpass")})
            }

           

            Spacer(modifier = Modifier.height(30.dp))
        Row(verticalAlignment = Alignment.CenterVertically,modifier=Modifier.padding(bottom = 20.dp)) {
            Text(text = "Click here to ")
            Text(
                text = "Register!",
                modifier = Modifier.clickable { navController.navigate("signup") },
                color = Color.Red,
                fontSize = 15.sp
            )
        }

        SnackbarHost(snackbarHostState1)

        }

    }


fun signInUser(
    email: String,
    password: String,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onComplete: () -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val auth = FirebaseAuth.getInstance()
            val result = auth.signInWithEmailAndPassword(email, password).await()

            val user = result.user
            if (user != null) {
                if (user.isEmailVerified) {
                    // Email is verified, allow access to restricted features
                    snackbarHostState.showSnackbar("Login successful!")
                    navController.navigate("home") // Navigate to home screen upon successful login
                } else {
                    // Email is not verified, prompt user to verify their email
                    snackbarHostState.showSnackbar("Please verify your email before logging in.")
                    // Optionally, you can navigate to a screen to prompt verification
                    onComplete()
                }
            } else {
                // If user is null, authentication failed
                snackbarHostState.showSnackbar("Authentication failed")
            }
        } catch (e: FirebaseAuthException) {
            snackbarHostState.showSnackbar("Authentication failed: ${e.message}")
        } finally {
            onComplete() // Callback to stop loading indicator
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview1() {
    LoginScreen(rememberNavController())
}
