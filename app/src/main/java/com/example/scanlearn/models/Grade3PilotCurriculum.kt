package com.example.scanlearn.models

import com.example.scanlearn.utils.SchoolStructure

object Grade3PilotCurriculum {

    const val GRADE_LEVEL = "Grade 3"
    const val QUARTER1_ID = "g3_q1"
    const val QUARTER2_ID = "g3_q2"
    private val pilotSections = SchoolStructure.sectionsForGrade(GRADE_LEVEL)

    val curriculumMaps = listOf(
        CurriculumMap(
            gradeLevel = GRADE_LEVEL,
            title = "Grade 3 Science",
            quarterIds = listOf(QUARTER1_ID, QUARTER2_ID)
        )
    )

    val quarters = listOf(
        Quarter(
            id = QUARTER1_ID,
            gradeLevel = GRADE_LEVEL,
            quarterNumber = 1,
            title = "Body Parts and Our Surroundings",
            description = "A Grade 3 pilot quarter about the body, the senses, and living things found around us.",
            unitIds = listOf("g3_q1_unit_body", "g3_q1_unit_surroundings"),
            orderIndex = 1,
            status = "published"
        ),
        Quarter(
            id = QUARTER2_ID,
            gradeLevel = GRADE_LEVEL,
            quarterNumber = 2,
            title = "Rocks, Soil, and Materials Around Us",
            description = "A Grade 3 quarter focused on earth materials, everyday objects, and classifying matter as solid, liquid, or gas.",
            unitIds = listOf("g3_q2_unit_earth_materials", "g3_q2_unit_states_of_matter"),
            orderIndex = 2,
            status = "draft"
        )
    )

    val units = listOf(
        Unit(
            id = "g3_q1_unit_body",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            title = "Knowing My Body",
            overview = "Students learn the main external body parts, the sense organs, and simple ways to care for the body.",
            lessonIds = listOf("g3_q1_lesson_body_parts", "g3_q1_lesson_sense_organs"),
            orderIndex = 1,
            estimatedMinutes = 60,
            status = "published"
        ),
        Unit(
            id = "g3_q1_unit_surroundings",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            title = "Living Things Around Us",
            overview = "Students observe things around home and school and identify simple plant examples found in their surroundings.",
            lessonIds = listOf("g3_q1_lesson_living_things", "g3_q1_lesson_school_plants"),
            orderIndex = 2,
            estimatedMinutes = 70,
            status = "published"
        ),
        Unit(
            id = "g3_q2_unit_earth_materials",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            title = "Earth Materials Around Us",
            overview = "Students observe common rocks and soil and describe simple visible properties and uses.",
            lessonIds = listOf("g3_q2_lesson_rocks", "g3_q2_lesson_soil"),
            orderIndex = 1,
            estimatedMinutes = 60,
            status = "draft"
        ),
        Unit(
            id = "g3_q2_unit_states_of_matter",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            title = "States of Matter in Daily Life",
            overview = "Students compare common materials and classify familiar examples as solid, liquid, or gas.",
            lessonIds = listOf("g3_q2_lesson_states_of_matter", "g3_q2_lesson_material_uses"),
            orderIndex = 2,
            estimatedMinutes = 70,
            status = "draft"
        )
    )

    val competencies = listOf(
        Competency(
            id = "g3_q1_comp_body_parts",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            code = "G3-Q1-C1",
            statement = "Identify the main external body parts and tell what they do.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q1_comp_senses",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            code = "G3-Q1-C2",
            statement = "Describe the sense organs and match them with what they help us do.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q1_comp_living_nonliving",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            code = "G3-Q1-C3",
            statement = "Classify common things in the surroundings as living or non-living.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q1_comp_school_plants",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            code = "G3-Q1-C4",
            statement = "Observe common plants around school or home and describe simple features.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q2_comp_rocks",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            code = "G3-Q2-C1",
            statement = "Identify common rocks and describe simple visible properties such as size, color, and texture.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q2_comp_soil",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            code = "G3-Q2-C2",
            statement = "Describe soil as an earth material and explain simple uses of soil in everyday life.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q2_comp_states",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            code = "G3-Q2-C3",
            statement = "Classify common examples as solid, liquid, or gas using observable clues.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q2_comp_material_uses",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            code = "G3-Q2-C4",
            statement = "Compare common materials and relate their properties to how people use them safely and effectively.",
            masteryThreshold = 75
        )
    )

