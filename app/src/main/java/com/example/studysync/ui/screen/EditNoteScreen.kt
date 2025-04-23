package com.example.studysync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.padding

@Composable
fun EditNoteScreen(navController: NavController, noteId: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(noteId != "new") }

    LaunchedEffect(noteId) {
        if (noteId != "new" && uid != null) {
            firestore.collection("users").document(uid).collection("notes").document(noteId)
                .get().addOnSuccessListener { document ->
                    title = document.getString("title") ?: ""
                    content = document.getString("content") ?: ""
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)) {
            Text(text = if (noteId == "new") "New Note" else "Edit Note", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (uid != null) {
                    val noteData = mapOf(
                        "title" to title,
                        "content" to content,
                        "lastEdited" to System.currentTimeMillis()
                    )
                    if (noteId == "new") {
                        firestore.collection("users").document(uid).collection("notes")
                            .add(noteData)
                            .addOnSuccessListener { navController.popBackStack() }
                    } else {
                        firestore.collection("users").document(uid).collection("notes").document(noteId)
                            .set(noteData)
                            .addOnSuccessListener { navController.popBackStack() }
                    }
                }
            }) {
                Text("Save")
            }
        }
    }
}