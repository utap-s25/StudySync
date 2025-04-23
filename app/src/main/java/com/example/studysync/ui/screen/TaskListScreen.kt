package com.example.studysync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.padding

@Composable
fun TaskListScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = remember { FirebaseFirestore.getInstance() }
    var tasks by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
    var listener: ListenerRegistration? = null

    DisposableEffect(Unit) {
        listener = firestore.collection("users").document(uid).collection("tasks")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    tasks = snapshot.documents.mapNotNull {
                        val data = it.data
                        if (data != null) it.id to data else null
                    }
                }
            }
        onDispose { listener?.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)
    ) {
        Text("Your Tasks", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("createTask") }) {
            Text("+ Add New Task")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(tasks, key = { it.first }) { (taskId, task) ->
                val title = task["title"]?.toString() ?: "(No Title)"
                val dueDate = task["dueDate"]?.toString() ?: "N/A"
                val completed = task["completed"] as? Boolean ?: false
                var offsetX by remember { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(offsetX.toInt(), 0) }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consumePositionChange()
                                if (dragAmount < 0) {
                                    offsetX += dragAmount
                                    if (offsetX < -200f) {
                                        firestore.collection("users").document(uid)
                                            .collection("tasks").document(taskId).delete()
                                        offsetX = 0f
                                    }
                                }
                            }
                        }
                        .clickable { navController.navigate("editTask/$taskId") }
                ) {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    textDecoration = if (completed) TextDecoration.LineThrough else null
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Due: $dueDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = if (completed) TextDecoration.LineThrough else null
                                )
                            }
                            Checkbox(
                                checked = completed,
                                onCheckedChange = { isChecked ->
                                    firestore.collection("users").document(uid)
                                        .collection("tasks").document(taskId)
                                        .update("completed", isChecked)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
