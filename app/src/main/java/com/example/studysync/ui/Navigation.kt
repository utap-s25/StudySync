package com.example.studysync.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studysync.ui.screens.*

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("tasks") { TaskListScreen(navController) }
        composable("createTask") { CreateTaskScreen(navController) }
        composable("editTask/{taskId}", arguments = listOf(navArgument("taskId") { type = NavType.StringType })) {
                backStackEntry ->
            EditTaskScreen(navController, backStackEntry.arguments?.getString("taskId") ?: "")
        }
        composable("notes") { NoteListScreen(navController) }
        composable("editNote/{noteId}", arguments = listOf(navArgument("noteId") { type = NavType.StringType })) {
                backStackEntry ->
            EditNoteScreen(navController, backStackEntry.arguments?.getString("noteId") ?: "")
        }
        composable("chatRooms") { RoomListScreen(navController) }
        composable("chat/{roomId}", arguments = listOf(navArgument("roomId") { type = NavType.StringType })) { backStackEntry ->
            ChatRoomScreen(navController, backStackEntry.arguments?.getString("roomId") ?: "")
        }
    }
}
