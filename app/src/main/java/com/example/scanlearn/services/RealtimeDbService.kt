package com.example.scanlearn.services

import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.LearningObjectAnalytics
import com.example.scanlearn.models.CategoryAnalytics
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.models.ScanAttempt
import com.example.scanlearn.models.ScannedObject
import com.example.scanlearn.models.StudentMissionProgress
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.models.Submission
import com.example.scanlearn.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RealtimeDbService {

    private val db = FirebaseDatabase.getInstance().reference

    fun saveUser(user: User, onComplete: (Boolean) -> Unit = {}) {
        db.child("users").child(user.id).setValue(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUser(uid: String, onResult: (User?) -> Unit) {
        db.child("users").child(uid).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(User::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getAllStudents(onResult: (List<User>) -> Unit) {
        db.child("users").orderByChild("role").equalTo("student")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(User::class.java) })
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveSubmission(uid: String, submission: Submission, onComplete: (Boolean) -> Unit = {}) {
        val key = db.child("submissions").child(uid).push().key ?: return
        db.child("submissions").child(uid).child(key).setValue(submission.copy(id = key))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getSubmissions(uid: String, onResult: (List<Submission>) -> Unit) {
        db.child("submissions").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(Submission::class.java) }.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getSubmissionsForAllStudents(onResult: (Map<String, List<Submission>>) -> Unit) {
        db.child("submissions")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val map = mutableMapOf<String, List<Submission>>()
                    snap.children.forEach { userSnap ->
                        val uid = userSnap.key ?: return@forEach
                        map[uid] = userSnap.children.mapNotNull { it.getValue(Submission::class.java) }
                    }
                    onResult(map)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyMap()) }
            })
    }

    fun saveQuizAttempt(uid: String, attempt: QuizAttempt, onComplete: (String?) -> Unit = {}) {
        val key = db.child("quiz_attempts").child(uid).push().key
        if (key == null) {
            onComplete(null)
            return
        }

        db.child("quiz_attempts").child(uid).child(key).setValue(attempt.copy(id = key))
            .addOnSuccessListener { onComplete(key) }
            .addOnFailureListener { onComplete(null) }
    }

    fun getQuizAttempts(uid: String, onResult: (List<QuizAttempt>) -> Unit) {
        db.child("quiz_attempts").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(QuizAttempt::class.java) }.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getQuizAttemptsForAllStudents(onResult: (Map<String, List<QuizAttempt>>) -> Unit) {
        db.child("quiz_attempts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val map = mutableMapOf<String, List<QuizAttempt>>()
                    snap.children.forEach { userSnap ->
                        val uid = userSnap.key ?: return@forEach
                        map[uid] = userSnap.children.mapNotNull { it.getValue(QuizAttempt::class.java) }
                    }
                    onResult(map)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyMap()) }
            })
    }

    fun getScanAttemptsForAllStudents(onResult: (Map<String, List<ScanAttempt>>) -> Unit) {
        db.child("scan_attempts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val map = mutableMapOf<String, List<ScanAttempt>>()
                    snap.children.forEach { userSnap ->
                        val uid = userSnap.key ?: return@forEach
                        map[uid] = userSnap.children.mapNotNull { it.getValue(ScanAttempt::class.java) }
                    }
                    onResult(map)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyMap()) }
            })
    }

    fun saveScannedObject(uid: String, scannedObject: ScannedObject, onComplete: (Boolean) -> Unit = {}) {
        val key = db.child("scanned").child(uid).push().key ?: return
        db.child("scanned").child(uid).child(key).setValue(scannedObject)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getScannedObjects(uid: String, onResult: (List<ScannedObject>) -> Unit) {
        db.child("scanned").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(ScannedObject::class.java) })
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getScannedCountForAllStudents(onResult: (Map<String, Int>) -> Unit) {
        db.child("scanned")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val map = mutableMapOf<String, Int>()
                    snap.children.forEach { userSnap ->
                        map[userSnap.key ?: return@forEach] = userSnap.childrenCount.toInt()
                    }
                    onResult(map)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyMap()) }
            })
    }

    fun getLearningObjects(onResult: (List<LearningObject>) -> Unit) {
        db.child("objects")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(LearningObject::class.java) })
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getPublishedLearningObjects(onResult: (List<LearningObject>) -> Unit) {
        getLearningObjects { objects ->
            onResult(
                objects.filter { objectItem ->
                    objectItem.status.isBlank() || objectItem.status.equals("published", ignoreCase = true)
                }
            )
        }
    }

    fun getLearningObject(objectId: String, onResult: (LearningObject?) -> Unit) {
        db.child("objects").child(objectId).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(LearningObject::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun saveLearningObject(obj: LearningObject, onComplete: (Boolean) -> Unit = {}) {
        db.child("objects").child(obj.id).setValue(obj)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateLearningObjectStatus(
        objectId: String,
        status: String,
        updatedAt: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val updates = mapOf<String, Any>(
            "status" to status,
            "updatedAt" to updatedAt
        )
        db.child("objects").child(objectId).updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveMission(mission: Mission, onComplete: (Boolean) -> Unit = {}) {
        db.child("missions").child(mission.id).setValue(mission)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllMissions(onResult: (List<Mission>) -> Unit) {
        db.child("missions")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(Mission::class.java) })
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getMissionsForSection(section: String, onResult: (List<Mission>) -> Unit) {
        db.child("missions")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val missions = snap.children.mapNotNull { it.getValue(Mission::class.java) }
                        .filter { mission ->
                            mission.active && (
                                mission.sectionIds.isEmpty() ||
                                    mission.sectionIds.any { it.equals(section, ignoreCase = true) }
                                )
                        }
                    onResult(missions)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getMission(missionId: String, onResult: (Mission?) -> Unit) {
        db.child("missions").child(missionId).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(Mission::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getStudentMissionProgressMap(
        studentId: String,
        onResult: (Map<String, StudentMissionProgress>) -> Unit
    ) {
        db.child("student_missions").child(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val map = mutableMapOf<String, StudentMissionProgress>()
                    snap.children.forEach { missionSnap ->
                        val progress = missionSnap.getValue(StudentMissionProgress::class.java) ?: return@forEach
                        map[missionSnap.key ?: progress.missionId] = progress
                    }
                    onResult(map)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyMap()) }
            })
    }

    fun updateStudentMissionProgress(
        studentId: String,
        missionId: String,
        objectId: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        getMission(missionId) { mission ->
            if (mission == null) {
                onComplete(false)
                return@getMission
            }

            db.child("student_missions").child(studentId).child(missionId).get()
                .addOnSuccessListener { snap ->
                    val existing = snap.getValue(StudentMissionProgress::class.java) ?: StudentMissionProgress(missionId = missionId)
                    val updatedCompletedObjects = (existing.completedObjectIds + objectId)
                        .distinct()
                        .filter { it in mission.objectsToFind }
                    val totalObjects = mission.objectsToFind.distinct().size.coerceAtLeast(1)
                    val progressPercent = (updatedCompletedObjects.size * 100) / totalObjects
                    val updated = StudentMissionProgress(
                        missionId = missionId,
                        completedObjectIds = updatedCompletedObjects,
                        progressPercent = progressPercent,
                        completed = updatedCompletedObjects.size >= totalObjects,
                        updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
                            .format(java.util.Date())
                    )

                    db.child("student_missions").child(studentId).child(missionId).setValue(updated)
                        .addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener { onComplete(false) }
                }
                .addOnFailureListener { onComplete(false) }
        }
    }

    fun seedLearningObjects(objects: List<LearningObject>, onComplete: (Boolean) -> Unit = {}) {
        val objectsMap = objects.associateBy { it.id }
        db.child("objects").setValue(objectsMap)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveScanAttempt(uid: String, attempt: ScanAttempt, onComplete: (String?) -> Unit = {}) {
        val key = db.child("scan_attempts").child(uid).push().key
        if (key == null) {
            onComplete(null)
            return
        }

        db.child("scan_attempts").child(uid).child(key).setValue(attempt.copy(id = key))
            .addOnSuccessListener { onComplete(key) }
            .addOnFailureListener { onComplete(null) }
    }

    fun getScanAttempts(uid: String, onResult: (List<ScanAttempt>) -> Unit) {
        db.child("scan_attempts").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(ScanAttempt::class.java) }.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun buildStudentProgressList(
        students: List<User>,
        submissionsMap: Map<String, List<Submission>>,
        scannedMap: Map<String, Int>,
        quizAttemptsMap: Map<String, List<QuizAttempt>>,
        scanAttemptsMap: Map<String, List<ScanAttempt>>
    ): List<StudentProgress> {
        return students.map { user ->
            val quizAttempts = quizAttemptsMap[user.id].orEmpty()
            val scanAttempts = scanAttemptsMap[user.id].orEmpty()
            val averageScorePercent = if (quizAttempts.isEmpty()) {
                0
            } else {
                val totalPercent = quizAttempts.sumOf { attempt ->
                    if (attempt.totalQuestions == 0) 0 else (attempt.score * 100) / attempt.totalQuestions
                }
                totalPercent / quizAttempts.size
            }
            StudentProgress(
                userId = user.id,
                name = user.name,
                studentNumber = user.studentNumber,
                section = user.section,
                scannedCount = scannedMap[user.id] ?: 0,
                submissionsCount = submissionsMap[user.id]?.size ?: 0,
                quizAttemptsCount = quizAttempts.size,
                averageScorePercent = averageScorePercent,
                manualCorrectionsCount = scanAttempts.count { it.manualCorrection },
                lowConfidenceCount = scanAttempts.count { it.confidence in 0.0001f..0.54f }
            )
        }
    }

    fun buildLearningObjectAnalytics(
        objects: List<LearningObject>,
        submissionsMap: Map<String, List<Submission>>,
        quizAttemptsMap: Map<String, List<QuizAttempt>>,
        scanAttemptsMap: Map<String, List<ScanAttempt>>
    ): List<LearningObjectAnalytics> {
        val submissions = submissionsMap.values.flatten()
        val quizAttempts = quizAttemptsMap.values.flatten()
        val scanAttempts = scanAttemptsMap.values.flatten()

        return objects.map { objectItem ->
            val objectScans = scanAttempts.filter { it.selectedObjectId == objectItem.id }
            val objectQuizAttempts = quizAttempts.filter { it.objectId == objectItem.id }
            val objectSubmissions = submissions.filter { it.objectId == objectItem.id }
            val averageScorePercent = if (objectQuizAttempts.isEmpty()) {
                0
            } else {
                objectQuizAttempts.sumOf { attempt ->
                    if (attempt.totalQuestions == 0) 0 else (attempt.score * 100) / attempt.totalQuestions
                } / objectQuizAttempts.size
            }

            LearningObjectAnalytics(
                objectId = objectItem.id,
                objectName = objectItem.name,
                category = objectItem.category,
                status = objectItem.status.ifBlank { "published" },
                totalScanSelections = objectScans.size,
                lowConfidenceSelections = objectScans.count { it.confidence in 0.0001f..0.54f },
                manualCorrections = objectScans.count { it.manualCorrection },
                quizAttempts = objectQuizAttempts.size,
                averageQuizScorePercent = averageScorePercent,
                recentLearners = objectSubmissions.map { it.studentId }.distinct().size
            )
        }
    }

    fun buildCategoryAnalytics(
        objects: List<LearningObject>,
        quizAttemptsMap: Map<String, List<QuizAttempt>>,
        scanAttemptsMap: Map<String, List<ScanAttempt>>
    ): List<CategoryAnalytics> {
        val objectById = objects.associateBy { it.id }
        val quizAttempts = quizAttemptsMap.values.flatten()
        val scanAttempts = scanAttemptsMap.values.flatten()

        val categories = objects.map { it.category }.distinct()
        return categories.map { category ->
            val categoryObjectIds = objects
                .filter { it.category.equals(category, ignoreCase = true) }
                .map { it.id }
                .toSet()

            val categoryScans = scanAttempts.filter { it.selectedObjectId in categoryObjectIds }
            val categoryQuizAttempts = quizAttempts.filter { it.objectId in categoryObjectIds }

            val averageScorePercent = if (categoryQuizAttempts.isEmpty()) {
                0
            } else {
                categoryQuizAttempts.sumOf { attempt ->
                    if (attempt.totalQuestions == 0) 0 else (attempt.score * 100) / attempt.totalQuestions
                } / categoryQuizAttempts.size
            }

            CategoryAnalytics(
                category = objectById[categoryObjectIds.firstOrNull()]?.category ?: category,
                totalSelections = categoryScans.size,
                manualCorrections = categoryScans.count { it.manualCorrection },
                lowConfidenceSelections = categoryScans.count { it.confidence in 0.0001f..0.54f },
                averageQuizScorePercent = averageScorePercent
            )
        }
    }
}
