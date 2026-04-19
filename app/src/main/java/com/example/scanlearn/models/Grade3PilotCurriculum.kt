package com.example.scanlearn.models

object Grade3PilotCurriculum {

    const val GRADE_LEVEL = "Grade 3"
    const val QUARTER_ID = "g3_q1"

    val curriculumMaps = listOf(
        CurriculumMap(
            gradeLevel = GRADE_LEVEL,
            title = "Grade 3 Science",
            quarterIds = listOf(QUARTER_ID)
        )
    )

    val quarters = listOf(
        Quarter(
            id = QUARTER_ID,
            gradeLevel = GRADE_LEVEL,
            quarterNumber = 1,
            title = "Body Parts and Our Surroundings",
            description = "A Grade 3 pilot quarter about the body, the senses, and living things found around us.",
            unitIds = listOf("g3_q1_unit_body", "g3_q1_unit_surroundings"),
            orderIndex = 1,
            status = "published"
        )
    )

    val units = listOf(
        Unit(
            id = "g3_q1_unit_body",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
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
            quarterId = QUARTER_ID,
            title = "Living Things Around Us",
            overview = "Students observe things around home and school and identify simple plant examples found in their surroundings.",
            lessonIds = listOf("g3_q1_lesson_living_things", "g3_q1_lesson_school_plants"),
            orderIndex = 2,
            estimatedMinutes = 70,
            status = "published"
        )
    )

    val competencies = listOf(
        Competency(
            id = "g3_q1_comp_body_parts",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
            code = "G3-Q1-C1",
            statement = "Identify the main external body parts and tell what they do.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q1_comp_senses",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
            code = "G3-Q1-C2",
            statement = "Describe the sense organs and match them with what they help us do.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q1_comp_living_nonliving",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
            code = "G3-Q1-C3",
            statement = "Classify common things in the surroundings as living or non-living.",
            masteryThreshold = 75
        ),
        Competency(
            id = "g3_q1_comp_school_plants",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
            code = "G3-Q1-C4",
            statement = "Observe common plants around school or home and describe simple features.",
            masteryThreshold = 75
        )
    )

    val lessons = listOf(
        Lesson(
            id = "g3_q1_lesson_body_parts",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
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
            createdBy = "system"
        ),
        Lesson(
            id = "g3_q1_lesson_sense_organs",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
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
            createdBy = "system"
        ),
        Lesson(
            id = "g3_q1_lesson_living_things",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
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
            createdBy = "system"
        ),
        Lesson(
            id = "g3_q1_lesson_school_plants",
            gradeLevel = GRADE_LEVEL,
            quarterId = QUARTER_ID,
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
        )
    )
}
