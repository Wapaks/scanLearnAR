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
        role: String,
        section: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (role == "teacher" && !email.endsWith("@neu.edu.ph")) {
            onError("Teacher accounts must use a @neu.edu.ph email address.")
            return
        }

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
                    studentNumber = studentNumber,
                    role = role,
                    section = section
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
        cachedUser: User?,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run {
                    onError("Login failed. Please try again.")
                    return@addOnSuccessListener
                }
                val role = if (email.endsWith("@neu.edu.ph")) "teacher" else "student"
                val user = User(
                    id = uid,
                    name = cachedUser?.name ?: email.substringBefore("@"),
                    email = email,
                    studentNumber = cachedUser?.studentNumber ?: "",
                    role = role,
                    section = cachedUser?.section ?: ""
                )
                onSuccess(user)
            }
            .addOnFailureListener { e ->
                onError(friendlyError(e.message))
            }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun signOut() = auth.signOut()

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