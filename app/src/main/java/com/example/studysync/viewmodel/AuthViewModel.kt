package com.example.studysync.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess() else onError(task.exception?.message ?: "Login failed")
            }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccess() else onError(task.exception?.message ?: "Registration failed")
            }
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}