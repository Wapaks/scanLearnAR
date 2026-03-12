package com.example.scanlearn.services

import android.content.Context
import android.content.SharedPreferences
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.ScannedObject
import com.example.scanlearn.models.Submission
import com.example.scanlearn.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.scanlearn.models.*

class StorageService(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("scan_learn_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ─── User ───────────────────────────────────────────────
    fun saveUser(user: User) {
        prefs.edit().putString("current_user", gson.toJson(user)).apply()
    }

    fun getUser(): User? {
        val json = prefs.getString("current_user", null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    fun clearUser() {
        prefs.edit().remove("current_user").apply()
    }

    // ─── Registered Users ───────────────────────────────────
    fun saveRegisteredUser(user: User, password: String) {
        val users = getRegisteredUsers().toMutableMap()
        users[user.email] = Pair(user, password)
        prefs.edit().putString("registered_users", gson.toJson(users)).apply()
    }

    fun findUserByEmailOrStudentNumber(identifier: String, password: String): User? {
        // In demo mode, create a user on the fly for any credentials
        val existing = getUser()
        if (existing != null) return existing
        return User(
            id = System.currentTimeMillis().toString(),
            name = "Demo Student",
            email = identifier,
            studentNumber = identifier
        )
    }

    private fun getRegisteredUsers(): Map<String, Any> {
        val json = prefs.getString("registered_users", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(json, type)
    }

    // ─── Submissions ─────────────────────────────────────────
    fun saveSubmission(submission: Submission) {
        val list = getSubmissions().toMutableList()
        list.add(submission)
        prefs.edit().putString("submissions", gson.toJson(list)).apply()
    }

    fun getSubmissions(): List<Submission> {
        val json = prefs.getString("submissions", null) ?: return emptyList()
        val type = object : TypeToken<List<Submission>>() {}.type
        return gson.fromJson(json, type)
    }

    // ─── Scanned Objects ─────────────────────────────────────
    fun saveScannedObject(scannedObject: ScannedObject) {
        val list = getScannedObjects().toMutableList()
        list.add(scannedObject)
        prefs.edit().putString("scanned_objects", gson.toJson(list)).apply()
    }

    fun getScannedObjects(): List<ScannedObject> {
        val json = prefs.getString("scanned_objects", null) ?: return emptyList()
        val type = object : TypeToken<List<ScannedObject>>() {}.type
        return gson.fromJson(json, type)
    }

    // ─── Missions ────────────────────────────────────────────
    fun saveMissions(missions: List<Mission>) {
        prefs.edit().putString("missions", gson.toJson(missions)).apply()
    }

    fun getMissions(): List<Mission> {
        val json = prefs.getString("missions", null) ?: return emptyList()
        val type = object : TypeToken<List<Mission>>() {}.type
        return gson.fromJson(json, type)
    }
}
