package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.StudentProgressAdapter
import com.example.scanlearn.databinding.ActivityTeacherStudentsBinding
import com.example.scanlearn.models.Mission
import com.example.scanlearn.models.SectionRecord
import com.example.scanlearn.models.StudentProgress
import com.example.scanlearn.repositories.CurriculumRepository
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.example.scanlearn.utils.SchoolStructure

class TeacherStudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherStudentsBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService
    private val curriculumRepository = CurriculumRepository()

    private var allStudents: List<StudentProgress> = emptyList()
    private var currentSection = ""
    private var lessonProgressMaps: Map<String, Map<String, com.example.scanlearn.models.StudentLessonProgress>> = emptyMap()
    private var masteryMaps: Map<String, Map<String, com.example.scanlearn.models.MasteryRecord>> = emptyMap()
    private var missionProgressMaps: Map<String, Map<String, com.example.scanlearn.models.StudentMissionProgress>> = emptyMap()
    private var allMissions: List<Mission> = emptyList()
    private var totalQuarterLessons: Int = 0
    private var sectionRecords: List<SectionRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        setupSectionSelector()

        loadStudents()
    }

    override fun onResume() {
        super.onResume()
        loadStudents()
    }

    private fun setupSectionSelector() {
        dbService.getSectionsForGrade(currentTeacherGrade()) { sections ->
            runOnUiThread {
                sectionRecords = sections
                val sectionNames = teacherSections()
                binding.etSectionSelector.setAdapter(
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sectionNames)
                )
                binding.etSectionSelector.setOnItemClickListener { _, _, position, _ ->
                    val section = sectionNames.getOrNull(position).orEmpty()
                    if (section.isNotBlank()) {
                        showSection(section)
                    }
                }
                if (currentSection.isBlank()) {
                    currentSection = sectionNames.firstOrNull().orEmpty()
                    binding.etSectionSelector.setText(currentSection, false)
                }
            }
        }
    }

    private fun loadStudents() {
        setLoadingState(true)
        val teacherGrade = currentTeacherGrade()

        curriculumRepository.getQuartersForGrade(teacherGrade) { quarters ->
            val quarter = quarters.firstOrNull()
            if (quarter == null) {
                totalQuarterLessons = 0
            }
            curriculumRepository.getUnitsForQuarter(quarter?.id.orEmpty()) { units ->
                totalQuarterLessons = units.sumOf { it.lessonIds.size }
                dbService.getAllStudents { students ->
                    dbService.getSubmissionsForAllStudents { submissionsMap ->
                        dbService.getScannedCountForAllStudents { scannedMap ->
                            dbService.getQuizAttemptsForAllStudents { quizAttemptsMap ->
                                dbService.getScanAttemptsForAllStudents { scanAttemptsMap ->
                                    dbService.getAllStudentLessonProgressMaps { lessonMaps ->
                                        dbService.getAllMasteryRecords { masteryRecordMaps ->
                                            dbService.getAllStudentMissionProgressMaps { missionMaps ->
                                                dbService.getAllMissions { missions ->
                                                    lessonProgressMaps = lessonMaps
                                                    masteryMaps = masteryRecordMaps
                                                    missionProgressMaps = missionMaps
                                                    allMissions = missions.filter {
                                                        it.gradeLevel.equals(teacherGrade, ignoreCase = true)
                                                    }
                                                    allStudents = dbService.buildStudentProgressList(
                                                        students = students.filter {
                                                            it.gradeLevel.equals(teacherGrade, ignoreCase = true)
                                                        },
                                                        submissionsMap = submissionsMap,
                                                        scannedMap = scannedMap,
                                                        quizAttemptsMap = quizAttemptsMap,
                                                        scanAttemptsMap = scanAttemptsMap
                                                    ).map { student ->
                                                        val completedLessons = lessonProgressMaps[student.userId]
                                                            .orEmpty()
                                                            .values
                                                            .count { it.status == "completed" }
                                                        val completedTasks = missionProgressMaps[student.userId]
                                                            .orEmpty()
                                                            .values
                                                            .count { it.completed }
                                                        val stalledTasks = missionProgressMaps[student.userId]
                                                            .orEmpty()
                                                            .values
                                                            .count { !it.completed && it.progressPercent in 1..74 }
                                                        val masteredCompetencies = masteryMaps[student.userId]
                                                            .orEmpty()
                                                            .values
                                                            .count { it.masteryStatus == "mastered" }
                                                        student.copy(
                                                            completedLessonsCount = completedLessons,
                                                            completedTasksCount = completedTasks,
                                                            stalledTasksCount = stalledTasks,
                                                            masteredCompetenciesCount = masteredCompetencies
                                                        )
                                                    }

                                                    runOnUiThread {
                                                        setLoadingState(false)
                                                        setupSectionSelector()
                                                        if (currentSection.isBlank()) {
                                                            currentSection = teacherSections().firstOrNull().orEmpty()
                                                            binding.etSectionSelector.setText(currentSection, false)
                                                        }
                                                        showSection(currentSection)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showSection(section: String) {
        currentSection = section
        if (binding.etSectionSelector.text?.toString() != section) {
            binding.etSectionSelector.setText(section, false)
        }

        val filtered = allStudents.filter {
            it.section.trim().equals(section.trim(), ignoreCase = true)
        }
        binding.tvSectionTitle.text = "$section Section - ${filtered.size} student(s)"
        bindSectionSummary(filtered)

        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvStudents.visibility = View.GONE
            binding.tvEmptySection.text = "No students in $section yet."
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvStudents.visibility = View.VISIBLE
            binding.rvStudents.layoutManager = LinearLayoutManager(this)
            binding.rvStudents.adapter = StudentProgressAdapter(filtered) { student ->
                openStudentDetail(student)
            }
        }
    }

    private fun bindSectionSummary(filtered: List<StudentProgress>) {
        if (filtered.isEmpty()) {
            binding.tvSectionCurriculumSummary.text = "No students in this section yet."
            binding.tvSectionInterventionSummary.text = "No intervention signals yet."
            return
        }

        val completedLessons = filtered.sumOf { student ->
            lessonProgressMaps[student.userId].orEmpty().values.count { it.status == "completed" }
        }
        val totalPossibleLessons = (totalQuarterLessons * filtered.size).coerceAtLeast(1)
        val sectionCompletionPercent = (completedLessons * 100) / totalPossibleLessons

        val masteredCompetencies = filtered.sumOf { student ->
            masteryMaps[student.userId].orEmpty().values.count { it.masteryStatus == "mastered" }
        }
        val totalRecordedCompetencies = filtered.sumOf { student ->
            masteryMaps[student.userId].orEmpty().size
        }.coerceAtLeast(1)
        val masteryPercent = (masteredCompetencies * 100) / totalRecordedCompetencies

        val needsIntervention = filtered.count { student ->
            student.averageScorePercent < 75 || student.lowConfidenceCount >= 2 || student.manualCorrectionsCount >= 2
        }
        val quarterMissionIds = allMissions.filter { it.active && it.quarterId.isNotBlank() }.map { it.id }.toSet()
        val completedMissionRuns = filtered.sumOf { student ->
            missionProgressMaps[student.userId].orEmpty()
                .filterKeys { it in quarterMissionIds }
                .values.count { it.completed }
        }
        val stalledMissionRuns = filtered.sumOf { student ->
            missionProgressMaps[student.userId].orEmpty()
                .filterKeys { it in quarterMissionIds }
                .values.count { !it.completed && it.progressPercent in 1..74 }
        }

        binding.tvSectionCurriculumSummary.text =
            "$sectionCompletionPercent% lesson completion across ${filtered.size} learners. Mastery trend: $masteryPercent%. Task completions: $completedMissionRuns."
        binding.tvSectionInterventionSummary.text =
            "$needsIntervention learner(s) may need intervention based on weak quiz scores or repeated low-confidence cases. $stalledMissionRuns task run(s) are currently stalled."
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.rvStudents.visibility = View.GONE
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun openStudentDetail(student: StudentProgress) {
        val intent = Intent(this, StudentDetailActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_STUDENT_PROGRESS, student)
        startActivity(intent)
    }

    private fun currentTeacherGrade(): String {
        return SchoolStructure.normalizeGradeLevel(storageService.getUser()?.gradeLevel.orEmpty())
            .ifBlank { SchoolStructure.defaultGradeLevelForRole("teacher") }
    }

    private fun teacherSections(): List<String> {
        return sectionRecords.map { it.name }.ifEmpty {
            SchoolStructure.sectionsForGrade(currentTeacherGrade())
        }
    }
}