    val lessons = listOf(
        Lesson(
            id = "g3_q1_lesson_body_parts",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            unitId = "g3_q1_unit_body",
            title = "Main Body Parts",
            objective = "Identify common body parts and tell one use of each part.",
            summary = "Our body has many parts that help us move, see, hear, and do daily tasks. Each body part has an important job.",
            lessonType = "core",
            competencyIds = listOf("g3_q1_comp_body_parts"),
            activityIds = listOf("g3_q1_a1", "g3_q1_a2"),
            assessmentId = "g3_q1_assessment_body_parts",
            orderIndex = 1,
            estimatedMinutes = 20,
            difficulty = "easy",
            status = "published",
            createdBy = "system",
            releasedSectionIds = pilotSections
        ),
        Lesson(
            id = "g3_q1_lesson_sense_organs",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            unitId = "g3_q1_unit_body",
            title = "Sense Organs",
            objective = "Name the sense organs and match each one to a simple function.",
            summary = "We use our eyes, ears, nose, tongue, and skin to learn about the world. These sense organs help us notice sounds, sights, smells, tastes, and touch.",
            lessonType = "core",
            competencyIds = listOf("g3_q1_comp_senses"),
            activityIds = listOf("g3_q1_a3", "g3_q1_a4"),
            assessmentId = "g3_q1_assessment_senses",
            orderIndex = 2,
            estimatedMinutes = 20,
            difficulty = "easy",
            status = "published",
            createdBy = "system",
            releasedSectionIds = pilotSections
        ),
        Lesson(
            id = "g3_q1_lesson_living_things",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            unitId = "g3_q1_unit_surroundings",
            title = "Living and Non-Living Things",
            objective = "Classify things around us as living or non-living using simple clues.",
            summary = "Living things grow, need food and water, and can reproduce. Non-living things do not grow or need food.",
            lessonType = "core",
            scanSupported = true,
            competencyIds = listOf("g3_q1_comp_living_nonliving"),
            activityIds = listOf("g3_q1_a5", "g3_q1_a6"),
            linkedObjectIds = listOf("bird", "santan", "table"),
            assessmentId = "g3_q1_assessment_living_things",
            orderIndex = 1,
            estimatedMinutes = 25,
            difficulty = "easy",
            status = "published",
            createdBy = "system",
            releasedSectionIds = pilotSections
        ),
        Lesson(
            id = "g3_q1_lesson_school_plants",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            unitId = "g3_q1_unit_surroundings",
            title = "Plants Around School and Home",
            objective = "Observe common plants and describe simple visible features such as leaves, stem, and flowers.",
            summary = "Plants around us may have leaves, stems, flowers, or special behaviors. We can observe them closely to learn how they are alike and different.",
            lessonType = "core",
            scanSupported = true,
            competencyIds = listOf("g3_q1_comp_school_plants"),
            activityIds = listOf("g3_q1_a7", "g3_q1_a8"),
            linkedObjectIds = listOf("santan", "oregano", "makahiya"),
            assessmentId = "g3_q1_assessment_school_plants",
            orderIndex = 2,
            estimatedMinutes = 25,
            difficulty = "easy",
            status = "published",
            createdBy = "system",
            releasedSectionIds = pilotSections
        ),
        Lesson(
            id = "g3_q2_lesson_rocks",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            unitId = "g3_q2_unit_earth_materials",
            title = "Common Rocks Around Home and School",
            objective = "Identify common rocks found in everyday surroundings and describe simple visible features.",
            summary = "Rocks can be found in roads, gardens, and school grounds. They differ in color, size, texture, and shape.",
            lessonType = "core",
            scanSupported = true,
            competencyIds = listOf("g3_q2_comp_rocks"),
            activityIds = listOf("g3_q2_a1", "g3_q2_a2"),
            linkedObjectIds = listOf("rock", "stone"),
            assessmentId = "g3_q2_assessment_rocks",
            orderIndex = 1,
            estimatedMinutes = 20,
            difficulty = "easy",
            status = "draft",
            createdBy = "system"
        ),
        Lesson(
            id = "g3_q2_lesson_soil",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            unitId = "g3_q2_unit_earth_materials",
            title = "Soil and What It Is Used For",
            objective = "Describe soil as an important earth material and tell simple uses of soil.",
            summary = "Soil helps plants grow and supports many living things. It can be found in gardens, fields, and pots around home and school.",
            lessonType = "core",
            scanSupported = true,
            competencyIds = listOf("g3_q2_comp_soil"),
            activityIds = listOf("g3_q2_a3", "g3_q2_a4"),
            linkedObjectIds = listOf("santan", "oregano", "makahiya"),
            assessmentId = "g3_q2_assessment_soil",
            orderIndex = 2,
            estimatedMinutes = 20,
            difficulty = "easy",
            status = "draft",
            createdBy = "system"
        ),
        Lesson(
            id = "g3_q2_lesson_states_of_matter",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            unitId = "g3_q2_unit_states_of_matter",
            title = "Solids, Liquids, and Gases",
            objective = "Classify everyday examples as solid, liquid, or gas.",
            summary = "Solids keep their shape, liquids take the shape of containers, and gases spread to fill space.",
            lessonType = "core",
            competencyIds = listOf("g3_q2_comp_states"),
            activityIds = listOf("g3_q2_a5", "g3_q2_a6"),
            linkedObjectIds = listOf("table", "bottle"),
            assessmentId = "g3_q2_assessment_states",
            orderIndex = 1,
            estimatedMinutes = 25,
            difficulty = "easy",
            status = "draft",
            createdBy = "system"
        ),
        Lesson(
            id = "g3_q2_lesson_material_uses",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            unitId = "g3_q2_unit_states_of_matter",
            title = "Materials in Daily Life",
            objective = "Compare common materials by their simple properties and relate them to everyday use.",
            summary = "Different materials such as wood, metal, plastic, glass, soil, and stone are useful in different ways because of their properties.",
            lessonType = "core",
            scanSupported = true,
            competencyIds = listOf("g3_q2_comp_material_uses"),
            activityIds = listOf("g3_q2_a7", "g3_q2_a8"),
            linkedObjectIds = listOf("table", "bottle", "spoon"),
            assessmentId = "g3_q2_assessment_material_uses",
            orderIndex = 2,
            estimatedMinutes = 25,
            difficulty = "easy",
            status = "draft",
            createdBy = "system"
        )
    )

