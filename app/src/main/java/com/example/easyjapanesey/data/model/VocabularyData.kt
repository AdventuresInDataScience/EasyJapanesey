package com.example.easyjapanesey.data.model

data class VocabularyCard(
    val emoji: String,
    val english: String,
    val romaji: String
)

// Level 2 - final flashcard collection
data class Level2Group(
    val name: String,
    val cards: List<VocabularyCard>
)

// Level 1 - can contain Level2Groups or cards directly
data class Level1Group(
    val name: String,
    val level2Groups: List<Level2Group> = emptyList(),
    val cards: List<VocabularyCard> = emptyList()
) {
    val hasSubLevels: Boolean
        get() = level2Groups.isNotEmpty()
}

// Category - Noun, Verb, Adjective, etc.
data class Category(
    val name: String,
    val level1Groups: List<Level1Group>
)
