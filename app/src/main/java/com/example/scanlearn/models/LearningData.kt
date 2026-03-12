package com.example.scanlearn.models

object LearningData {

    val LEARNING_OBJECTS = listOf(
        LearningObject(
            id = "apple",
            name = "Apple",
            category = "fruit",
            description = "The apple is a sweet, edible fruit produced by an apple tree. Apple trees are cultivated worldwide and are the most widely grown species in the genus Malus.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/800px-Red_Apple.jpg",
            facts = listOf(
                "Apples are members of the rose family.",
                "There are more than 7,500 varieties of apples.",
                "Apples are 25% air, which is why they float.",
                "The science of apple growing is called pomology.",
                "Apple trees can live for more than 100 years."
            ),
            quiz = listOf(
                QuizQuestion(
                    question = "What family does the apple belong to?",
                    options = listOf(
                        "Citrus family",
                        "Rose family",
                        "Berry family",
                        "Melon family"
                    ),
                    correctAnswer = 1
                ),
                QuizQuestion(
                    question = "Approximately how many varieties of apples exist?",
                    options = listOf("500", "1,000", "7,500", "10,000"),
                    correctAnswer = 2
                ),
                QuizQuestion(
                    question = "Why do apples float in water?",
                    options = listOf(
                        "They are very light",
                        "They are 25% air",
                        "They have a waxy coating",
                        "They repel water"
                    ),
                    correctAnswer = 1
                )
            )
        ),
        LearningObject(
            id = "leaf",
            name = "Leaf",
            category = "plant",
            description = "A leaf is a flattened structure of a higher plant, typically green and blade-like, attached to a stem directly or via a stalk. Leaves carry out photosynthesis.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Ficus_leaf.jpg/800px-Ficus_leaf.jpg",
            facts = listOf(
                "Leaves are the primary site of photosynthesis.",
                "The green color comes from chlorophyll.",
                "Leaves have tiny pores called stomata.",
                "Some leaves can live for thousands of years on a plant.",
                "Leaves change color in autumn due to chlorophyll breakdown."
            ),
            quiz = listOf(
                QuizQuestion(
                    question = "What is the primary function of a leaf?",
                    options = listOf(
                        "Water storage",
                        "Photosynthesis",
                        "Reproduction",
                        "Protection"
                    ),
                    correctAnswer = 1
                ),
                QuizQuestion(
                    question = "What gives leaves their green color?",
                    options = listOf("Water", "Sunlight", "Chlorophyll", "Oxygen"),
                    correctAnswer = 2
                ),
                QuizQuestion(
                    question = "What are the tiny pores on leaves called?",
                    options = listOf("Chloroplasts", "Stomata", "Veins", "Cuticle"),
                    correctAnswer = 1
                )
            )
        ),
        LearningObject(
            id = "rock",
            name = "Rock",
            category = "geology",
            description = "A rock is any naturally occurring solid mass or aggregate of minerals or mineraloid matter. Rocks are categorized by the minerals included and how they are formed.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/61/Granite_quarry.jpg/800px-Granite_quarry.jpg",
            facts = listOf(
                "There are three main types of rocks: igneous, sedimentary, and metamorphic.",
                "Rocks are made up of one or more minerals.",
                "The oldest known rocks on Earth are about 4 billion years old.",
                "Rocks form the outer layer of the Earth called the crust.",
                "Diamonds are the hardest natural rock."
            ),
            quiz = listOf(
                QuizQuestion(
                    question = "What are the three main types of rocks?",
                    options = listOf(
                        "Hard, soft, and medium",
                        "Igneous, sedimentary, and metamorphic",
                        "Sand, clay, and granite",
                        "Volcanic, coastal, and mountain"
                    ),
                    correctAnswer = 1
                ),
                QuizQuestion(
                    question = "What is the hardest natural rock?",
                    options = listOf("Granite", "Quartz", "Diamond", "Ruby"),
                    correctAnswer = 2
                ),
                QuizQuestion(
                    question = "How old are the oldest known rocks on Earth?",
                    options = listOf(
                        "1 million years",
                        "100 million years",
                        "1 billion years",
                        "4 billion years"
                    ),
                    correctAnswer = 3
                )
            )
        ),
        LearningObject(
            id = "water",
            name = "Water",
            category = "chemistry",
            description = "Water is an inorganic compound with the chemical formula H₂O. It is a transparent, tasteless, odorless liquid at room temperature and pressure.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b1/Water_drop_on_a_leaf_Luc_Viatour.jpg/800px-Water_drop_on_a_leaf_Luc_Viatour.jpg",
            facts = listOf(
                "Water covers about 71% of the Earth's surface.",
                "The chemical formula for water is H₂O.",
                "Water is the only substance that naturally exists in all three states.",
                "Hot water freezes faster than cold water (Mpemba effect).",
                "Water has a very high surface tension."
            ),
            quiz = listOf(
                QuizQuestion(
                    question = "What percentage of Earth's surface is covered by water?",
                    options = listOf("50%", "60%", "71%", "85%"),
                    correctAnswer = 2
                ),
                QuizQuestion(
                    question = "What is the chemical formula for water?",
                    options = listOf("HO", "H₂O", "H₃O", "HO₂"),
                    correctAnswer = 1
                ),
                QuizQuestion(
                    question = "What phenomenon describes hot water freezing faster than cold?",
                    options = listOf(
                        "Newton's effect",
                        "Mpemba effect",
                        "Thermal paradox",
                        "Frost effect"
                    ),
                    correctAnswer = 1
                )
            )
        ),
        LearningObject(
            id = "sun",
            name = "Sun",
            category = "astronomy",
            description = "The Sun is the star at the center of the Solar System. It is a nearly perfect sphere of hot plasma, heated to incandescence by nuclear fusion reactions in its core.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg/800px-The_Sun_by_the_Atmospheric_Imaging_Assembly_of_NASA%27s_Solar_Dynamics_Observatory_-_20100819.jpg",
            facts = listOf(
                "The Sun accounts for 99.86% of the mass of the Solar System.",
                "It takes about 8 minutes for sunlight to reach Earth.",
                "The Sun's core temperature is about 15 million degrees Celsius.",
                "The Sun is about 4.6 billion years old.",
                "About 1.3 million Earths could fit inside the Sun."
            ),
            quiz = listOf(
                QuizQuestion(
                    question = "How long does it take for sunlight to reach Earth?",
                    options = listOf("1 minute", "4 minutes", "8 minutes", "15 minutes"),
                    correctAnswer = 2
                ),
                QuizQuestion(
                    question = "What percentage of the Solar System's mass is the Sun?",
                    options = listOf("75%", "90%", "99.86%", "100%"),
                    correctAnswer = 2
                ),
                QuizQuestion(
                    question = "How old is the Sun approximately?",
                    options = listOf(
                        "1 billion years",
                        "2.3 billion years",
                        "4.6 billion years",
                        "10 billion years"
                    ),
                    correctAnswer = 2
                )
            )
        )
    )

    val MISSIONS = listOf(
        Mission(
            id = "mission1",
            title = "Nature Explorer",
            description = "Discover the wonders of the natural world around you",
            objectsToFind = listOf("apple", "leaf", "rock"),
            completed = false
        ),
        Mission(
            id = "mission2",
            title = "Science Discovery",
            description = "Explore fundamental science concepts",
            objectsToFind = listOf("water", "sun"),
            completed = false
        ),
        Mission(
            id = "mission3",
            title = "Complete Collection",
            description = "Scan all available learning objects",
            objectsToFind = listOf("apple", "leaf", "rock", "water", "sun"),
            completed = false
        )
    )
}
