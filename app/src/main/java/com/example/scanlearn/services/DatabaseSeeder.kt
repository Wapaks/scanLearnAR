package com.example.scanlearn.services

import com.example.scanlearn.models.Grade3PilotCurriculum
import com.example.scanlearn.models.LearningData
import com.example.scanlearn.models.LearningObject
import com.example.scanlearn.utils.SchoolStructure

class DatabaseSeeder(private val dbService: RealtimeDbService) {

    fun seed(onComplete: () -> Unit = {}) {
        val allObjects = buildAllObjects()
        dbService.seedLearningObjects(allObjects) {
            onComplete()
        }
    }

    fun ensureCoreDataSeeded(onComplete: () -> Unit = {}) {
        ensureSectionMasterDataSeeded {
            dbService.getLearningObjects { existingObjects ->
                if (existingObjects.isEmpty()) {
                    seed {
                        ensureGrade3PilotCurriculumSeeded(onComplete)
                    }
                } else {
                    ensureGrade3PilotCurriculumSeeded(onComplete)
                }
            }
        }
    }

    private fun ensureSectionMasterDataSeeded(onComplete: () -> Unit = {}) {
        dbService.getAllSections { sections ->
            val expectedCount = SchoolStructure.buildSectionRecords().size
            if (sections.size >= expectedCount) {
                onComplete()
                return@getAllSections
            }

            dbService.seedSections(SchoolStructure.buildSectionRecords()) {
                onComplete()
            }
        }
    }

    fun ensureGrade3PilotCurriculumSeeded(onComplete: () -> Unit = {}) {
        dbService.getCurriculumMap(Grade3PilotCurriculum.GRADE_LEVEL) { existingMap ->
            val shouldSeedCurriculum = existingMap == null ||
                !existingMap.quarterIds.contains(Grade3PilotCurriculum.QUARTER2_ID)

            if (!shouldSeedCurriculum) {
                dbService.getQuarter(Grade3PilotCurriculum.QUARTER2_ID) { quarterTwo ->
                    if (quarterTwo != null) {
                        ensureGrade3PilotMissionsSeeded(onComplete)
                    } else {
                        seedGrade3Curriculum(onComplete)
                    }
                }
                return@getCurriculumMap
            }

            seedGrade3Curriculum(onComplete)
        }
    }

    private fun seedGrade3Curriculum(onComplete: () -> Unit = {}) {
        dbService.seedCurriculumContent(
            curriculumMaps = Grade3PilotCurriculum.curriculumMaps,
            quarters = Grade3PilotCurriculum.quarters,
            units = Grade3PilotCurriculum.units,
            lessons = Grade3PilotCurriculum.lessons,
            activities = Grade3PilotCurriculum.activities,
            competencies = Grade3PilotCurriculum.competencies
        ) {
            ensureGrade3PilotMissionsSeeded(onComplete)
        }
    }

    private fun ensureGrade3PilotMissionsSeeded(onComplete: () -> Unit = {}) {
        dbService.getAllMissions { missions ->
            val hasQuarterOneMissions = missions.any { it.quarterId == Grade3PilotCurriculum.QUARTER1_ID }
            val hasQuarterTwoMissions = missions.any { it.quarterId == Grade3PilotCurriculum.QUARTER2_ID }
            if (hasQuarterOneMissions && hasQuarterTwoMissions) {
                onComplete()
                return@getAllMissions
            }

            dbService.seedMissions(Grade3PilotCurriculum.missions) {
                onComplete()
            }
        }
    }

    private fun buildAllObjects(): List<LearningObject> {
        val specific = LearningData.SPECIFIC_TEMPLATES.values.toList()
        val general = LearningData.RANDOM_POOL_BY_CATEGORY.values.flatten()
        return specific + general
    }
}