    val activities = listOf(
        LessonActivity(
            id = "g3_q1_a1",
            lessonId = "g3_q1_lesson_body_parts",
            type = "multiple_choice",
            prompt = "Which body part do we use for walking?",
            instructions = "Choose the best answer.",
            content = "Think about the part that helps you stand and move from place to place.",
            options = listOf("Legs", "Ears", "Nose", "Teeth"),
            correctAnswer = "Legs",
            explanation = "Our legs help us stand, walk, run, and jump.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q1_a2",
            lessonId = "g3_q1_lesson_body_parts",
            type = "short_answer",
            prompt = "Name one body part you use when writing.",
            instructions = "Type a short answer.",
            content = "You may answer with hand, fingers, or arm.",
            correctAnswer = "hand",
            explanation = "We often use our hand and fingers when holding a pencil and writing.",
            points = 1,
            orderIndex = 2
        ),
        LessonActivity(
            id = "g3_q1_a3",
            lessonId = "g3_q1_lesson_sense_organs",
            type = "multiple_choice",
            prompt = "Which sense organ helps us hear music?",
            instructions = "Choose the best answer.",
            content = "Match the organ to the sense of hearing.",
            options = listOf("Eyes", "Ears", "Tongue", "Skin"),
            correctAnswer = "Ears",
            explanation = "Our ears help us hear different sounds like music, voices, and bells.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q1_a4",
            lessonId = "g3_q1_lesson_sense_organs",
            type = "multiple_choice",
            prompt = "Which sense organ helps us smell food?",
            instructions = "Choose the best answer.",
            content = "Think about what helps you smell soup or flowers.",
            options = listOf("Nose", "Eyes", "Hands", "Feet"),
            correctAnswer = "Nose",
            explanation = "The nose helps us smell good and bad odors around us.",
            points = 1,
            orderIndex = 2
        ),
        LessonActivity(
            id = "g3_q1_a5",
            lessonId = "g3_q1_lesson_living_things",
            type = "multiple_choice",
            prompt = "Which one is a living thing?",
            instructions = "Choose the best answer.",
            content = "Living things grow and need food and water.",
            options = listOf("Bird", "Chair", "Pencil", "Table"),
            correctAnswer = "Bird",
            explanation = "A bird is a living thing because it grows, eats, breathes, and reproduces.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q1_a6",
            lessonId = "g3_q1_lesson_living_things",
            type = "multiple_choice",
            prompt = "Why is a table non-living?",
            instructions = "Choose the best answer.",
            content = "Think about what living things can do.",
            options = listOf(
                "It cannot grow by itself",
                "It can move fast",
                "It can eat food",
                "It can make seeds"
            ),
            correctAnswer = "It cannot grow by itself",
            explanation = "A table is non-living because it does not grow, breathe, or need food.",
            points = 1,
            orderIndex = 2
        ),
        LessonActivity(
            id = "g3_q1_a7",
            lessonId = "g3_q1_lesson_school_plants",
            type = "multiple_choice",
            prompt = "Which plant is known for folding its leaves when touched?",
            instructions = "Choose the best answer.",
            content = "Think about a plant common in the Philippines.",
            options = listOf("Santan", "Oregano", "Makahiya", "Sunflower"),
            correctAnswer = "Makahiya",
            explanation = "Makahiya folds its leaves when touched, which is why many children remember it easily.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q1_a8",
            lessonId = "g3_q1_lesson_school_plants",
            type = "multiple_choice",
            prompt = "Which plant is often used as a herbal remedy for coughs?",
            instructions = "Choose the best answer.",
            content = "This plant is common in home gardens.",
            options = listOf("Oregano", "Santan", "Makahiya", "Rose"),
            correctAnswer = "Oregano",
            explanation = "Oregano is commonly used in the Philippines as a home remedy for coughs.",
            points = 1,
            orderIndex = 2
        ),
        LessonActivity(
            id = "g3_q2_a1",
            lessonId = "g3_q2_lesson_rocks",
            type = "multiple_choice",
            prompt = "Which one is most likely a rock you can find on the ground?",
            instructions = "Choose the best answer.",
            content = "Think about a hard earth material you may see near roads or gardens.",
            options = listOf("Stone", "Water", "Smoke", "Milk"),
            correctAnswer = "Stone",
            explanation = "A stone is a common rock-like material found on the ground.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q2_a2",
            lessonId = "g3_q2_lesson_rocks",
            type = "short_answer",
            prompt = "Name one visible property of a rock.",
            instructions = "Type a short answer.",
            content = "You may answer with color, size, shape, or texture.",
            correctAnswer = "color",
            explanation = "Rocks can be described by visible properties like color, size, shape, and texture.",
            points = 1,
            orderIndex = 2
        ),
        LessonActivity(
            id = "g3_q2_a3",
            lessonId = "g3_q2_lesson_soil",
            type = "multiple_choice",
            prompt = "Why is soil important for plants?",
            instructions = "Choose the best answer.",
            content = "Think about what plants need from the ground.",
            options = listOf("It helps them grow", "It makes them fly", "It turns into animals", "It makes them sing"),
            correctAnswer = "It helps them grow",
            explanation = "Soil supports plant growth by holding roots, water, and nutrients.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q2_a4",
            lessonId = "g3_q2_lesson_soil",
            type = "multiple_choice",
            prompt = "Where do we often find soil?",
            instructions = "Choose the best answer.",
            content = "Think about a place where plants grow.",
            options = listOf("Garden", "Ceiling", "Television", "Notebook"),
            correctAnswer = "Garden",
            explanation = "Soil is commonly found in gardens, pots, fields, and other places where plants grow.",
            points = 1,
            orderIndex = 2
        ),
        LessonActivity(
            id = "g3_q2_a5",
            lessonId = "g3_q2_lesson_states_of_matter",
            type = "multiple_choice",
            prompt = "Which one is a liquid?",
            instructions = "Choose the best answer.",
            content = "Liquids flow and take the shape of containers.",
            options = listOf("Water", "Table", "Stone", "Chair"),
            correctAnswer = "Water",
            explanation = "Water is a liquid because it flows and takes the shape of a container.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q2_a6",
            lessonId = "g3_q2_lesson_states_of_matter",
            type = "multiple_choice",
            prompt = "Which one is a gas?",
            instructions = "Choose the best answer.",
            content = "Gases spread to fill space.",
            options = listOf("Air", "Bottle", "Spoon", "Stone"),
            correctAnswer = "Air",
            explanation = "Air is a gas that spreads through the space around us.",
            points = 1,
            orderIndex = 2
        ),
        LessonActivity(
            id = "g3_q2_a7",
            lessonId = "g3_q2_lesson_material_uses",
            type = "multiple_choice",
            prompt = "Which material is best for a drinking glass?",
            instructions = "Choose the best answer.",
            content = "Think about a material often used for cups and glasses.",
            options = listOf("Glass", "Soil", "Smoke", "Cloud"),
            correctAnswer = "Glass",
            explanation = "Glass is commonly used for drinking glasses because it is hard and keeps its shape.",
            points = 1,
            orderIndex = 1
        ),
        LessonActivity(
            id = "g3_q2_a8",
            lessonId = "g3_q2_lesson_material_uses",
            type = "multiple_choice",
            prompt = "Why is wood useful for tables or chairs?",
            instructions = "Choose the best answer.",
            content = "Think about the properties of wood.",
            options = listOf("It is strong and keeps shape", "It melts easily", "It flows like water", "It disappears in air"),
            correctAnswer = "It is strong and keeps shape",
            explanation = "Wood is useful for tables and chairs because it is strong and keeps its shape.",
            points = 1,
            orderIndex = 2
        )
    )

