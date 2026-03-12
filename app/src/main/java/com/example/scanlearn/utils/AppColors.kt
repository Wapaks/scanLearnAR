package com.example.scanlearn.utils

import android.graphics.Color

object AppColors {
    val PRIMARY = Color.parseColor("#2196F3")
    val EXPLORER = Color.parseColor("#4CAF50")
    val MISSION = Color.parseColor("#FF9800")
    val CHALLENGE = Color.parseColor("#F44336")
    val SUCCESS = Color.parseColor("#4CAF50")
    val WARNING = Color.parseColor("#FFC107")
    val SURFACE = Color.parseColor("#FFFFFF")
    val BACKGROUND = Color.parseColor("#F5F5F5")
    val TEXT_PRIMARY = Color.parseColor("#212121")
    val TEXT_SECONDARY = Color.parseColor("#757575")

    fun getModeColor(mode: String): Int = when (mode) {
        "explorer" -> EXPLORER
        "mission" -> MISSION
        "challenge" -> CHALLENGE
        else -> PRIMARY
    }

    fun getModeLabel(mode: String): String = when (mode) {
        "explorer" -> "Explorer Mode"
        "mission" -> "Mission Mode"
        "challenge" -> "Challenge Mode"
        else -> "Scan & Learn"
    }
}