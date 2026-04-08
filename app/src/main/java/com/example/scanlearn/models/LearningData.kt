package com.example.scanlearn.models

object LearningData {

    val LABEL_TO_CATEGORY = mapOf(
        "dog" to "animals", "cat" to "animals", "bird" to "animals",
        "frog" to "animals", "butterfly" to "animals", "animal" to "animals",
        "mammal" to "animals", "reptile" to "animals", "fish" to "animals",
        "insect" to "animals", "wildlife" to "animals", "pet" to "animals",
        "horse" to "animals", "cow" to "animals", "rabbit" to "animals",
        "elephant" to "animals", "lion" to "animals", "tiger" to "animals",
        "snake" to "animals", "turtle" to "animals", "deer" to "animals",
        "monkey" to "animals", "bear" to "animals", "duck" to "animals",

        "plant" to "plants", "leaf" to "plants", "flower" to "plants",
        "tree" to "plants", "grass" to "plants", "vegetation" to "plants",
        "rose" to "plants", "sunflower" to "plants", "herb" to "plants",
        "fern" to "plants", "cactus" to "plants", "moss" to "plants",
        "bush" to "plants", "vine" to "plants", "garden" to "plants",
        "nature" to "plants", "botany" to "plants", "petal" to "plants",
        "oregano" to "plants", "santan" to "plants", "makahiya" to "plants",

        "pencil" to "classroom", "pen" to "classroom", "book" to "classroom",
        "bag" to "classroom", "table" to "classroom", "chair" to "classroom",
        "blackboard" to "classroom", "notebook" to "classroom", "ruler" to "classroom",
        "eraser" to "classroom", "scissors" to "classroom", "paper" to "classroom",
        "desk" to "classroom", "board" to "classroom", "chalk" to "classroom",
        "marker" to "classroom", "globe" to "classroom", "calculator" to "classroom",
        "classroom" to "classroom", "school" to "classroom", "backpack" to "classroom",
        "furniture" to "classroom", "seat" to "classroom", "chalkboard" to "classroom"
    )

    val LABEL_TO_SPECIFIC = mapOf(
        "frog" to "frog", "toad" to "frog",
        "bird" to "bird", "sparrow" to "bird", "pigeon" to "bird", "eagle" to "bird",
        "dog" to "dog", "puppy" to "dog", "canine" to "dog",
        "cat" to "cat", "kitten" to "cat", "feline" to "cat",
        "butterfly" to "butterfly", "moth" to "butterfly",

        "sunflower" to "sunflower",
        "rose" to "rose",
        "santan" to "santan", "ixora" to "santan",
        "oregano" to "oregano",
        "makahiya" to "makahiya", "mimosa" to "makahiya", "sensitive plant" to "makahiya",

        "pencil" to "pencil",
        "bag" to "bag", "backpack" to "bag", "schoolbag" to "bag",
        "table" to "table", "desk" to "table",
        "chair" to "chair", "seat" to "chair",
        "blackboard" to "blackboard", "chalkboard" to "blackboard", "board" to "blackboard"
    )

