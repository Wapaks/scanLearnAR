package com.example.scanlearn.services

import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.ScannedObject
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
            .addOnSuccessListener { snap ->
                onResult(snap.getValue(User::class.java))
            }
            .addOnFailureListener { onResult(null) }
    }

    fun getAllStudents(onResult: (List<User>) -> Unit) {
        db.child("users").orderByChild("role").equalTo("student")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val students = snap.children.mapNotNull { it.getValue(User::class.java) }
                    onResult(students)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveSubmission(uid: String, submission: Submission, onComplete: (Boolean) -> Unit = {}) {
        val key = db.child("submissions").child(uid).push().key ?: return
        val sub = submission.copy(id = key)
        db.child("submissions").child(uid).child(key).setValue(sub)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getSubmissions(uid: String, onResult: (List<Submission>) -> Unit) {
        db.child("submissions").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val list = snap.children.mapNotNull { it.getValue(Submission::class.java) }
                    onResult(list.reversed())
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
                        val subs = userSnap.children.mapNotNull { it.getValue(Submission::class.java) }
                        map[uid] = subs
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
                    val list = snap.children.mapNotNull { it.getValue(ScannedObject::class.java) }
                    onResult(list)
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
                        val uid = userSnap.key ?: return@forEach
                        map[uid] = userSnap.childrenCount.toInt()
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
                    val list = snap.children.mapNotNull { it.getValue(LearningObject::class.java) }
                    onResult(list)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun seedLearningObjects(objects: List<LearningObject>, onComplete: (Boolean) -> Unit = {}) {
        val objectsMap = objects.associateBy { it.id }
        db.child("objects").setValue(objectsMap)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun buildStudentProgressList(
        students: List<User>,
        submissionsMap: Map<String, List<Submission>>,
        scannedMap: Map<String, Int>
    ): List<StudentProgress> {
        return students.map { user ->
            StudentProgress(
                userId = user.id,
                name = user.name,
                studentNumber = user.studentNumber,
                section = user.section,
                scannedCount = scannedMap[user.id] ?: 0,
                submissionsCount = submissionsMap[user.id]?.size ?: 0
            )
        }
    }
}