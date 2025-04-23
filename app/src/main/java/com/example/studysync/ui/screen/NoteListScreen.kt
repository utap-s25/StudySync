package com.example.studysync.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.padding

@Composable
fun NoteListScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = remember { FirebaseFirestore.getInstance() }
    var notes by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
    var listener: ListenerRegistration? = null

    DisposableEffect(Unit) {
        listener = firestore.collection("users").document(uid).collection("notes")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    notes = snapshot.documents.mapNotNull {
                        val data = it.data
                        if (data != null) it.id to data else null
                    }
                }
            }
        onDispose { listener?.remove() }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(WindowInsets.systemBars.asPaddingValues())
        .padding(16.dp)) {
        Text("Your Notes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("editNote/new") }) {
            Text("+ Add New Note")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(notes, key = { it.first }) { (noteId, note) ->
                val title = note["title"]?.toString() ?: "(Untitled)"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { navController.navigate("editNote/$noteId") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}