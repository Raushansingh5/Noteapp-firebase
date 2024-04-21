package com.example.newnotes


import androidx.compose.material3.*
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val notes = remember { mutableStateListOf<Note>() }

    val userEmail = ("Hi " + user?.email?.substringBefore('@')) ?: "No user logged in"

    var mDisplayMenu by remember { mutableStateOf(false) }
    LaunchedEffect(user) {
        user?.let { currentUser ->
            fetchUserNotes(currentUser.uid) { fetchedNotes ->
                notes.addAll(fetchedNotes)
            }
        }
    }



    Scaffold(
        topBar = {
            // TopAppBar with PopupMenu
            TopAppBar(
                title = {
                    Text( text = "My Screen",
                        style = TextStyle(fontSize = 25.sp))


                },
                actions = {

                    // Creating Icon button favorites, on click
                    // would create a Toast message

                    // Creating Icon button for dropdown menu
                    IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                        Icon(Icons.Default.MoreVert, "")
                    }

                    // Creating a dropdown menu
                    DropdownMenu(
                        expanded = mDisplayMenu,
                        onDismissRequest = { mDisplayMenu = false }
                    ) {

                        // Creating dropdown menu item, on click
                        // would create a Toast message
                        DropdownMenuItem(
                            text = {
                                Text("Logout")
                            },
                            onClick = { performLogout(navController) },
                        )
                        // Creating dropdown menu item, on click
                        // would create a Toast message
                        DropdownMenuItem(
                            text = {
                                Text("Refresh")
                            },
                            onClick = { /* TODO */ },
                        )
                    }

                }
            )
        },
        floatingActionButton = {
            // FloatingActionButton
            FloatingActionButton(
                onClick = {
                    // Handle FAB click
                    // For example, navigate to another screen
                    navController.navigate("addNote")
                },
                content = {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            )
        },
        content = {
            // Main content
            // Main content
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Display all notes
                notes.forEach { note ->
                    NoteItem(note = note)
                }
            }
        }
    )
}

@Composable
fun NoteItem(note: Note) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = note.title)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = note.content)
        }
    }
}

private fun performLogout(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    CoroutineScope(Dispatchers.IO).launch {
        auth.signOut()
        withContext(Dispatchers.Main) {
            navController.navigate("signin")
        }
    }
}



private const val TAG = "Firestore"

fun addNote(userId: String, note: Note) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId)
        .collection("notes")
        .add(note)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "Note added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding note", e)
        }
}

fun fetchUserNotes(userId: String, onNotesFetched: (List<Note>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId)
        .collection("notes")
        .get()
        .addOnSuccessListener { result ->
            val notesList = mutableListOf<Note>()
            for (document in result) {
                val note = document.toObject(Note::class.java)
                notesList.add(note)
            }
            onNotesFetched(notesList)
        }
        .addOnFailureListener { exception ->
            Log.w("Firestore", "Error getting documents: ", exception)
        }
}

@Preview(showBackground = true)
@Composable
fun preview1() {
    Home(rememberNavController())
}
