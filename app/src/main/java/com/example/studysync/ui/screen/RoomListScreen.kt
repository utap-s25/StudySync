package com.example.studysync.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import androidx.compose.foundation.clickable

@Composable
fun RoomListScreen(navController: NavController) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var roomName by remember { mutableStateOf(TextFieldValue("")) }
    var memberEmails by remember { mutableStateOf(TextFieldValue("")) }
    var rooms by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val listener = firestore.collection("chatRooms")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    rooms = snapshot.documents.mapNotNull {
                        val name = it.getString("name") ?: "Unnamed Room"
                        val members = it.get("members") as? List<*> ?: emptyList<String>()
                        if (currentUserEmail in members) it.id to name else null
                    }
                }
            }
        onDispose { listener.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)
    ) {
        Text("Study Rooms", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = roomName,
            onValueChange = { roomName = it },
            placeholder = { Text("Room Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = memberEmails,
            onValueChange = { memberEmails = it },
            placeholder = { Text("Invite emails (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val name = roomName.text.trim()
                if (name.isNotEmpty() && currentUserEmail.isNotEmpty()) {
                    val membersList = memberEmails.text
                        .split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toMutableList()
                        .apply { add(currentUserEmail) } // include creator

                    val room = hashMapOf(
                        "name" to name,
                        "createdBy" to currentUserEmail,
                        "members" to membersList
                    )
                    firestore.collection("chatRooms").add(room)
                    roomName = TextFieldValue("")
                    memberEmails = TextFieldValue("")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Create New Room")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(rooms, key = { it.first }) { (roomId, name) ->
                val offsetX = remember { Animatable(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount < 0) {
                                    coroutineScope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount)
                                        if (offsetX.value < -300) {
                                            firestore.collection("chatRooms").document(roomId).delete()
                                        } else {
                                            offsetX.animateTo(0f, tween(300))
                                        }
                                    }
                                }
                            }
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("chat/$roomId")
                            }
                    ) {
                        Text(
                            text = name,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
