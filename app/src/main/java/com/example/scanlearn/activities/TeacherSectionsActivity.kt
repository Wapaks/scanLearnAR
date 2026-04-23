package com.example.scanlearn.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.scanlearn.adapters.SectionIssueAdapter
import com.example.scanlearn.adapters.SectionVerificationAdapter
import com.example.scanlearn.databinding.ActivityTeacherSectionsBinding
import com.example.scanlearn.models.SectionRecord
import com.example.scanlearn.models.User
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.SchoolStructure

class TeacherSectionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherSectionsBinding
    private lateinit var dbService: RealtimeDbService
    private lateinit var storageService: StorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherSectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbService = RealtimeDbService()
        storageService = StorageService(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.rvSections.layoutManager = LinearLayoutManager(this)
        binding.rvIssues.layoutManager = LinearLayoutManager(this)

        loadSectionVerification()
    }

    override fun onResume() {
        super.onResume()
        loadSectionVerification()
    }

    private fun loadSectionVerification() {
        setLoading(true)
        val teacherGrade = teacherGrade()
        binding.tvScreenSubtitle.text = "Inspect Firebase sections and assignment health for $teacherGrade."
        binding.tvGradeBadge.text = teacherGrade

        dbService.getSectionsForGrade(teacherGrade) { sections ->
            dbService.getAllUsers { users ->
                runOnUiThread {
                    renderSectionVerification(teacherGrade, sections, users)
                }
            }
        }
    }

    private fun renderSectionVerification(
        teacherGrade: String,
        sections: List<SectionRecord>,
        users: List<User>
    ) {
        val gradeSections = sections.ifEmpty {
            SchoolStructure.sectionsForGrade(teacherGrade).map { sectionName ->
                SectionRecord(
                    id = SchoolStructure.buildSectionId(teacherGrade, sectionName),
                    name = sectionName,
                    gradeLevel = teacherGrade
                )
            }
        }
        val validSectionNames = gradeSections.map { it.name }.toSet()
        val gradeUsers = users.filter { user ->
            user.gradeLevel.equals(teacherGrade, ignoreCase = true) ||
                validSectionNames.contains(
                    SchoolStructure.resolveSectionName(user.section.ifBlank { user.sectionId })
                )
        }
        val teacherAssignments = gradeUsers.filter { it.role.equals("teacher", ignoreCase = true) }
        val studentAssignments = gradeUsers.filter { it.role.equals("student", ignoreCase = true) }
        val invalidStudents = studentAssignments.filter { user ->
            val normalizedGrade = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role)
            val section = SchoolStructure.resolveSectionName(user.section.ifBlank { user.sectionId })
            normalizedGrade != teacherGrade || section.isBlank() || section !in validSectionNames
        }

        val rows = gradeSections.map { section ->
            SectionVerificationAdapter.SectionVerificationRow(
                sectionName = section.name,
                studentCount = studentAssignments.count {
                    SchoolStructure.resolveSectionName(it.section.ifBlank { it.sectionId })
                        .equals(section.name, ignoreCase = true)
                },
                hasInvalidAssignments = invalidStudents.isNotEmpty()
            )
        }

        binding.tvSectionCount.text = gradeSections.size.toString()
        binding.tvStudentCount.text = studentAssignments.size.toString()
        binding.tvTeacherCount.text = teacherAssignments.size.toString()
        binding.tvIssueCount.text = invalidStudents.size.toString()

        binding.rvSections.adapter = SectionVerificationAdapter(rows)
        binding.tvTeacherSummary.text = if (teacherAssignments.isEmpty()) {
            "No teachers are currently assigned to $teacherGrade in Firebase users."
        } else {
            "Teachers assigned to $teacherGrade: " + teacherAssignments.joinToString(", ") { user ->
                user.name.ifBlank { user.email }
            }
        }

        binding.tvIssueSummary.text = if (invalidStudents.isEmpty()) {
            "No assignment issues found. Students in $teacherGrade match the section master records."
        } else {
            invalidStudents.joinToString("\n") { user ->
                val currentSection = SchoolStructure.resolveSectionName(user.section.ifBlank { user.sectionId })
                val label = if (currentSection.isBlank()) "missing section" else "invalid section: $currentSection"
                "${user.name.ifBlank { user.email }} - $label"
            }
        }
        binding.rvIssues.adapter = SectionIssueAdapter(
            items = invalidStudents.map { user ->
                val currentSection = SchoolStructure.resolveSectionName(user.section.ifBlank { user.sectionId })
                SectionIssueAdapter.SectionIssueRow(
                    userId = user.id,
                    studentName = user.name.ifBlank { user.email },
                    studentMeta = listOf(user.studentNumber, user.email).filter { it.isNotBlank() }.joinToString(" • "),
                    issueDetail = buildString {
                        val currentGrade = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role)
                        if (currentGrade != teacherGrade) {
                            append("Wrong grade assignment: $currentGrade")
                            if (currentSection.isNotBlank()) {
                                append(" • ")
                            }
                        }
                        append(
                            if (currentSection.isBlank()) {
                                "Missing section assignment"
                            } else {
                                "Invalid section assignment: $currentSection"
                            }
                        )
                    },
                    currentGradeLevel = SchoolStructure.resolveGradeLevel(user.gradeLevel, user.role),
                    currentSection = currentSection
                )
            },
            onRepair = { issue ->
                showGradeRepairDialog(issue)
            }
        )
        binding.emptyIssues.visibility = if (invalidStudents.isEmpty()) View.VISIBLE else View.GONE
        binding.tvIssueSummary.visibility = if (invalidStudents.isEmpty()) View.GONE else View.VISIBLE
        binding.rvIssues.visibility = if (invalidStudents.isEmpty()) View.GONE else View.VISIBLE
        setLoading(false)
    }

    private fun showGradeRepairDialog(issue: SectionIssueAdapter.SectionIssueRow) {
        val grades = SchoolStructure.gradeLevels
        MaterialAlertDialogBuilder(this)
            .setTitle("Fix Grade And Section")
            .setMessage(
                "Current assignment: ${issue.currentGradeLevel.ifBlank { "Unknown Grade" }} / " +
                    "${issue.currentSection.ifBlank { "No Section" }}.\nChoose the correct grade first."
            )
            .setItems(grades.toTypedArray()) { dialog, which ->
                val selectedGrade = grades.getOrNull(which).orEmpty()
                if (selectedGrade.isNotBlank()) {
                    showSectionRepairDialog(issue, selectedGrade)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSectionRepairDialog(
        issue: SectionIssueAdapter.SectionIssueRow,
        selectedGrade: String
    ) {
        val validSections = SchoolStructure.sectionsForGrade(selectedGrade)
        if (validSections.isEmpty()) {
            Toast.makeText(this, "No valid sections are available for $selectedGrade.", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Fix Section")
            .setMessage("Assign ${issue.studentName} to a valid section for $selectedGrade.")
            .setItems(validSections.toTypedArray()) { dialog, which ->
                val selectedSection = validSections.getOrNull(which).orEmpty()
                if (selectedSection.isNotBlank()) {
                    repairStudentAssignment(issue.userId, selectedGrade, selectedSection)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun repairStudentAssignment(userId: String, teacherGrade: String, selectedSection: String) {
        setLoading(true)
        dbService.getUser(userId) { user ->
            if (user == null) {
                runOnUiThread {
                    setLoading(false)
                    Toast.makeText(this, "Could not load this student record.", Toast.LENGTH_SHORT).show()
                }
                return@getUser
            }

            val updatedUser = user.copy(
                gradeLevel = teacherGrade,
                section = selectedSection,
                sectionId = selectedSection
            )
            dbService.saveUser(updatedUser) { success ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(
                            this,
                            "${updatedUser.name.ifBlank { updatedUser.email }} is now assigned to $selectedSection.",
                            Toast.LENGTH_LONG
                        ).show()
                        loadSectionVerification()
                    } else {
                        setLoading(false)
                        Toast.makeText(this, "Could not update the assignment.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun teacherGrade(): String {
        return SchoolStructure.resolveGradeLevel(storageService.getUser()?.gradeLevel.orEmpty(), "teacher")
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}
