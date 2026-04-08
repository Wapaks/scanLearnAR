package com.example.scanlearn.utils

import android.graphics.Bitmap
import com.example.scanlearn.models.ClassificationResult
import com.example.scanlearn.models.DetectionLabel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ObjectClassifier {

    var lastDetectedLabels: List<DetectionLabel> = emptyList()
        private set

    val labelToSpecificId = mapOf(
        "dog" to "dog", "puppy" to "dog", "canine" to "dog", "labrador" to "dog",
        "poodle" to "dog", "bulldog" to "dog", "golden retriever" to "dog",
        "cat" to "cat", "kitten" to "cat", "feline" to "cat", "kitty" to "cat",
        "tabby" to "cat", "persian cat" to "cat",
        "bird" to "bird", "sparrow" to "bird", "pigeon" to "bird", "eagle" to "bird",
        "parrot" to "bird", "crow" to "bird", "hen" to "bird", "rooster" to "bird",
        "frog" to "frog", "toad" to "frog", "tadpole" to "frog",
        "butterfly" to "butterfly", "monarch butterfly" to "butterfly",
        "sunflower" to "sunflower", "helianthus" to "sunflower",
        "yellow flower" to "sunflower", "petal" to "sunflower",
        "rose" to "rose", "red rose" to "rose", "pink rose" to "rose",
        "santan" to "santan", "ixora" to "santan", "jungle flame" to "santan",
        "oregano" to "oregano", "herb leaf" to "oregano",
        "makahiya" to "makahiya", "mimosa" to "makahiya",
        "sensitive plant" to "makahiya", "touch me not" to "makahiya",
        "pencil" to "pencil", "graphite pencil" to "pencil", "colored pencil" to "pencil",
        "bag" to "bag", "backpack" to "bag", "school bag" to "bag", "rucksack" to "bag",
        "table" to "table", "desk" to "table", "wooden table" to "table",
        "chair" to "chair", "seat" to "chair", "stool" to "chair",
        "blackboard" to "blackboard", "chalkboard" to "blackboard"
    )

    val labelToGeneralName = mapOf(
        "wildlife" to "Wild Animal", "wild animal" to "Wild Animal",
        "zoo" to "Wild Animal", "safari" to "Wild Animal",
        "mammal" to "Mammal", "wolf" to "Mammal", "fox" to "Mammal",
        "bear" to "Mammal", "lion" to "Mammal", "tiger" to "Mammal",
        "elephant" to "Mammal", "horse" to "Mammal", "cow" to "Mammal",
        "deer" to "Mammal", "rabbit" to "Mammal", "monkey" to "Mammal",
        "gorilla" to "Mammal", "cheetah" to "Mammal", "leopard" to "Mammal",
        "zebra" to "Mammal", "giraffe" to "Mammal", "hippo" to "Mammal",
        "pig" to "Mammal", "sheep" to "Mammal", "goat" to "Mammal",
        "whale" to "Mammal", "dolphin" to "Mammal", "seal" to "Mammal",
        "reptile" to "Reptile", "snake" to "Reptile", "lizard" to "Reptile",
        "gecko" to "Reptile", "chameleon" to "Reptile", "iguana" to "Reptile",
        "crocodile" to "Reptile", "alligator" to "Reptile", "turtle" to "Reptile",
        "tortoise" to "Reptile", "komodo" to "Reptile", "cobra" to "Reptile",
        "python" to "Reptile", "viper" to "Reptile",
        "insect" to "Insect", "ant" to "Insect", "bee" to "Insect",
        "wasp" to "Insect", "beetle" to "Insect", "grasshopper" to "Insect",
        "cockroach" to "Insect", "dragonfly" to "Insect", "mosquito" to "Insect",
        "ladybug" to "Insect", "firefly" to "Insect", "caterpillar" to "Insect",
        "spider" to "Insect", "moth" to "Insect", "fly" to "Insect",
        "fish" to "Fish", "shark" to "Fish", "salmon" to "Fish",
        "Food" to "Fish", "clownfish" to "Fish", "goldfish" to "Fish",
        "tilapia" to "Fish", "catfish" to "Fish", "swordfish" to "Fish",
        "trout" to "Fish", "eel" to "Fish", "stingray" to "Fish",
        "seahorse" to "Fish", "piranha" to "Fish", "aquatic animal" to "Fish",
        "marine animal" to "Fish", "marine life" to "Fish", "aquatic" to "Fish",
        "flowering plant" to "Flowering Plant", "blossom" to "Flowering Plant",
        "wildflower" to "Flowering Plant", "orchid" to "Flowering Plant",
        "tulip" to "Flowering Plant", "daisy" to "Flowering Plant",
        "lily" to "Flowering Plant", "jasmine" to "Flowering Plant",
        "hibiscus" to "Flowering Plant", "gumamela" to "Flowering Plant",
        "carnation" to "Flowering Plant", "lavender" to "Flowering Plant",
        "flower" to "Flowering Plant", "floral" to "Flowering Plant",
        "tree" to "Tree", "oak" to "Tree", "pine" to "Tree",
        "palm" to "Tree", "mango tree" to "Tree", "coconut tree" to "Tree",
        "acacia" to "Tree", "banyan" to "Tree", "willow" to "Tree",
        "trunk" to "Tree", "bark" to "Tree", "branch" to "Tree",
        "herb" to "Herb", "herbal plant" to "Herb", "medicinal plant" to "Herb",
        "basil" to "Herb", "mint" to "Herb", "thyme" to "Herb",
        "parsley" to "Herb", "rosemary" to "Herb", "lemongrass" to "Herb",
        "ginger" to "Herb", "aloe vera" to "Herb", "pandan" to "Herb",
        "malunggay" to "Herb", "lagundi" to "Herb", "sambong" to "Herb",
        "grass" to "Grass", "lawn" to "Grass", "meadow" to "Grass",
        "rice" to "Grass", "wheat" to "Grass", "corn" to "Grass",
        "sugarcane" to "Grass", "bamboo" to "Grass", "reed" to "Grass",
        "fern" to "Fern", "frond" to "Fern", "moss" to "Fern",
        "algae" to "Fern", "lichen" to "Fern",
        "pen" to "Writing Tool", "ballpen" to "Writing Tool",
        "marker" to "Writing Tool", "highlighter" to "Writing Tool",
        "crayon" to "Writing Tool", "chalk" to "Writing Tool",
        "stylus" to "Writing Tool", "writing" to "Writing Tool",
        "book" to "Book", "textbook" to "Book", "novel" to "Book",
        "dictionary" to "Book", "journal" to "Book", "notebook" to "Book",
        "magazine" to "Book", "reading material" to "Book",
        "ruler" to "Ruler", "measuring tape" to "Ruler",
        "protractor" to "Ruler", "compass" to "Ruler",
        "scissors" to "Scissors", "cutter" to "Scissors",
        "shears" to "Scissors", "paper cutter" to "Scissors",
        "globe" to "Globe", "map" to "Globe", "atlas" to "Globe",
        "world map" to "Globe", "geography" to "Globe", "sphere" to "Globe"
    )

    private val labelToCategory = mapOf(
        "dog" to "animals", "puppy" to "animals", "canine" to "animals", "labrador" to "animals",
        "poodle" to "animals", "bulldog" to "animals", "golden retriever" to "animals",
        "cat" to "animals", "kitten" to "animals", "feline" to "animals", "kitty" to "animals",
        "tabby" to "animals", "persian cat" to "animals", "bird" to "animals", "sparrow" to "animals",
        "pigeon" to "animals", "eagle" to "animals", "parrot" to "animals", "crow" to "animals",
        "hen" to "animals", "rooster" to "animals", "frog" to "animals", "toad" to "animals",
        "tadpole" to "animals", "butterfly" to "animals", "monarch butterfly" to "animals",
        "wildlife" to "animals", "wild animal" to "animals", "zoo" to "animals", "safari" to "animals",
        "mammal" to "animals", "wolf" to "animals", "fox" to "animals", "bear" to "animals",
        "lion" to "animals", "tiger" to "animals", "elephant" to "animals", "horse" to "animals",
        "cow" to "animals", "deer" to "animals", "rabbit" to "animals", "monkey" to "animals",
        "gorilla" to "animals", "cheetah" to "animals", "leopard" to "animals", "zebra" to "animals",
        "giraffe" to "animals", "hippo" to "animals", "pig" to "animals", "sheep" to "animals",
        "goat" to "animals", "whale" to "animals", "dolphin" to "animals", "seal" to "animals",
        "reptile" to "animals", "snake" to "animals", "lizard" to "animals", "gecko" to "animals",
        "chameleon" to "animals", "iguana" to "animals", "crocodile" to "animals",
        "alligator" to "animals", "turtle" to "animals", "tortoise" to "animals", "komodo" to "animals",
        "cobra" to "animals", "python" to "animals", "viper" to "animals", "insect" to "animals",
        "ant" to "animals", "bee" to "animals", "wasp" to "animals", "beetle" to "animals",
        "grasshopper" to "animals", "cockroach" to "animals", "dragonfly" to "animals",
        "mosquito" to "animals", "ladybug" to "animals", "firefly" to "animals",
        "caterpillar" to "animals", "spider" to "animals", "moth" to "animals", "fly" to "animals",
        "fish" to "animals", "shark" to "animals", "salmon" to "animals", "clownfish" to "animals",
        "goldfish" to "animals", "tilapia" to "animals", "catfish" to "animals",
        "swordfish" to "animals", "trout" to "animals", "eel" to "animals", "stingray" to "animals",
        "seahorse" to "animals", "piranha" to "animals", "aquatic animal" to "animals",
        "marine animal" to "animals", "marine life" to "animals", "aquatic" to "animals",
        "sunflower" to "plants", "helianthus" to "plants", "yellow flower" to "plants",
        "petal" to "plants", "rose" to "plants", "red rose" to "plants", "pink rose" to "plants",
        "santan" to "plants", "ixora" to "plants", "jungle flame" to "plants",
        "oregano" to "plants", "herb leaf" to "plants", "makahiya" to "plants",
        "mimosa" to "plants", "sensitive plant" to "plants", "touch me not" to "plants",
        "flowering plant" to "plants", "blossom" to "plants", "wildflower" to "plants",
        "orchid" to "plants", "tulip" to "plants", "daisy" to "plants", "lily" to "plants",
        "jasmine" to "plants", "hibiscus" to "plants", "gumamela" to "plants",
        "carnation" to "plants", "lavender" to "plants", "flower" to "plants", "floral" to "plants",
        "tree" to "plants", "oak" to "plants", "pine" to "plants", "palm" to "plants",
        "mango tree" to "plants", "coconut tree" to "plants", "acacia" to "plants",
        "banyan" to "plants", "willow" to "plants", "trunk" to "plants", "bark" to "plants",
        "branch" to "plants", "herb" to "plants", "herbal plant" to "plants",
        "medicinal plant" to "plants", "basil" to "plants", "mint" to "plants", "thyme" to "plants",
        "parsley" to "plants", "rosemary" to "plants", "lemongrass" to "plants", "ginger" to "plants",
        "aloe vera" to "plants", "pandan" to "plants", "malunggay" to "plants",
        "lagundi" to "plants", "sambong" to "plants", "grass" to "plants", "lawn" to "plants",
        "meadow" to "plants", "rice" to "plants", "wheat" to "plants", "corn" to "plants",
        "sugarcane" to "plants", "bamboo" to "plants", "reed" to "plants", "fern" to "plants",
        "frond" to "plants", "moss" to "plants", "algae" to "plants", "lichen" to "plants",
        "pencil" to "classroom", "graphite pencil" to "classroom", "colored pencil" to "classroom",
        "bag" to "classroom", "backpack" to "classroom", "school bag" to "classroom",
        "rucksack" to "classroom", "table" to "classroom", "desk" to "classroom",
        "wooden table" to "classroom", "chair" to "classroom", "seat" to "classroom",
        "stool" to "classroom", "blackboard" to "classroom", "chalkboard" to "classroom",
        "pen" to "classroom", "ballpen" to "classroom", "marker" to "classroom",
        "highlighter" to "classroom", "crayon" to "classroom", "chalk" to "classroom",
        "stylus" to "classroom", "writing" to "classroom", "book" to "classroom",
        "textbook" to "classroom", "novel" to "classroom", "dictionary" to "classroom",
        "journal" to "classroom", "notebook" to "classroom", "magazine" to "classroom",
        "reading material" to "classroom", "ruler" to "classroom", "measuring tape" to "classroom",
        "protractor" to "classroom", "compass" to "classroom", "scissors" to "classroom",
        "cutter" to "classroom", "shears" to "classroom", "paper cutter" to "classroom",
        "globe" to "classroom", "map" to "classroom", "atlas" to "classroom",
        "world map" to "classroom", "geography" to "classroom", "sphere" to "classroom"
    )

    private val specificIdToCategory = mapOf(
        "dog" to "animals", "cat" to "animals", "bird" to "animals",
        "frog" to "animals", "butterfly" to "animals",
        "sunflower" to "plants", "rose" to "plants", "santan" to "plants",
        "oregano" to "plants", "makahiya" to "plants",
        "pencil" to "classroom", "bag" to "classroom", "table" to "classroom",
        "chair" to "classroom", "blackboard" to "classroom"
    )

    fun classify(bitmap: Bitmap, onResult: (ClassificationResult) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.4f)
            .build()
        val labeler = ImageLabeling.getClient(options)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    android.util.Log.d("MLKit", "Detected: ${label.text} (${label.confidence})")
                }

                val detectedLabels = labels.map {
                    DetectionLabel(text = it.text, confidence = it.confidence)
                }
                val labelTexts = detectedLabels.map { it.text }
                lastDetectedLabels = detectedLabels


                for (label in labelTexts) {
                    val lower = label.lowercase()
                    val specificId = labelToSpecificId[lower]
                    if (specificId != null) {
                        val category = specificIdToCategory[specificId]
                        onResult(
                            ClassificationResult(
                                category = category,
                                specificId = specificId,
                                labels = detectedLabels
                            )
                        )
                        labeler.close()
                        return@addOnSuccessListener
                    }
                }

                val category = detectCategory(labelTexts)
                onResult(
                    ClassificationResult(
                        category = category,
                        specificId = null,
                        labels = detectedLabels
                    )
                )
                labeler.close()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MLKit", "Labeling failed: ${e.message}")
                lastDetectedLabels = emptyList()
                onResult(ClassificationResult())
                labeler.close()
            }
    }

    fun resolveGeneralName(mlKitLabels: List<String>): String? {
        for (label in mlKitLabels) {
            val match = labelToGeneralName[label.lowercase()]
            if (match != null) return match
        }
        return null
    }

    fun detectCategory(mlKitLabels: List<String>): String? {
        for (label in mlKitLabels) {
            val match = labelToCategory[label.lowercase()]
            if (match != null) return match
        }
        return null
    }
}