    val SPECIFIC_TEMPLATES = mapOf(
        "frog" to LearningObject(
            id = "frog", name = "Frog", category = "animals",
            description = "Frogs are amphibians known for their jumping ability, bulging eyes, and smooth moist skin. They live both in water and on land and are found on every continent except Antarctica.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Litoria_infrafrenata_-_Julatten.jpg/800px-Litoria_infrafrenata_-_Julatten.jpg",
            facts = listOf(
                "Frogs absorb water through their skin instead of drinking it.",
                "There are over 7,000 known species of frogs.",
                "Frogs are found on every continent except Antarctica.",
                "A group of frogs is called an army.",
                "The glass frog has transparent skin through which organs are visible."
            ),
            quiz = listOf(
                QuizQuestion("How do frogs absorb water?", listOf("By drinking", "Through their skin", "Through their mouth", "Through their eyes"), 1),
                QuizQuestion("How many known species of frogs are there?", listOf("Over 1,000", "Over 3,000", "Over 7,000", "Over 15,000"), 2),
                QuizQuestion("What is a group of frogs called?", listOf("A colony", "A pack", "An army", "A school"), 2)
            )
        ),
        "bird" to LearningObject(
            id = "bird", name = "Bird", category = "animals",
            description = "Birds are warm-blooded vertebrates characterized by feathers, beaks, wings, and the ability to fly. They are found on every continent and play vital roles in ecosystems.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/A_small_cup_of_coffee.JPG/800px-A_small_cup_of_coffee.JPG",
            facts = listOf(
                "There are approximately 10,000 species of birds worldwide.",
                "Birds are the only living descendants of dinosaurs.",
                "Some birds migrate thousands of kilometers each year.",
                "The ostrich is the largest bird and cannot fly.",
                "Birds have hollow bones which help them fly."
            ),
            quiz = listOf(
                QuizQuestion("How many species of birds exist?", listOf("1,000", "5,000", "10,000", "50,000"), 2),
                QuizQuestion("What are birds descended from?", listOf("Reptiles", "Dinosaurs", "Mammals", "Fish"), 1),
                QuizQuestion("Which is the largest bird?", listOf("Eagle", "Penguin", "Ostrich", "Flamingo"), 2)
            )
        ),
        "dog" to LearningObject(
            id = "dog", name = "Dog", category = "animals",
            description = "The dog is a domesticated mammal and one of the most popular pets worldwide. Dogs are known for their loyalty, intelligence, and strong bond with humans.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/26/YellowLabradorLooking_new.jpg/800px-YellowLabradorLooking_new.jpg",
            facts = listOf(
                "Dogs have been domesticated for over 15,000 years.",
                "A dog's nose print is unique, like a human fingerprint.",
                "Dogs can understand up to 250 words and gestures.",
                "The average dog has the intelligence of a 2-year-old child.",
                "Dogs have a sense of smell 10,000 to 100,000 times stronger than humans."
            ),
            quiz = listOf(
                QuizQuestion("How long have dogs been domesticated?", listOf("1,000 years", "5,000 years", "15,000 years", "100,000 years"), 2),
                QuizQuestion("What makes a dog's nose unique?", listOf("Its color", "Its size", "Its print", "Its wetness"), 2),
                QuizQuestion("How many words can a dog understand?", listOf("Up to 50", "Up to 100", "Up to 250", "Up to 500"), 2)
            )
        ),
        "cat" to LearningObject(
            id = "cat", name = "Cat", category = "animals",
            description = "The cat is a small carnivorous mammal and one of the most popular household pets. Cats are known for their independence, agility, and keen hunting instincts.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Cat_November_2010-1a.jpg/800px-Cat_November_2010-1a.jpg",
            facts = listOf(
                "Cats sleep 12 to 16 hours a day.",
                "A group of cats is called a clowder.",
                "Cats have 32 muscles in each ear.",
                "Cats can jump up to six times their own height.",
                "A cat's purr vibrates at a frequency that can promote bone healing."
            ),
            quiz = listOf(
                QuizQuestion("How many hours do cats sleep daily?", listOf("4-6 hours", "8-10 hours", "12-16 hours", "20-22 hours"), 2),
                QuizQuestion("What is a group of cats called?", listOf("A pack", "A clowder", "A herd", "A flock"), 1),
                QuizQuestion("How many muscles does each cat ear have?", listOf("8", "16", "32", "64"), 2)
            )
        ),
        "butterfly" to LearningObject(
            id = "butterfly", name = "Butterfly", category = "animals",
            description = "Butterflies are insects with large colorful wings. They undergo a remarkable transformation called metamorphosis, going from caterpillar to chrysalis to butterfly.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Monarch_Butterfly_Danaus_plexippus_Feeding_Down.jpg/800px-Monarch_Butterfly_Danaus_plexippus_Feeding_Down.jpg",
            facts = listOf(
                "Butterflies taste with their feet.",
                "A butterfly's life cycle has four stages: egg, larva, pupa, adult.",
                "Butterflies can see ultraviolet light.",
                "The monarch butterfly migrates up to 4,800 km.",
                "Butterflies have two compound eyes made of thousands of lenses."
            ),
            quiz = listOf(
                QuizQuestion("How do butterflies taste?", listOf("With their mouth", "With their antennae", "With their feet", "With their wings"), 2),
                QuizQuestion("How many stages does a butterfly life cycle have?", listOf("2", "3", "4", "5"), 2),
                QuizQuestion("How far can a monarch butterfly migrate?", listOf("100 km", "1,000 km", "2,400 km", "4,800 km"), 3)
            )
        ),

        "sunflower" to LearningObject(
            id = "sunflower", name = "Sunflower", category = "plants",
            description = "The sunflower is a tall, bright flowering plant native to North America. It is known for its large yellow flower head that tracks the movement of the sun when young.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Sunflower_sky_backdrop.jpg/800px-Sunflower_sky_backdrop.jpg",
            facts = listOf(
                "Sunflowers can grow up to 3 meters tall.",
                "Young sunflowers track the sun from east to west each day.",
                "A sunflower head is made up of up to 2,000 tiny flowers.",
                "Sunflower seeds are a rich source of Vitamin E.",
                "Sunflowers can absorb toxins and even radiation from soil."
            ),
            quiz = listOf(
                QuizQuestion("How tall can sunflowers grow?", listOf("1 meter", "2 meters", "3 meters", "5 meters"), 2),
                QuizQuestion("What tracks the sun daily in young sunflowers?", listOf("Its roots", "Its stem", "Its flower head", "Its leaves"), 2),
                QuizQuestion("How many tiny flowers make up one sunflower head?", listOf("Up to 500", "Up to 1,000", "Up to 2,000", "Up to 5,000"), 2)
            )
        ),
        "rose" to LearningObject(
            id = "rose", name = "Rose", category = "plants",
            description = "The rose is one of the most beloved flowering plants in the world. Known for its beauty and fragrance, roses have been cultivated for thousands of years and symbolize love and beauty.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Collage_of_Nine_Flowers.jpg/800px-Collage_of_Nine_Flowers.jpg",
            facts = listOf(
                "Roses have been cultivated for over 5,000 years.",
                "There are over 300 species of roses.",
                "Rose hips are rich in Vitamin C.",
                "The rose is the national flower of England and the USA.",
                "Roses belong to the same family as apples and strawberries."
            ),
            quiz = listOf(
                QuizQuestion("How long have roses been cultivated?", listOf("500 years", "1,000 years", "3,000 years", "5,000 years"), 3),
                QuizQuestion("What vitamin are rose hips rich in?", listOf("Vitamin A", "Vitamin B", "Vitamin C", "Vitamin D"), 2),
                QuizQuestion("What family do roses belong to?", listOf("Citrus family", "Same family as apples", "Orchid family", "Cactus family"), 1)
            )
        ),
        "santan" to LearningObject(
            id = "santan", name = "Santan", category = "plants",
            description = "Santan (Ixora coccinea) is a tropical flowering shrub widely grown in the Philippines and Southeast Asia. It is commonly used as an ornamental plant in gardens and hedges.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Collage_of_Nine_Flowers.jpg/800px-Collage_of_Nine_Flowers.jpg",
            facts = listOf(
                "Santan is also known as Ixora or jungle flame.",
                "It blooms in clusters of small red, orange, pink, or white flowers.",
                "Santan is native to South and Southeast Asia.",
                "In the Philippines, children often suck nectar from the flowers.",
                "Santan is used in traditional medicine for fever and skin conditions."
            ),
            quiz = listOf(
                QuizQuestion("What is another name for the Santan plant?", listOf("Sampaguita", "Ixora", "Ylang-ylang", "Gumamela"), 1),
                QuizQuestion("Where is Santan originally native to?", listOf("South America", "Africa", "South and Southeast Asia", "Europe"), 2),
                QuizQuestion("What is Santan traditionally used for in medicine?", listOf("Headache and cough", "Fever and skin conditions", "Bone and joint pain", "Eye infections"), 1)
            )
        ),
        "oregano" to LearningObject(
            id = "oregano", name = "Oregano", category = "plants",
            description = "Oregano (Origanum vulgare) is an aromatic herb used widely in cooking and traditional medicine. In the Philippines, oregano is a popular herbal remedy for coughs and colds.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Collage_of_Nine_Flowers.jpg/800px-Collage_of_Nine_Flowers.jpg",
            facts = listOf(
                "Oregano contains antioxidants and antibacterial compounds.",
                "It has been used medicinally for over 2,500 years.",
                "Oregano is a key ingredient in Italian and Greek cuisines.",
                "In the Philippines, oregano leaves are boiled to treat coughs.",
                "Oregano oil is one of the most potent natural antibiotics known."
            ),
            quiz = listOf(
                QuizQuestion("What is oregano commonly used for in the Philippines?", listOf("Skin rash", "Coughs and colds", "Fever", "Toothache"), 1),
                QuizQuestion("How long has oregano been used medicinally?", listOf("500 years", "1,000 years", "2,500 years", "5,000 years"), 2),
                QuizQuestion("What makes oregano oil notable?", listOf("Its fragrance", "Its color", "Its antibiotic properties", "Its sweetness"), 2)
            )
        ),
        "makahiya" to LearningObject(
            id = "makahiya", name = "Makahiya", category = "plants",
            description = "Makahiya (Mimosa pudica) is a creeping herb famous for its unique ability to fold its leaves when touched. Its name means 'shy' in Filipino, referring to this sensitive behavior.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Collage_of_Nine_Flowers.jpg/800px-Collage_of_Nine_Flowers.jpg",
            facts = listOf(
                "Makahiya folds its leaves when touched as a defense mechanism.",
                "Its scientific name is Mimosa pudica.",
                "The leaf movement is caused by rapid water loss in specialized cells.",
                "Makahiya produces small pink or purple fluffy flowers.",
                "In herbal medicine, it is used for wounds and skin infections."
            ),
            quiz = listOf(
                QuizQuestion("Why does Makahiya fold its leaves when touched?", listOf("To absorb sunlight", "As a defense mechanism", "To store water", "To attract insects"), 1),
                QuizQuestion("What is the scientific name of Makahiya?", listOf("Mimosa pudica", "Ixora coccinea", "Rosa sinensis", "Ficus elastica"), 0),
                QuizQuestion("What causes Makahiya leaves to move?", listOf("Wind response", "Rapid water loss in cells", "Muscle-like fibers", "Chemical signals from roots"), 1)
            )
        ),

        "pencil" to LearningObject(
            id = "pencil", name = "Pencil", category = "classroom",
            description = "A pencil is a writing instrument made with a graphite core encased in wood. Pencils are one of the most widely used tools for writing and drawing in classrooms worldwide.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
            facts = listOf(
                "Pencil cores are made of graphite mixed with clay, not lead.",
                "The average pencil can draw a line 56 km long.",
                "A pencil can write approximately 45,000 words.",
                "Pencils were first used in the 1560s after graphite was discovered.",
                "Astronauts originally used pencils in space before the space pen was invented."
            ),
            quiz = listOf(
                QuizQuestion("What is a pencil core made of?", listOf("Lead", "Charcoal", "Graphite and clay", "Carbon"), 2),
                QuizQuestion("How long a line can an average pencil draw?", listOf("1 km", "10 km", "56 km", "100 km"), 2),
                QuizQuestion("When were pencils first used?", listOf("1200s", "1400s", "1560s", "1700s"), 2)
            )
        ),
        "bag" to LearningObject(
            id = "bag", name = "School Bag", category = "classroom",
            description = "A school bag or backpack is used by students to carry books, notebooks, and supplies to school. It is one of the most essential tools for a student's daily learning.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
            facts = listOf(
                "Backpacks were first used by soldiers to carry supplies.",
                "The word 'backpack' was first used in the USA in the 1910s.",
                "Students should not carry more than 10-15% of their body weight in a bag.",
                "Ergonomic school bags are designed to protect the spine.",
                "The first school bags were simple cloth sacks tied with string."
            ),
            quiz = listOf(
                QuizQuestion("Who first used backpacks?", listOf("Students", "Explorers", "Soldiers", "Merchants"), 2),
                QuizQuestion("How much weight should a student's bag contain?", listOf("5% of body weight", "10-15% of body weight", "25% of body weight", "50% of body weight"), 1),
                QuizQuestion("What are ergonomic bags designed to protect?", listOf("The arms", "The spine", "The knees", "The neck"), 1)
            )
        ),
        "table" to LearningObject(
            id = "table", name = "Table", category = "classroom",
            description = "A table is a piece of furniture with a flat top surface supported by legs. In classrooms, tables are used for writing, reading, experiments, and group activities.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
            facts = listOf(
                "Tables have been used since ancient Egypt around 2500 BC.",
                "The word 'table' comes from the Latin word 'tabula' meaning board.",
                "Round tables have no head seat, symbolizing equality.",
                "A standard classroom table is about 75 cm high.",
                "Tables come in hundreds of designs suited for different purposes."
            ),
            quiz = listOf(
                QuizQuestion("How long have tables been used by humans?", listOf("Since 500 BC", "Since 1000 BC", "Since 2500 BC", "Since 5000 BC"), 2),
                QuizQuestion("What does the Latin word 'tabula' mean?", listOf("Flat surface", "Board", "Furniture", "Stand"), 1),
                QuizQuestion("What does a round table symbolize?", listOf("Power", "Royalty", "Equality", "Knowledge"), 2)
            )
        ),
        "chair" to LearningObject(
            id = "chair", name = "Chair", category = "classroom",
            description = "A chair is a piece of furniture designed for sitting, with a back support and four legs. Chairs are essential in classrooms for comfortable learning.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
            facts = listOf(
                "Chairs have existed for thousands of years, first used by ancient Egyptians.",
                "The average person spends about 8-10 hours per day sitting.",
                "Ergonomic chairs are designed to support proper posture.",
                "The first chairs were symbols of authority and power.",
                "Sitting up straight in a chair helps improve concentration."
            ),
            quiz = listOf(
                QuizQuestion("Who first used chairs as symbols of authority?", listOf("Romans", "Greeks", "Ancient Egyptians", "Chinese"), 2),
                QuizQuestion("How many hours does the average person sit daily?", listOf("2-4 hours", "4-6 hours", "8-10 hours", "12-14 hours"), 2),
                QuizQuestion("What are ergonomic chairs designed for?", listOf("Style", "Durability", "Proper posture", "Easy storage"), 2)
            )
        ),
        "blackboard" to LearningObject(
            id = "blackboard", name = "Blackboard", category = "classroom",
            description = "A blackboard is a smooth dark surface used for writing with chalk in classrooms. It is one of the oldest and most recognizable teaching tools in education history.",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
            facts = listOf(
                "Blackboards were first used in schools in the early 1800s.",
                "James Pillans invented the large blackboard for classroom use.",
                "Modern boards are often green, which reduces eye strain.",
                "The squeaking of chalk on a blackboard can reach 3,000 Hz.",
                "Whiteboards replaced blackboards in many modern classrooms."
            ),
            quiz = listOf(
                QuizQuestion("When were blackboards first used in schools?", listOf("1600s", "Early 1800s", "Late 1900s", "1700s"), 1),
                QuizQuestion("Why are many modern boards green?", listOf("It looks better", "It reduces eye strain", "Chalk shows better", "It is cheaper"), 1),
                QuizQuestion("What replaced blackboards in many modern classrooms?", listOf("Projectors", "Tablets", "Whiteboards", "Smartboards"), 2)
            )
        )
    )

