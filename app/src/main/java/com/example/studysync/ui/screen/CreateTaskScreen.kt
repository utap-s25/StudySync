package com.example.studysync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CreateTaskScreen(navController: NavController) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Task", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (title.isNotBlank() && dueDate.isNotBlank() && uid != null) {
                val task = hashMapOf(
                    "title" to title,
                    "dueDate" to dueDate
                )
                firestore.collection("users").document(uid).collection("tasks")
                    .add(task)
                    .addOnSuccessListener {
                        navController.navigate("tasks") { popUpTo("createTask") { inclusive = true } }
                    }
                    .addOnFailureListener {
                        error = it.message
                    }
            } else {
                error = "Title and due date are required"
            }
        }) {
            Text("Save Task")
        }
        Spacer(modifier = Modifier.height(8.dp))
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}