    val missions = listOf(
        Mission(
            id = "g3_q1_mission_living_walk",
            title = "Living Things Walk",
            description = "Find one living thing and one non-living thing around you, then compare what makes them different.",
            missionType = "curriculum_scan",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            lessonIds = listOf("g3_q1_lesson_living_things"),
            objectsToFind = listOf("bird", "table"),
            sectionIds = pilotSections,
            releasedSectionIds = pilotSections,
            category = "science",
            requiredCount = 2,
            active = true,
            createdBy = "system",
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-01-01T00:00:00Z"
        ),
        Mission(
            id = "g3_q1_mission_plant_watch",
            title = "Plant Feature Watch",
            description = "Observe two common plants and look closely at the leaves, stem, and flowers you can see.",
            missionType = "curriculum_scan",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            lessonIds = listOf("g3_q1_lesson_school_plants"),
            objectsToFind = listOf("santan", "oregano"),
            sectionIds = pilotSections,
            releasedSectionIds = pilotSections,
            category = "plants",
            requiredCount = 2,
            active = true,
            createdBy = "system",
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-01-01T00:00:00Z"
        ),
        Mission(
            id = "g3_q1_mission_sense_hunt",
            title = "Sense Organ Clue Hunt",
            description = "Use your senses to notice objects around you and connect what you saw, heard, or smelled to the sense organ you used.",
            missionType = "guided_review",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER1_ID,
            lessonIds = listOf("g3_q1_lesson_sense_organs"),
            objectsToFind = listOf("bird", "oregano"),
            sectionIds = pilotSections,
            releasedSectionIds = pilotSections,
            category = "science",
            requiredCount = 2,
            active = true,
            createdBy = "system",
            createdAt = "2026-01-01T00:00:00Z",
            updatedAt = "2026-01-01T00:00:00Z"
        ),
        Mission(
            id = "g3_q2_mission_rock_and_soil_spotting",
            title = "Rock and Soil Spotting",
            description = "Find one rock-like material and one soil-related example, then compare how they look and feel.",
            missionType = "curriculum_scan",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            lessonIds = listOf("g3_q2_lesson_rocks", "g3_q2_lesson_soil"),
            objectsToFind = listOf("rock", "stone", "santan"),
            sectionIds = pilotSections,
            category = "earth_materials",
            requiredCount = 2,
            active = false,
            createdBy = "system",
            createdAt = "2026-04-19T00:00:00Z",
            updatedAt = "2026-04-19T00:00:00Z"
        ),
        Mission(
            id = "g3_q2_mission_matter_state_hunt",
            title = "Matter State Hunt",
            description = "Identify one solid and one liquid in the environment, then explain how they are different.",
            missionType = "curriculum_scan",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            lessonIds = listOf("g3_q2_lesson_states_of_matter"),
            objectsToFind = listOf("table", "bottle"),
            sectionIds = pilotSections,
            category = "matter",
            requiredCount = 2,
            active = false,
            createdBy = "system",
            createdAt = "2026-04-19T00:00:00Z",
            updatedAt = "2026-04-19T00:00:00Z"
        ),
        Mission(
            id = "g3_q2_mission_material_use_check",
            title = "Material Use Check",
            description = "Look for useful materials at school or home and connect each one to one reason it is used.",
            missionType = "guided_review",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER2_ID,
            lessonIds = listOf("g3_q2_lesson_material_uses"),
            objectsToFind = listOf("table", "spoon", "bottle"),
            sectionIds = pilotSections,
            category = "materials",
            requiredCount = 2,
            active = false,
            createdBy = "system",
            createdAt = "2026-04-19T00:00:00Z",
            updatedAt = "2026-04-19T00:00:00Z"
        )
    )
}
