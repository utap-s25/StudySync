package com.example.studysync.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun ChatRoomScreen(navController: NavController, roomId: String) {
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Anon"
    val firestore = remember { FirebaseFirestore.getInstance() }
    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var roomName by remember { mutableStateOf("") }
    var inviteEmail by remember { mutableStateOf(TextFieldValue("")) }
    var removeEmail by remember { mutableStateOf(TextFieldValue("")) }
    var listener: ListenerRegistration? = null

    // Fetch room name on first load
    LaunchedEffect(roomId) {
        firestore.collection("chatRooms").document(roomId)
            .get().addOnSuccessListener { doc ->
                roomName = doc.getString("name") ?: "Room"
            }
    }

    DisposableEffect(roomId) {
        listener = firestore.collection("chatRooms").document(roomId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { it.data }
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
        Text(roomName, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Invite input
        OutlinedTextField(
            value = inviteEmail,
            onValueChange = { inviteEmail = it },
            label = { Text("Invite member by email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val email = inviteEmail.text.trim()
            if (email.isNotEmpty()) {
                firestore.collection("chatRooms")
                    .document(roomId)
                    .update("members", FieldValue.arrayUnion(email))
                inviteEmail = TextFieldValue("")

                val systemMsg = mapOf(
                    "senderId" to "System",
                    "content" to "$userEmail added $email to the chat",
                    "timestamp" to System.currentTimeMillis(),
                    "type" to "system"
                )
                firestore.collection("chatRooms")
                    .document(roomId)
                    .collection("messages")
                    .add(systemMsg)
            }
        }) {
            Text("Add Member")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Remove member input
        OutlinedTextField(
            value = removeEmail,
            onValueChange = { removeEmail = it },
            label = { Text("Remove member by email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            val email = removeEmail.text.trim()
            if (email.isNotEmpty()) {
                firestore.collection("chatRooms")
                    .document(roomId)
                    .update("members", FieldValue.arrayRemove(email))
                removeEmail = TextFieldValue("")

                val systemMsg = mapOf(
                    "senderId" to "System",
                    "content" to "$userEmail removed $email from the chat",
                    "timestamp" to System.currentTimeMillis(),
                    "type" to "system"
                )
                firestore.collection("chatRooms")
                    .document(roomId)
                    .collection("messages")
                    .add(systemMsg)
            }
        }) {
            Text("Remove Member")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                val sender = message["senderId"]?.toString() ?: "Anon"
                val content = message["content"]?.toString() ?: ""
                val type = message["type"]?.toString()

                if (type == "system") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("$sender:", style = MaterialTheme.typography.labelSmall)
                        Text(content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val content = messageText.text.trim()
                if (content.isNotEmpty()) {
                    val message = mapOf(
                        "senderId" to userEmail,
                        "content" to content,
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("chatRooms")
                        .document(roomId)
                        .collection("messages")
                        .add(message)
                    messageText = TextFieldValue("")
                }
            }) {
                Text("Send")
            }
        }
    }
}
