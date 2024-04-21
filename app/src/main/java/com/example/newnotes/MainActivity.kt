package com.example.newnotes

import LoginScreen
import RegistrationScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newnotes.ForgetPassScreen
import com.example.newnotes.Home
import com.google.firebase.auth.FirebaseAuth

import com.example.newnotes.ui.theme.NewNotesTheme

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewNotesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Check if there is a logged-in user
                    val auth = FirebaseAuth.getInstance()
                    val currentUser = auth.currentUser
                    val userId = auth.currentUser?.uid ?: ""
                    val startDestination = if (currentUser != null) "home" else "signin"

                    NavHost(navController, startDestination = startDestination) {
                        composable("signup") { RegistrationScreen(navController) }
                        composable("signin") { LoginScreen(navController) }
                        composable("home") { Home(navController) }
                        composable("forgotpass") { ForgetPassScreen(navController) }
                        composable("addNote") {
                            NoteEditor {
                                // Save note to Firestore
                                addNote(userId, it) // Replace userId with actual user ID
                                navController.popBackStack()
                            }
                        }
                    }



                }
            }
        }
    }

    override fun onBackPressed() {
        // Check if the current destination is the Home screen
        val currentDestination = navController.currentBackStackEntry?.destination?.route
        if (currentDestination == "home") {
            // If on the Home screen, close the app
            finish()
        } else {
            // Otherwise, navigate back as usual
            super.onBackPressed()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewNotesTheme {

    }
}