    val RANDOM_POOL_BY_CATEGORY = mapOf(
        "animals" to listOf(
            LearningObject(
                id = "animal_general_1", name = "Wild Animal", category = "animals",
                description = "Wild animals are species that live in natural habitats without human domestication. They play crucial roles in maintaining ecological balance and biodiversity.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/26/YellowLabradorLooking_new.jpg/800px-YellowLabradorLooking_new.jpg",
                facts = listOf("There are over 8.7 million animal species on Earth.", "Animals make up the kingdom Animalia.", "All animals are multicellular organisms.", "Animals obtain energy by consuming other organisms.", "The blue whale is the largest animal ever known to exist."),
                quiz = listOf(QuizQuestion("How many animal species exist on Earth?", listOf("1 million", "5 million", "8.7 million", "20 million"), 2), QuizQuestion("What is the largest animal ever known?", listOf("Elephant", "Dinosaur", "Blue whale", "Giant squid"), 2), QuizQuestion("How do animals obtain energy?", listOf("From sunlight", "From water", "By consuming other organisms", "From air"), 2))
            ),
            LearningObject(
                id = "animal_general_2", name = "Mammal", category = "animals",
                description = "Mammals are warm-blooded vertebrates that give birth to live young and nurse them with milk. They are one of the most successful groups of animals on Earth.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Cat_November_2010-1a.jpg/800px-Cat_November_2010-1a.jpg",
                facts = listOf("There are about 5,500 species of mammals.", "Mammals are warm-blooded animals.", "Most mammals give birth to live young.", "Mammals nurse their young with milk.", "The bat is the only mammal capable of true flight."),
                quiz = listOf(QuizQuestion("How many mammal species exist?", listOf("1,000", "3,000", "5,500", "10,000"), 2), QuizQuestion("What unique thing can bats do?", listOf("Breathe underwater", "True flight", "See in color", "Live 100 years"), 1), QuizQuestion("How do mammals feed their young?", listOf("With honey", "With milk", "With insects", "With berries"), 1))
            ),
            LearningObject(
                id = "animal_general_3", name = "Reptile", category = "animals",
                description = "Reptiles are cold-blooded vertebrates covered in scales. They include lizards, snakes, turtles, and crocodiles, and were dominant life forms during the age of dinosaurs.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Litoria_infrafrenata_-_Julatten.jpg/800px-Litoria_infrafrenata_-_Julatten.jpg",
                facts = listOf("Reptiles are cold-blooded and rely on the sun for warmth.", "There are over 10,000 species of reptiles.", "The Komodo dragon is the world's largest lizard.", "Crocodiles have remained unchanged for 200 million years.", "Most reptiles lay eggs."),
                quiz = listOf(QuizQuestion("How do reptiles regulate body temperature?", listOf("By sweating", "By shivering", "By basking in the sun", "By breathing faster"), 2), QuizQuestion("What is the world's largest lizard?", listOf("Monitor lizard", "Iguana", "Komodo dragon", "Chameleon"), 2), QuizQuestion("How many reptile species exist?", listOf("2,000", "5,000", "Over 10,000", "Over 20,000"), 2))
            ),
            LearningObject(
                id = "animal_general_4", name = "Insect", category = "animals",
                description = "Insects are the most diverse group of animals on Earth, with over a million described species. They have six legs, three body parts, and most have wings.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Monarch_Butterfly_Danaus_plexippus_Feeding_Down.jpg/800px-Monarch_Butterfly_Danaus_plexippus_Feeding_Down.jpg",
                facts = listOf("There are over 1 million described insect species.", "Insects have three body parts: head, thorax, abdomen.", "Insects make up 80% of all animal species.", "Ants can carry 50 times their own body weight.", "Bees communicate through a waggle dance."),
                quiz = listOf(QuizQuestion("How many insect species have been described?", listOf("100,000", "500,000", "Over 1 million", "Over 5 million"), 2), QuizQuestion("What are the three body parts of an insect?", listOf("Head, body, tail", "Head, thorax, abdomen", "Head, wings, legs", "Eyes, mouth, legs"), 1), QuizQuestion("How much weight can an ant carry?", listOf("5 times its weight", "20 times its weight", "50 times its weight", "100 times its weight"), 2))
            ),
            LearningObject(
                id = "animal_general_5", name = "Fish", category = "animals",
                description = "Fish are aquatic vertebrates that breathe through gills and are typically covered with scales. They are the most diverse group of vertebrates on Earth.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Goldfish_at_3.jpg/800px-Goldfish_at_3.jpg",
                facts = listOf("There are over 33,000 known species of fish.", "Fish have been on Earth for over 500 million years.", "The whale shark is the largest fish in the world.", "Fish breathe through organs called gills.", "Some fish can generate electricity to stun prey."),
                quiz = listOf(QuizQuestion("How many known species of fish exist?", listOf("Over 5,000", "Over 15,000", "Over 33,000", "Over 100,000"), 2), QuizQuestion("What organ do fish use to breathe?", listOf("Lungs", "Skin", "Gills", "Nose"), 2), QuizQuestion("What is the largest fish in the world?", listOf("Great white shark", "Tuna", "Whale shark", "Swordfish"), 2))
            )
        ),
        "plants" to listOf(
            LearningObject(
                id = "plant_general_1", name = "Flowering Plant", category = "plants",
                description = "Flowering plants, or angiosperms, are the most diverse group of plants on Earth. They produce flowers and fruits and make up about 90% of all plant species.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Collage_of_Nine_Flowers.jpg/800px-Collage_of_Nine_Flowers.jpg",
                facts = listOf("Flowering plants make up 90% of all plant species.", "There are over 300,000 species of flowering plants.", "Flowers evolved to attract pollinators.", "Fruits develop from the flower's ovary after fertilization.", "The Rafflesia is the world's largest individual flower."),
                quiz = listOf(QuizQuestion("What percentage of plants are flowering plants?", listOf("50%", "70%", "90%", "100%"), 2), QuizQuestion("How many species of flowering plants exist?", listOf("Over 50,000", "Over 100,000", "Over 300,000", "Over 1 million"), 2), QuizQuestion("What is the world's largest individual flower?", listOf("Sunflower", "Titan arum", "Rafflesia", "Water lily"), 2))
            ),
            LearningObject(
                id = "plant_general_2", name = "Tree", category = "plants",
                description = "Trees are large woody plants with a single main stem called a trunk. They are among the longest-living organisms on Earth and provide essential oxygen and habitat.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Ficus_leaf.jpg/800px-Ficus_leaf.jpg",
                facts = listOf("There are approximately 3 trillion trees on Earth.", "The oldest known tree is over 5,000 years old.", "Trees absorb CO₂ and release oxygen through photosynthesis.", "A single tree can absorb up to 22 kg of CO₂ per year.", "The Amazon rainforest produces 20% of the world's oxygen."),
                quiz = listOf(QuizQuestion("How many trees are on Earth?", listOf("1 billion", "100 billion", "3 trillion", "10 trillion"), 2), QuizQuestion("How much CO₂ can one tree absorb per year?", listOf("1 kg", "5 kg", "22 kg", "100 kg"), 2), QuizQuestion("What percentage of oxygen does the Amazon produce?", listOf("5%", "10%", "20%", "50%"), 2))
            ),
            LearningObject(
                id = "plant_general_3", name = "Herb", category = "plants",
                description = "Herbs are plants with aromatic or medicinal properties. They have been used for thousands of years in cooking, traditional medicine, and religious rituals worldwide.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/40/Sunflower_sky_backdrop.jpg/800px-Sunflower_sky_backdrop.jpg",
                facts = listOf("Herbs have been used medicinally for over 60,000 years.", "About 80% of the world's population relies on herbal medicine.", "Lavender is one of the most widely used herbs globally.", "Many modern medicines were originally derived from herbs.", "The Philippines has over 1,500 identified medicinal plants."),
                quiz = listOf(QuizQuestion("How long have herbs been used medicinally?", listOf("5,000 years", "20,000 years", "Over 60,000 years", "100,000 years"), 2), QuizQuestion("What percentage of people rely on herbal medicine?", listOf("20%", "40%", "60%", "80%"), 3), QuizQuestion("How many medicinal plants does the Philippines have?", listOf("Over 200", "Over 500", "Over 1,500", "Over 5,000"), 2))
            ),
            LearningObject(
                id = "plant_general_4", name = "Grass", category = "plants",
                description = "Grasses are the most widespread plants on Earth, covering about 40% of the land surface. They include rice, wheat, corn, and bamboo among thousands of species.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b1/Bamboo_shoot.jpg/800px-Bamboo_shoot.jpg",
                facts = listOf("Grasses cover about 40% of Earth's land surface.", "Rice, wheat, and corn are all types of grass.", "There are approximately 10,000 species of grasses.", "Grasses were crucial to the development of human civilization.", "Bamboo is the fastest-growing grass, growing up to 91 cm per day."),
                quiz = listOf(QuizQuestion("What percentage of Earth's land do grasses cover?", listOf("10%", "20%", "40%", "60%"), 2), QuizQuestion("Which of these is a type of grass?", listOf("Potato", "Rice", "Tomato", "Mango"), 1), QuizQuestion("How many species of grass exist?", listOf("1,000", "5,000", "10,000", "50,000"), 2))
            ),
            LearningObject(
                id = "plant_general_5", name = "Fern", category = "plants",
                description = "Ferns are ancient plants that have existed for over 360 million years. They do not produce flowers or seeds but reproduce through spores found on the underside of their leaves.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Ficus_leaf.jpg/800px-Ficus_leaf.jpg",
                facts = listOf("Ferns are over 360 million years old.", "Ferns reproduce using spores, not seeds.", "There are about 10,500 known species of ferns.", "Ferns were among the first plants to colonize land.", "Some ferns can grow up to 10 meters tall in tropical rainforests."),
                quiz = listOf(QuizQuestion("How old are ferns as a plant group?", listOf("100 million years", "200 million years", "360 million years", "500 million years"), 2), QuizQuestion("How do ferns reproduce?", listOf("Seeds", "Flowers", "Spores", "Bulbs"), 2), QuizQuestion("How many fern species exist?", listOf("1,000", "5,000", "10,500", "20,000"), 2))
            )
        ),
        "classroom" to listOf(
            LearningObject(
                id = "classroom_general_1", name = "Writing Tool", category = "classroom",
                description = "Writing tools are instruments used to create text or drawings on a surface. From ancient reed pens to modern ballpoint pens, they have been central to human communication.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
                facts = listOf("The first writing tools were reeds and bones used 30,000 years ago.", "The ballpoint pen was invented by László Bíró in 1938.", "Fountain pens use capillary action to deliver ink.", "The average person uses about 100 pens per year.", "Quill pens made from bird feathers were used for 1,400 years."),
                quiz = listOf(QuizQuestion("Who invented the ballpoint pen?", listOf("Thomas Edison", "László Bíró", "Albert Einstein", "James Watt"), 1), QuizQuestion("How long were quill pens used?", listOf("100 years", "500 years", "1,400 years", "3,000 years"), 2), QuizQuestion("How many pens does an average person use per year?", listOf("10", "50", "100", "500"), 2))
            ),
            LearningObject(
                id = "classroom_general_2", name = "Book", category = "classroom",
                description = "A book is a collection of written or printed pages bound together. Books are one of humanity's most important inventions for preserving and sharing knowledge across generations.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
                facts = listOf("The first printed book was the Gutenberg Bible in 1455.", "The Library of Congress has over 170 million items.", "Reading for 6 minutes can reduce stress by 68%.", "Over 2 million books are published worldwide each year.", "The average person reads about 200-400 words per minute."),
                quiz = listOf(QuizQuestion("What was the first printed book?", listOf("The Iliad", "The Gutenberg Bible", "Don Quixote", "The Odyssey"), 1), QuizQuestion("How many minutes of reading reduces stress by 68%?", listOf("2 minutes", "6 minutes", "15 minutes", "30 minutes"), 1), QuizQuestion("How many books are published worldwide each year?", listOf("100,000", "500,000", "Over 2 million", "Over 10 million"), 2))
            ),
            LearningObject(
                id = "classroom_general_3", name = "Ruler", category = "classroom",
                description = "A ruler is a flat measuring tool used in classrooms to measure length and draw straight lines. It is one of the most basic and essential tools in education.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
                facts = listOf("The standard ruler is 30 cm or 12 inches long.", "Rulers date back to ancient civilizations.", "The metric system uses centimeters; imperial uses inches.", "Architects use specialized rulers called scales.", "The first rulers were made from ivory, wood, or metal."),
                quiz = listOf(QuizQuestion("How long is a standard ruler?", listOf("15 cm", "20 cm", "30 cm", "45 cm"), 2), QuizQuestion("What do architects use to draw blueprints?", listOf("A protractor", "A compass", "A scale ruler", "A T-square"), 2), QuizQuestion("What were the first rulers made from?", listOf("Plastic", "Ivory, wood, or metal", "Stone", "Bamboo"), 1))
            ),
            LearningObject(
                id = "classroom_general_4", name = "Scissors", category = "classroom",
                description = "Scissors are a cutting instrument with two blades joined at a pivot point. They are essential classroom tools used for cutting paper, fabric, and other craft materials.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Writing_with_a_pen.jpg/800px-Writing_with_a_pen.jpg",
                facts = listOf("Scissors have been used for nearly 4,000 years.", "The first scissors were made from a single piece of bronze.", "Leonardo da Vinci is credited with improving scissors design.", "Left-handed scissors are specially designed for left-handed users.", "The pivot point connecting the blades is called the fulcrum."),
                quiz = listOf(QuizQuestion("How long have scissors been in use?", listOf("500 years", "1,000 years", "2,000 years", "Nearly 4,000 years"), 3), QuizQuestion("What were the first scissors made from?", listOf("Iron", "Steel", "Bronze", "Wood"), 2), QuizQuestion("What is the pivot point of scissors called?", listOf("Hinge", "Joint", "Fulcrum", "Pivot"), 2))
            ),
            LearningObject(
                id = "classroom_general_5", name = "Globe", category = "classroom",
                description = "A globe is a spherical model of Earth used in classrooms to teach geography. It accurately represents the shape and position of continents, countries, and oceans.",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/USGS_globes.jpg/800px-USGS_globes.jpg",
                facts = listOf("The first known globe was made by Martin Behaim in 1492.", "A globe is the only map that does not distort size or shape.", "Earth is slightly flattened at the poles.", "About 71% of Earth's surface is water.", "The imaginary line around Earth's middle is called the equator."),
                quiz = listOf(QuizQuestion("Who made the first known globe?", listOf("Columbus", "Martin Behaim", "Galileo", "Newton"), 1), QuizQuestion("What percentage of Earth is covered by water?", listOf("50%", "60%", "71%", "85%"), 2), QuizQuestion("What is the line around Earth's middle called?", listOf("Prime Meridian", "Tropic of Cancer", "Equator", "Latitude"), 2))
            )
        )
    )

