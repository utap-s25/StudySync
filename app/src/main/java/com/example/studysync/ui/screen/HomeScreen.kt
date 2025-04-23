package com.example.studysync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studysync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
@Composable
fun HomeScreen(navController: NavController) {
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "User"
    val firestore = remember { FirebaseFirestore.getInstance() }
    var tasks by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var notes by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var rooms by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .collection("tasks")
            .get()
            .addOnSuccessListener { snapshot ->
                tasks = snapshot.documents.mapNotNull { it.data }
            }

        firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .collection("notes")
            .orderBy("lastEdited", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                notes = snapshot.documents.mapNotNull { it.data }
            }

        firestore.collection("chatRooms")
            .whereArrayContains("members", userEmail)
            .limit(2)
            .get()
            .addOnSuccessListener { snapshot ->
                rooms = snapshot.documents.mapNotNull { it.data }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_studysync),
            contentDescription = "StudySync Logo",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light)
            )
            Text(
                text = userEmail,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                HomeSectionCard(
                    iconRes = R.drawable.ic_check,
                    title = "Tasks",
                    subtitle = "You have ${tasks.size} tasks",
                    buttonText = "Go to Task List",
                    onClick = { navController.navigate("tasks") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                HomeSectionCard(
                    iconRes = R.drawable.ic_notes,
                    title = "Notes",
                    subtitle = "Last edited: ${notes.firstOrNull()?.get("title") ?: "None"}",
                    buttonText = "View Notes",
                    onClick = { navController.navigate("notes") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                HomeSectionCard(
                    iconRes = R.drawable.ic_chat,
                    title = "Chat Rooms",
                    subtitle = "You're in ${rooms.size} rooms",
                    buttonText = "Open Chat Rooms",
                    onClick = { navController.navigate("chatRooms") }
                )
            }
        }
    }
}

@Composable
fun HomeSectionCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Fit
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}
