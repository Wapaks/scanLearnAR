package com.example.scanlearn.repositories

import com.example.scanlearn.models.MasteryRecord
import com.example.scanlearn.models.StudentLessonProgress
import com.example.scanlearn.services.RealtimeDbService

class ProgressRepository(
    private val dbService: RealtimeDbService = RealtimeDbService()
) {

    fun getStudentLessonProgress(
        studentId: String,
        lessonId: String,
        onResult: (StudentLessonProgress?) -> Unit
    ) {
        dbService.getStudentLessonProgress(studentId, lessonId, onResult)
    }

    fun getStudentLessonProgressMap(
        studentId: String,
        onResult: (Map<String, StudentLessonProgress>) -> Unit
    ) {
        dbService.getStudentLessonProgressMap(studentId, onResult)
    }

    fun saveStudentLessonProgress(
        studentId: String,
        progress: StudentLessonProgress,
        onComplete: (Boolean) -> Unit = {}
    ) {
        dbService.saveStudentLessonProgress(studentId, progress.lessonId, progress, onComplete)
    }

    fun getMasteryRecords(
        studentId: String,
        onResult: (Map<String, MasteryRecord>) -> Unit
    ) {
        dbService.getMasteryRecordsForStudent(studentId, onResult)
    }

    fun saveMasteryRecord(
        studentId: String,
        record: MasteryRecord,
        onComplete: (Boolean) -> Unit = {}
    ) {
        dbService.saveMasteryRecord(studentId, record.competencyId, record, onComplete)
    }
}
