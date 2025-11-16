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
        val categoryMap = mutableMapOf<String, MutableMap<String, MutableMap<String, MutableList<VocabularyCard>>>>()
        
        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size >= 5) {
                val category = parts[0].trim()
                val level1 = parts[1].trim()
                val level2 = parts[2].trim()
                val emoji = parts[3].trim()
                val english = parts[4].trim()
                val romaji = parts[5].trim()
                
                val card = VocabularyCard(emoji, english, romaji)
                
                categoryMap
                    .getOrPut(category) { mutableMapOf() }
                    .getOrPut(level1) { mutableMapOf() }
                    .getOrPut(level2) { mutableListOf() }
                    .add(card)
            }
        }
        
        reader.close()
        
        // Convert to data classes
        val categories = categoryMap.map { (categoryName, level1Map) ->
            val level1Groups = level1Map.map { (level1Name, level2Map) ->
                if (level2Map.size == 1 && level2Map.containsKey("")) {
                    // Single level - cards directly under level1
                    Level1Group(level1Name, emptyList(), level2Map[""]!!)
                } else {
                    // Two levels - level2 groups under level1
                    val level2Groups = level2Map
                        .filter { it.key.isNotEmpty() }
                        .map { (level2Name, cards) ->
                            Level2Group(level2Name, cards)
                        }
                    Level1Group(level1Name, level2Groups, emptyList())
                }
            }
            Category(categoryName, level1Groups)
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
