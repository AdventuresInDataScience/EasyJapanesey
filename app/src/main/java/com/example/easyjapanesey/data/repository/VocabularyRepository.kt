package com.example.easyjapanesey.data.repository

import android.content.Context
import com.example.easyjapanesey.data.model.Category
import com.example.easyjapanesey.data.model.Level1Group
import com.example.easyjapanesey.data.model.Level2Group
import com.example.easyjapanesey.data.model.VocabularyCard
import java.io.BufferedReader
import java.io.InputStreamReader

class VocabularyRepository(private val context: Context) {
    
    private var cachedCategories: List<Category>? = null
    
    fun loadVocabulary(): List<Category> {
        if (cachedCategories != null) return cachedCategories!!
        
        val inputStream = context.assets.open("vocabulary.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        // Skip header line
        reader.readLine()
        
        // Parse CSV into grouped structure
        // New format: Level,Type,Vocabulary,Meaning,Emoji
        // Level = Category (e.g., N5, N4, N3, N2, N1)
        // Type = Level1 (e.g., Noun, Verb, Adjective, etc.)
        val categoryMap = mutableMapOf<String, MutableMap<String, MutableList<VocabularyCard>>>()
        
        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size >= 5) {
                val level = parts[0].trim()       // Category (e.g., N5, N4)
                val type = parts[1].trim()        // Level1 (e.g., Noun, Verb)
                val vocabulary = parts[2].trim()  // The vocabulary word
                val meaning = parts[3].trim()     // English meaning
                val emoji = parts[4].trim()       // Emoji
                
                val card = VocabularyCard(emoji, meaning, vocabulary)
                
                categoryMap
                    .getOrPut(level) { mutableMapOf() }
                    .getOrPut(type) { mutableListOf() }
                    .add(card)
            }
        }
        
        reader.close()
        
        // Convert to data classes
        val categories = categoryMap.map { (levelName, typeMap) ->
            val level1Groups = typeMap.map { (typeName, cards) ->
                // Vocabulary has two levels: Level (category) and Type (level1)
                // Cards are directly under Type with no further subdivision
                Level1Group(typeName, emptyList(), cards)
            }
            Category(levelName, level1Groups)
        }
        
        cachedCategories = categories
        return categories
    }
    
    fun getCardsForPath(category: String, level1: String, level2: String?): List<VocabularyCard> {
        val categories = loadVocabulary()
        val cat = categories.find { it.name == category } ?: return emptyList()
        val l1 = cat.level1Groups.find { it.name == level1 } ?: return emptyList()
        
        return if (level2 != null) {
            // Two-level path
            l1.level2Groups.find { it.name == level2 }?.cards ?: emptyList()
        } else {
            // Single-level path
            l1.cards
        }
    }
}
