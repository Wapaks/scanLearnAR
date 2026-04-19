package com.example.scanlearn.services

import com.example.scanlearn.models.AiDraftVariant
import com.example.scanlearn.models.AiUsageLog
import com.example.scanlearn.models.CategoryAnalytics
import com.example.scanlearn.models.ChatConversation
import com.example.scanlearn.models.ChatMessage
import com.example.scanlearn.models.Competency
import com.example.scanlearn.models.CurriculumMap
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.models.LearningObjectAnalytics
import com.example.scanlearn.models.Lesson
import com.example.scanlearn.models.LessonActivity
import com.example.scanlearn.models.MasteryRecord
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.QuizAttempt
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.models.ScanAttempt
import com.example.scanlearn.models.ScannedObject
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.models.StudentMissionProgress
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.models.Submission
import com.example.scanlearn.models.Unit as CurriculumUnit
import com.example.scanlearn.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun getAllUsers(onResult: (List<User>) -> Unit) {
        db.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val users = snap.children.mapNotNull { child ->
                        child.getValue(User::class.java)
                    }
                    onResult(users)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getTeachers(onResult: (List<User>) -> Unit) {
        getAllUsers { users ->
            onResult(
                users.filter { user -> user.role.trim().equals("teacher", ignoreCase = true) }
            )
        }
    }

    fun getChatContacts(currentUser: User, onResult: (List<User>) -> Unit) {
        getAllUsers { users ->
            val contacts = users.filter { otherUser ->
                otherUser.id.isNotBlank() &&
                    otherUser.id != currentUser.id &&
                    canUsersChat(currentUser, otherUser)
            }.sortedWith(compareBy<User> { chatRoleRank(it.role) }.thenBy { it.name.lowercase() })
            onResult(contacts)
        }
    }

    fun getConversationsForUser(userId: String, onResult: (List<ChatConversation>) -> Unit) {
        db.child("chat_conversations")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val conversations = snap.children.mapNotNull { child ->
                        child.getValue(ChatConversation::class.java)
                    }.filter { conversation ->
                        conversation.participantIds.contains(userId)
                    }.sortedByDescending { it.lastUpdatedAt }
                    onResult(conversations)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun observeConversationsForUser(
        userId: String,
        onResult: (List<ChatConversation>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val conversations = snap.children.mapNotNull { child ->
                    child.getValue(ChatConversation::class.java)
                }.filter { conversation ->
                    conversation.participantIds.contains(userId)
                }.sortedByDescending { it.lastUpdatedAt }
                onResult(conversations)
            }

            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        }
        db.child("chat_conversations").addValueEventListener(listener)
        return listener
    }

    fun removeConversationsListener(listener: ValueEventListener?) {
        if (listener == null) return
        db.child("chat_conversations").removeEventListener(listener)
    }

    fun getOrCreateConversation(
        currentUser: User,
        otherUser: User,
        onResult: (ChatConversation?) -> Unit
    ) {
        if (!canUsersChat(currentUser, otherUser)) {
            onResult(null)
            return
        }

        val conversationId = buildConversationId(currentUser.id, otherUser.id)
        val conversationRef = db.child("chat_conversations").child(conversationId)
        conversationRef.get()
            .addOnSuccessListener { snap ->
                val existing = snap.getValue(ChatConversation::class.java)
                if (existing != null) {
                    onResult(existing)
                    return@addOnSuccessListener
                }

                val now = nowIsoString()
                val conversation = ChatConversation(
                    id = conversationId,
                    participantIds = listOf(currentUser.id, otherUser.id).sorted(),
                    participantNames = mapOf(
                        currentUser.id to currentUser.name,
                        otherUser.id to otherUser.name
                    ),
                    participantRoles = mapOf(
                        currentUser.id to currentUser.role,
                        otherUser.id to otherUser.role
                    ),
                    unreadCounts = mapOf(
                        currentUser.id to 0,
                        otherUser.id to 0
                    ),
                    createdAt = now,
                    lastUpdatedAt = now
                )
                conversationRef.setValue(conversation)
                    .addOnSuccessListener { onResult(conversation) }
                    .addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener { onResult(null) }
    }

    fun sendChatMessage(
        sender: User,
        receiver: User,
        messageText: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val trimmed = messageText.trim()
        if (trimmed.isBlank()) {
            onComplete(false, "Message cannot be empty.")
            return
        }
        if (!canUsersChat(sender, receiver)) {
            onComplete(false, "This chat is not allowed.")
            return
        }

        getOrCreateConversation(sender, receiver) { conversation ->
            if (conversation == null) {
                onComplete(false, "Could not open the conversation.")
                return@getOrCreateConversation
            }

            val messagesRef = db.child("chat_messages").child(conversation.id)
            val key = messagesRef.push().key
            if (key == null) {
                onComplete(false, "Could not create the message.")
                return@getOrCreateConversation
            }

            val now = nowIsoString()
            val message = ChatMessage(
                id = key,
                conversationId = conversation.id,
                senderId = sender.id,
                senderName = sender.name,
                receiverId = receiver.id,
                message = trimmed,
                createdAt = now
            )

            messagesRef.child(key).setValue(message)
                .addOnSuccessListener {
                    val updatedConversation = conversation.copy(
                        participantNames = conversation.participantNames + mapOf(
                            sender.id to sender.name,
                            receiver.id to receiver.name
                        ),
                        participantRoles = conversation.participantRoles + mapOf(
                            sender.id to sender.role,
                            receiver.id to receiver.role
                        ),
                        unreadCounts = conversation.unreadCounts.toMutableMap().apply {
                            put(sender.id, 0)
                            put(receiver.id, (get(receiver.id) ?: 0) + 1)
                        },
                        lastMessage = trimmed,
                        lastSenderId = sender.id,
                        lastUpdatedAt = now
                    )
                    db.child("chat_conversations").child(conversation.id).setValue(updatedConversation)
                        .addOnSuccessListener { onComplete(true, null) }
                        .addOnFailureListener { onComplete(false, "Message sent but thread update failed.") }
                }
                .addOnFailureListener { onComplete(false, "Could not send the message.") }
        }
    }

    fun observeMessages(
        conversationId: String,
        onResult: (List<ChatMessage>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val messages = snap.children.mapNotNull { child ->
                    child.getValue(ChatMessage::class.java)
                }.sortedBy { it.createdAt }
                onResult(messages)
            }

            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        }
        db.child("chat_messages").child(conversationId).addValueEventListener(listener)
        return listener
    }

    fun removeMessagesListener(conversationId: String, listener: ValueEventListener?) {
        if (listener == null) return
        db.child("chat_messages").child(conversationId).removeEventListener(listener)
    }

    fun markConversationAsRead(
        conversationId: String,
        userId: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (conversationId.isBlank() || userId.isBlank()) {
            onComplete(false)
            return
        }

        val conversationRef = db.child("chat_conversations").child(conversationId)
        conversationRef.get()
            .addOnSuccessListener { snap ->
                val conversation = snap.getValue(ChatConversation::class.java)
                if (conversation == null) {
                    onComplete(false)
                    return@addOnSuccessListener
                }

                val currentUnread = conversation.unreadCounts[userId] ?: 0
                if (currentUnread == 0) {
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val updatedCounts = conversation.unreadCounts.toMutableMap().apply {
                    put(userId, 0)
                }
                conversationRef.child("unreadCounts").setValue(updatedCounts)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUnreadConversationCount(conversations: List<ChatConversation>, userId: String): Int {
        return conversations.count { conversation -> (conversation.unreadCounts[userId] ?: 0) > 0 }
    }

    fun getTotalUnreadMessages(conversations: List<ChatConversation>, userId: String): Int {
        return conversations.sumOf { conversation -> conversation.unreadCounts[userId] ?: 0 }
    }

    fun getAllStudents(onResult: (List<User>) -> Unit) {
        db.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val students = snap.children.mapNotNull { child ->
                        child.getValue(User::class.java)
                    }.filter { user ->
                        val role = user.role.trim().lowercase()
                        role == "student" || (
                            role.isBlank() &&
                                (user.studentNumber.isNotBlank() || user.section.isNotBlank())
                            )
                    }.map { user ->
                        user.copy(section = normalizeSectionName(user.section))
                    }
                    onResult(students)
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

    fun saveCurriculumMap(curriculumMap: CurriculumMap, onComplete: (Boolean) -> Unit = {}) {
        db.child("curriculum_maps").child(curriculumMap.gradeLevel).setValue(curriculumMap)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveAiDraftVariant(variant: AiDraftVariant, onComplete: (Boolean) -> Unit = {}) {
        val key = variant.id.ifBlank { db.child("ai_draft_variants").push().key.orEmpty() }
        if (key.isBlank()) {
            onComplete(false)
            return
        }
        db.child("ai_draft_variants").child(key).setValue(variant.copy(id = key))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAiDraftVariantsForTarget(
        targetType: String,
        targetId: String,
        onResult: (List<AiDraftVariant>) -> Unit
    ) {
        db.child("ai_draft_variants")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val variants = snap.children.mapNotNull { it.getValue(AiDraftVariant::class.java) }
                        .filter { it.targetType == targetType && it.targetId == targetId }
                        .sortedByDescending { it.createdAt }
                    onResult(variants)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveAiUsageLog(log: AiUsageLog, onComplete: (Boolean) -> Unit = {}) {
        val key = log.id.ifBlank { db.child("ai_usage_logs").push().key.orEmpty() }
        if (key.isBlank()) {
            onComplete(false)
            return
        }
        db.child("ai_usage_logs").child(key).setValue(log.copy(id = key))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getCurriculumMap(gradeLevel: String, onResult: (CurriculumMap?) -> Unit) {
        db.child("curriculum_maps").child(gradeLevel).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(CurriculumMap::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getAllCurriculumMaps(onResult: (List<CurriculumMap>) -> Unit) {
        db.child("curriculum_maps")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(CurriculumMap::class.java) })
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveQuarter(quarter: Quarter, onComplete: (Boolean) -> Unit = {}) {
        db.child("quarters").child(quarter.id).setValue(quarter)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getQuarter(quarterId: String, onResult: (Quarter?) -> Unit) {
        db.child("quarters").child(quarterId).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(Quarter::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getQuartersForGrade(gradeLevel: String, onResult: (List<Quarter>) -> Unit) {
        db.child("quarters")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val quarters = snap.children.mapNotNull { it.getValue(Quarter::class.java) }
                        .filter { it.gradeLevel.equals(gradeLevel, ignoreCase = true) }
                        .sortedWith(compareBy<Quarter> { it.orderIndex }.thenBy { it.quarterNumber })
                    onResult(quarters)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveUnit(unit: CurriculumUnit, onComplete: (Boolean) -> Unit = {}) {
        db.child("units").child(unit.id).setValue(unit)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getUnit(unitId: String, onResult: (CurriculumUnit?) -> Unit) {
        db.child("units").child(unitId).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(CurriculumUnit::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getUnitsForQuarter(quarterId: String, onResult: (List<CurriculumUnit>) -> Unit) {
        db.child("units")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val units = snap.children.mapNotNull { it.getValue(CurriculumUnit::class.java) }
                        .filter { it.quarterId == quarterId }
                        .sortedBy { it.orderIndex }
                    onResult(units)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveLesson(lesson: Lesson, onComplete: (Boolean) -> Unit = {}) {
        db.child("lessons").child(lesson.id).setValue(lesson)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getLesson(lessonId: String, onResult: (Lesson?) -> Unit) {
        db.child("lessons").child(lessonId).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(Lesson::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getAllLessons(onResult: (List<Lesson>) -> Unit) {
        db.child("lessons")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    onResult(snap.children.mapNotNull { it.getValue(Lesson::class.java) })
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getLessonsForUnit(unitId: String, onResult: (List<Lesson>) -> Unit) {
        db.child("lessons")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val lessons = snap.children.mapNotNull { it.getValue(Lesson::class.java) }
                        .filter { it.unitId == unitId }
                        .sortedBy { it.orderIndex }
                    onResult(lessons)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveLessonActivity(activity: LessonActivity, onComplete: (Boolean) -> Unit = {}) {
        db.child("activities").child(activity.id).setValue(activity)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getActivitiesForLesson(lessonId: String, onResult: (List<LessonActivity>) -> Unit) {
        db.child("activities")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val activities = snap.children.mapNotNull { it.getValue(LessonActivity::class.java) }
                        .filter { it.lessonId == lessonId }
                        .sortedBy { it.orderIndex }
                    onResult(activities)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun deleteActivitiesForLesson(lessonId: String, onComplete: (Boolean) -> Unit = {}) {
        db.child("activities")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val updates = mutableMapOf<String, Any?>()
                    snap.children.forEach { child ->
                        val activity = child.getValue(LessonActivity::class.java) ?: return@forEach
                        if (activity.lessonId == lessonId) {
                            updates["activities/${child.key}"] = null
                        }
                    }
                    if (updates.isEmpty()) {
                        onComplete(true)
                        return
                    }
                    db.updateChildren(updates)
                        .addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener { onComplete(false) }
                }

                override fun onCancelled(error: DatabaseError) { onComplete(false) }
            })
    }

    fun saveCompetency(competency: Competency, onComplete: (Boolean) -> Unit = {}) {
        db.child("competencies").child(competency.id).setValue(competency)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getCompetenciesForQuarter(quarterId: String, onResult: (List<Competency>) -> Unit) {
        db.child("competencies")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val competencies = snap.children.mapNotNull { it.getValue(Competency::class.java) }
                        .filter { it.quarterId == quarterId }
                    onResult(competencies)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun saveStudentLessonProgress(
        studentId: String,
        lessonId: String,
        progress: StudentLessonProgress,
        onComplete: (Boolean) -> Unit = {}
    ) {
        db.child("student_lesson_progress").child(studentId).child(lessonId).setValue(progress)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getStudentLessonProgress(
        studentId: String,
        lessonId: String,
        onResult: (StudentLessonProgress?) -> Unit
    ) {
        db.child("student_lesson_progress").child(studentId).child(lessonId).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(StudentLessonProgress::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getStudentLessonProgressMap(
        studentId: String,
        onResult: (Map<String, StudentLessonProgress>) -> Unit
    ) {
        db.child("student_lesson_progress").child(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val map = mutableMapOf<String, StudentLessonProgress>()
                    snap.children.forEach { lessonSnap ->
                        val progress = lessonSnap.getValue(StudentLessonProgress::class.java) ?: return@forEach
                        map[lessonSnap.key ?: progress.lessonId] = progress
                    }
                    onResult(map)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyMap()) }
            })
    }

    fun saveMasteryRecord(
        studentId: String,
        competencyId: String,
        record: MasteryRecord,
        onComplete: (Boolean) -> Unit = {}
    ) {
        db.child("mastery_records").child(studentId).child(competencyId).setValue(record)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getMasteryRecord(
        studentId: String,
        competencyId: String,
        onResult: (MasteryRecord?) -> Unit
    ) {
        db.child("mastery_records").child(studentId).child(competencyId).get()
            .addOnSuccessListener { snap -> onResult(snap.getValue(MasteryRecord::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun getMasteryRecordsForStudent(
        studentId: String,
        onResult: (Map<String, MasteryRecord>) -> Unit
    ) {
        db.child("mastery_records").child(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val map = mutableMapOf<String, MasteryRecord>()
                    snap.children.forEach { competencySnap ->
                        val record = competencySnap.getValue(MasteryRecord::class.java) ?: return@forEach
                        map[competencySnap.key ?: record.competencyId] = record
                    }
                    onResult(map)
                }

                override fun onCancelled(error: DatabaseError) { onResult(emptyMap()) }
            })
    }

    fun seedCurriculumContent(
        curriculumMaps: List<CurriculumMap>,
        quarters: List<Quarter>,
        units: List<CurriculumUnit>,
        lessons: List<Lesson>,
        activities: List<LessonActivity>,
        competencies: List<Competency>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val values = mapOf(
            "curriculum_maps" to curriculumMaps.associateBy { it.gradeLevel },
            "quarters" to quarters.associateBy { it.id },
            "units" to units.associateBy { it.id },
            "lessons" to lessons.associateBy { it.id },
            "activities" to activities.associateBy { it.id },
            "competencies" to competencies.associateBy { it.id }
        )

        db.updateChildren(values)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
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
                section = normalizeSectionName(user.section),
                scannedCount = scannedMap[user.id] ?: 0,
                submissionsCount = submissionsMap[user.id]?.size ?: 0,
                quizAttemptsCount = quizAttempts.size,
                averageScorePercent = averageScorePercent,
                manualCorrectionsCount = scanAttempts.count { it.manualCorrection },
                lowConfidenceCount = scanAttempts.count { it.confidence in 0.0001f..0.54f }
            )
        }
    }

    private fun normalizeSectionName(section: String): String {
        val cleaned = section.trim()
        return when {
            cleaned.equals("santan", ignoreCase = true) -> "Santan"
            cleaned.equals("daisy", ignoreCase = true) -> "Daisy"
            cleaned.equals("orchid", ignoreCase = true) -> "Orchid"
            else -> cleaned
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

    fun canUsersChat(sender: User, receiver: User): Boolean {
        if (sender.id.isBlank() || receiver.id.isBlank() || sender.id == receiver.id) return false

        val senderRole = sender.role.trim().lowercase()
        val receiverRole = receiver.role.trim().lowercase()

        return when (senderRole) {
            "teacher" -> receiverRole == "teacher" || receiverRole == "student"
            "student" -> receiverRole == "teacher"
            else -> false
        }
    }

    private fun buildConversationId(firstUserId: String, secondUserId: String): String {
        return listOf(firstUserId, secondUserId).sorted().joinToString("_")
    }

    private fun chatRoleRank(role: String): Int {
        return when (role.trim().lowercase()) {
            "teacher" -> 0
            "student" -> 1
            else -> 2
        }
    }

    private fun nowIsoString(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
    }
}
