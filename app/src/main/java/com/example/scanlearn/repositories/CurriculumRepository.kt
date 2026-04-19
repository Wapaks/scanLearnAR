package com.example.scanlearn.repositories

import com.example.scanlearn.models.Competency
import com.example.scanlearn.models.CurriculumMap
import com.example.scanlearn.models.Quarter
import com.example.scanlearn.models.Unit as CurriculumUnit
import com.example.scanlearn.services.RealtimeDbService

class CurriculumRepository(
    private val dbService: RealtimeDbService = RealtimeDbService()
) {

    fun getCurriculumMap(gradeLevel: String, onResult: (CurriculumMap?) -> Unit) {
        dbService.getCurriculumMap(gradeLevel, onResult)
    }

    fun getQuartersForGrade(gradeLevel: String, onResult: (List<Quarter>) -> Unit) {
        dbService.getQuartersForGrade(gradeLevel, onResult)
    }

    fun getUnitsForQuarter(quarterId: String, onResult: (List<CurriculumUnit>) -> Unit) {
        dbService.getUnitsForQuarter(quarterId, onResult)
    }

    fun getUnit(unitId: String, onResult: (CurriculumUnit?) -> Unit) {
        dbService.getUnit(unitId, onResult)
    }

    fun getCompetenciesForQuarter(quarterId: String, onResult: (List<Competency>) -> Unit) {
        dbService.getCompetenciesForQuarter(quarterId, onResult)
    }

    fun saveCurriculumMap(curriculumMap: CurriculumMap, onComplete: (Boolean) -> Unit = {}) {
        dbService.saveCurriculumMap(curriculumMap, onComplete)
    }

    fun saveQuarter(quarter: Quarter, onComplete: (Boolean) -> Unit = {}) {
        dbService.saveQuarter(quarter, onComplete)
    }

    fun saveUnit(unit: CurriculumUnit, onComplete: (Boolean) -> Unit = {}) {
        dbService.saveUnit(unit, onComplete)
    }
}
