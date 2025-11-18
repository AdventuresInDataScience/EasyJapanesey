package com.example.easyjapanesey.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.easyjapanesey.data.model.CardMode
import com.example.easyjapanesey.data.model.CardStatus
import com.example.easyjapanesey.data.model.FilterMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserProgressRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_progress",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    // Card Status Management
    fun getCardStatus(cardId: String): CardStatus {
        val statusString = prefs.getString("status_$cardId", CardStatus.UNSEEN.name)
        return CardStatus.valueOf(statusString ?: CardStatus.UNSEEN.name)
    }
    
    fun setCardStatus(cardId: String, status: CardStatus) {
        prefs.edit().putString("status_$cardId", status.name).apply()
    }
    
    fun getAllCardStatuses(): Map<String, CardStatus> {
        val allEntries = prefs.all
        val statuses = mutableMapOf<String, CardStatus>()
        
        allEntries.forEach { (key, value) ->
            if (key.startsWith("status_")) {
                val cardId = key.removePrefix("status_")
                val status = CardStatus.valueOf(value as String)
                statuses[cardId] = status
            }
        }
        
        return statuses
    }
    
    // Current Position Management (per collection)
    fun getCurrentPosition(collectionKey: String): Int {
        return prefs.getInt("position_$collectionKey", 0)
    }
    
    fun setCurrentPosition(collectionKey: String, position: Int) {
        prefs.edit().putInt("position_$collectionKey", position).apply()
    }
    
    fun resetAllPositions() {
        val editor = prefs.edit()
        prefs.all.keys.forEach { key ->
            if (key.startsWith("position_")) {
                editor.remove(key)
            }
        }
        editor.apply()
    }
    
    // Filter Mode Management
    fun getFilterMode(): FilterMode {
        val modeString = prefs.getString("filter_mode", FilterMode.ALL.name)
        return FilterMode.valueOf(modeString ?: FilterMode.ALL.name)
    }
    
    fun setFilterMode(mode: FilterMode) {
        prefs.edit().putString("filter_mode", mode.name).apply()
    }
    
    // Reset All Progress
    fun resetAllProgress() {
        val editor = prefs.edit()
        prefs.all.keys.forEach { key ->
            if (key.startsWith("status_")) {
                editor.remove(key)
            }
        }
        editor.apply()
    }
    
    // Generate card ID from card properties
    fun generateCardId(category: String, level1: String, level2: String?, english: String): String {
        return "$category-$level1-${level2 ?: "none"}-$english"
            .replace(" ", "_")
            .lowercase()
    }
    
    // Menu Expansion State Management
    fun isMenuExpanded(menuKey: String): Boolean {
        return prefs.getBoolean("expanded_$menuKey", false)
    }
    
    fun setMenuExpanded(menuKey: String, isExpanded: Boolean) {
        prefs.edit().putBoolean("expanded_$menuKey", isExpanded).apply()
    }
    
    // Card Mode Management (Read vs Recall)
    fun getCardMode(): CardMode {
        val modeString = prefs.getString("card_mode", CardMode.RECALL.name)
        return CardMode.valueOf(modeString ?: CardMode.RECALL.name)
    }
    
    fun setCardMode(mode: CardMode) {
        prefs.edit().putString("card_mode", mode.name).apply()
    }
}
