package com.example.studysync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditTaskScreen(navController: NavController, taskId: String) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(taskId) {
        if (uid != null) {
            firestore.collection("users").document(uid).collection("tasks").document(taskId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        title = document.getString("title") ?: ""
                        dueDate = document.getString("dueDate") ?: ""
                    }
                    loading = false
                }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Edit Task", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (uid != null) {
                    firestore.collection("users").document(uid).collection("tasks").document(taskId)
                        .update(mapOf("title" to title, "dueDate" to dueDate))
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                }
            }) {
                Text("Save Changes")
            }
        }
    }
}