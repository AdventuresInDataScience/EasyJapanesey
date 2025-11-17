package com.example.easyjapanesey.data.repository

import android.content.Context
import com.example.easyjapanesey.data.model.Category
import com.example.easyjapanesey.data.model.Level1Group
import com.example.easyjapanesey.data.model.VocabularyCard
import java.io.BufferedReader
import java.io.InputStreamReader

class PhrasesRepository(private val context: Context) {
    
    private var cachedCategories: List<Category>? = null
    
    fun loadPhrases(): List<Category> {
        if (cachedCategories != null) return cachedCategories!!
        
        val inputStream = context.assets.open("phrases.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        // Skip header line
        reader.readLine()
        
        // Parse CSV into grouped structure
        // Phrases only have Level (single level) - no Type subdivision
        val levelMap = mutableMapOf<String, MutableList<VocabularyCard>>()
        
        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size >= 4) {
                val level = parts[0].trim()
                val emoji = parts[1].trim()
                val phrase = parts[2].trim()
                val meaning = parts[3].trim()
                
                val card = VocabularyCard(emoji, meaning, phrase)
                
                levelMap
                    .getOrPut(level) { mutableListOf() }
                    .add(card)
            }
        }
        
        reader.close()
        
        // Convert to data classes - each level becomes its own category
        // This creates a flat structure: N1, N2, N3, N4, N5 as separate categories
        val categories = levelMap.map { (levelName, cards) ->
            val level1Groups = listOf(Level1Group("All", emptyList(), cards))
            Category(levelName, level1Groups)
        }
        
        cachedCategories = categories
        return categories
    }
    
    fun getCardsForPath(category: String, level1: String?, level2: String?): List<VocabularyCard> {
        val categories = loadPhrases()
        val cat = categories.find { it.name == category } ?: return emptyList()
        
        // For phrases, we just return all cards in the category (level)
        return cat.level1Groups.firstOrNull()?.cards ?: emptyList()
    }
}
