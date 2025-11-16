package com.example.easyjapanesey.ui.flashcard

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyjapanesey.data.model.VocabularyCard
import com.example.easyjapanesey.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class FlashcardUiState(
    val cards: List<VocabularyCard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val ttsAvailable: Boolean = false,
    val ttsError: String? = null
)

class FlashcardViewModel(
    private val context: Context,
    category: String,
    level1: String,
    level2: String?
) : ViewModel() {
    
    private val repository = VocabularyRepository(context)
    private var tts: TextToSpeech? = null
    
    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()
    
    // Store position per collection
    private val cardPositions = mutableMapOf<String, Int>()
    private val collectionKey = "$category-$level1-${level2 ?: ""}"
    
    init {
        loadCards(category, level1, level2)
        initializeTTS()
    }
    
    private fun loadCards(category: String, level1: String, level2: String?) {
        viewModelScope.launch {
            val cards = repository.getCardsForPath(category, level1, level2)
            val savedPosition = cardPositions[collectionKey] ?: 0
            _uiState.value = _uiState.value.copy(
                cards = cards,
                currentIndex = savedPosition.coerceIn(0, cards.size - 1)
            )
        }
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.JAPANESE)
                when (result) {
                    TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                        _uiState.value = _uiState.value.copy(
                            ttsAvailable = false,
                            ttsError = "Japanese language pack not installed"
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            ttsAvailable = true,
                            ttsError = null
                        )
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    ttsAvailable = false,
                    ttsError = "Text-to-Speech initialization failed"
                )
            }
        }
    }
    
    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }
    
    fun nextCard() {
        val currentState = _uiState.value
        if (currentState.cards.isNotEmpty()) {
            val newIndex = (currentState.currentIndex + 1) % currentState.cards.size
            cardPositions[collectionKey] = newIndex
            _uiState.value = currentState.copy(
                currentIndex = newIndex,
                isFlipped = false
            )
        }
    }
    
    fun previousCard() {
        val currentState = _uiState.value
        if (currentState.cards.isNotEmpty()) {
            val newIndex = if (currentState.currentIndex - 1 < 0) {
                currentState.cards.size - 1
            } else {
                currentState.currentIndex - 1
            }
            cardPositions[collectionKey] = newIndex
            _uiState.value = currentState.copy(
                currentIndex = newIndex,
                isFlipped = false
            )
        }
    }
    
    fun speakWord(text: String) {
        if (_uiState.value.ttsAvailable) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