    val LEARNING_OBJECTS: List<LearningObject> get() = SPECIFIC_TEMPLATES.values.toList()

    val MISSIONS = listOf(
        Mission(id = "mission1", title = "Nature Explorer", description = "Discover the wonders of the natural world around you", objectsToFind = listOf("frog", "bird", "rose")),
        Mission(id = "mission2", title = "Filipino Plants", description = "Learn about plants found in the Philippines", objectsToFind = listOf("santan", "oregano", "makahiya")),
        Mission(id = "mission3", title = "Classroom Champion", description = "Identify all classroom objects around you", objectsToFind = listOf("pencil", "bag", "table", "chair", "blackboard"))
    )

    fun detectCategoryAndSpecific(mlKitLabels: List<String>): Pair<String?, String?> {
        var detectedCategory: String? = null
        var detectedSpecific: String? = null

        for (label in mlKitLabels) {
            val lower = label.lowercase()

            if (detectedSpecific == null) {
                for ((keyword, specificId) in LABEL_TO_SPECIFIC) {
                    if (lower.contains(keyword) || keyword.contains(lower)) {
                        detectedSpecific = specificId
                        break
                    }
                }
            }

            if (detectedCategory == null) {
                for ((keyword, category) in LABEL_TO_CATEGORY) {
                    if (lower.contains(keyword) || keyword.contains(lower)) {
                        detectedCategory = category
                        break
                    }
                }
            }

            if (detectedCategory != null && detectedSpecific != null) break
        }

        return Pair(detectedCategory, detectedSpecific)
    }

    fun getTemplateForDetection(mlKitLabels: List<String>): Pair<String?, LearningObject?> {
        val (category, specificId) = detectCategoryAndSpecific(mlKitLabels)

        val template = when {
            specificId != null -> SPECIFIC_TEMPLATES[specificId]
            category != null -> RANDOM_POOL_BY_CATEGORY[category]?.random()
            else -> null
        }

        return Pair(category, template)
    }
}