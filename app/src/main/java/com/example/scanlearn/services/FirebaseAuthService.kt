package com.example.scanlearn.services

import com.example.scanlearn.models.User
import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthService {

    private val auth = FirebaseAuth.getInstance()

    fun register(
        name: String,
        email: String,
        studentNumber: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run {
                    onError("Registration failed. Please try again.")
                    return@addOnSuccessListener
                }
                val user = User(
                    id = uid,
                    name = name,
                    email = email,
                    studentNumber = studentNumber
                )
                onSuccess(user)
            }
            .addOnFailureListener { e ->
                onError(friendlyError(e.message))
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run {
                    onError("Login failed. Please try again.")
                    return@addOnSuccessListener
                }
                val user = User(
                    id = uid,
                    name = email.substringBefore("@"),
                    email = email,
                    studentNumber = ""
                )
                onSuccess(user)
            }
            .addOnFailureListener { e ->
                onError(friendlyError(e.message))
            }
    }

    /**
     * Returns the currently signed-in Firebase user UID, or null if not logged in.
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Signs out the current user from Firebase Auth.
     */
    fun signOut() = auth.signOut()

    /**
     * Converts Firebase error messages to user-friendly strings.
     */
    private fun friendlyError(message: String?): String {
        return when {
            message == null -> "Something went wrong. Please try again."
            message.contains("email address is already in use") ->
                "This email is already registered. Please sign in."
            message.contains("no user record") ||
                    message.contains("password is invalid") ||
                    message.contains("INVALID_LOGIN_CREDENTIALS") ->
                "Incorrect email or password."
            message.contains("badly formatted") ->
                "Please enter a valid email address."
            message.contains("password should be at least") ->
                "Password must be at least 6 characters."
            message.contains("network") ->
                "No internet connection. Please check your network."
            else -> "Something went wrong. Please try again."
        }
    }
}