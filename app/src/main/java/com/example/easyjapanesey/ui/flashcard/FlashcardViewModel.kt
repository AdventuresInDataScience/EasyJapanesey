package com.example.easyjapanesey.ui.flashcard

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyjapanesey.data.model.CardMode
import com.example.easyjapanesey.data.model.CardStatus
import com.example.easyjapanesey.data.model.FilterMode
import com.example.easyjapanesey.data.model.VocabularyCard
import com.example.easyjapanesey.data.preferences.UserProgressRepository
import com.example.easyjapanesey.data.repository.PhrasesRepository
import com.example.easyjapanesey.data.repository.VocabularyRepository
import kotlinx.coroutines.delay
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
    val ttsError: String? = null,
    val cardMode: CardMode = CardMode.RECALL
)

class FlashcardViewModel(
    private val context: Context,
    private val category: String,
    private val level1: String,
    private val level2: String?
) : ViewModel() {
    
    private val vocabularyRepository = VocabularyRepository(context)
    private val phrasesRepository = PhrasesRepository(context)
    private val progressRepo = UserProgressRepository(context)
    private var tts: TextToSpeech? = null
    
    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()
    
    // Store all cards and filtered cards
    private var allCards: List<VocabularyCard> = emptyList()
    private val collectionKey = "$category-$level1-${level2 ?: ""}"
    
    init {
        loadCards(category, level1, level2)
        initializeTTS()
        updateCardMode()
    }
    
    private fun updateCardMode() {
        val mode = progressRepo.getCardMode()
        _uiState.value = _uiState.value.copy(cardMode = mode)
    }
    
    private fun loadCards(category: String, level1: String, level2: String?) {
        viewModelScope.launch {
            // Check if this is a phrases request (category is N1-N5 AND level1 is "All")
            allCards = if (category.matches(Regex("N[1-5]")) && level1 == "All") {
                // It's a phrases level
                phrasesRepository.getCardsForPath(category, level1, level2)
            } else {
                // It's vocabulary
                vocabularyRepository.getCardsForPath(category, level1, level2)
            }
            applyFilter()
        }
    }
    
    private fun applyFilter() {
        val filterMode = progressRepo.getFilterMode()
        val filtered = when (filterMode) {
            FilterMode.ALL -> allCards
            FilterMode.WRONG_ONLY -> allCards.filter { card ->
                val cardId = progressRepo.generateCardId(category, level1, level2, card.english)
                progressRepo.getCardStatus(cardId) == CardStatus.WRONG
            }
            FilterMode.WRONG_AND_UNSEEN -> allCards.filter { card ->
                val cardId = progressRepo.generateCardId(category, level1, level2, card.english)
                val status = progressRepo.getCardStatus(cardId)
                status == CardStatus.WRONG || status == CardStatus.UNSEEN
            }
        }
        
        if (filtered.isEmpty()) {
            // If filter results in no cards, show message or all cards
            _uiState.value = _uiState.value.copy(
                cards = allCards,
                currentIndex = 0
            )
        } else {
            val savedPosition = progressRepo.getCurrentPosition(collectionKey)
            _uiState.value = _uiState.value.copy(
                cards = filtered,
                currentIndex = savedPosition.coerceIn(0, filtered.size - 1)
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
            progressRepo.setCurrentPosition(collectionKey, newIndex)
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
            progressRepo.setCurrentPosition(collectionKey, newIndex)
            _uiState.value = currentState.copy(
                currentIndex = newIndex,
                isFlipped = false
            )
        }
    }
    
    fun markCardCorrect() {
        val currentState = _uiState.value
        if (currentState.cards.isNotEmpty()) {
            val currentCard = currentState.cards[currentState.currentIndex]
            val cardId = progressRepo.generateCardId(category, level1, level2, currentCard.english)
            progressRepo.setCardStatus(cardId, CardStatus.CORRECT)
            
            // Auto-advance to next card
            nextCard()
        }
    }
    
    fun markCardWrong() {
        val currentState = _uiState.value
        if (currentState.cards.isNotEmpty()) {
            val currentCard = currentState.cards[currentState.currentIndex]
            val cardId = progressRepo.generateCardId(category, level1, level2, currentCard.english)
            progressRepo.setCardStatus(cardId, CardStatus.WRONG)
            
            // Auto-advance to next card
            nextCard()
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
