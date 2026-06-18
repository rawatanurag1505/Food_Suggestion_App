package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.MealSuggestion
import com.example.api.suggestMeals
import com.example.db.AppDatabase
import com.example.db.AppRepository
import com.example.db.PantryItem
import com.example.db.UserProfile
import com.example.db.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val sharedPrefs = application.getSharedPreferences("mealmate_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
        
        // Restore session on startup
        val savedEmail = sharedPrefs.getString("logged_in_email", null)
        if (savedEmail != null) {
            viewModelScope.launch {
                val account = repository.getUserAccount(savedEmail)
                if (account != null) {
                    _currentUser.value = account
                }
            }
        }
    }

    fun login(email: String, passwordHash: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || passwordHash.isBlank()) {
                onError("Please fill in all fields")
                return@launch
            }
            val account = repository.getUserAccount(email.trim())
            if (account != null && account.passwordHash == passwordHash) {
                _currentUser.value = account
                sharedPrefs.edit().putString("logged_in_email", account.email).apply()
                onSuccess()
            } else {
                onError("Invalid email or password")
            }
        }
    }

    fun signup(email: String, username: String, passwordHash: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || username.isBlank() || passwordHash.isBlank()) {
                onError("Please fill in all fields")
                return@launch
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                onError("Please enter a valid email address")
                return@launch
            }
            val existing = repository.getUserAccount(email.trim())
            if (existing != null) {
                onError("An account already exists with this email")
                return@launch
            }
            val newAccount = UserAccount(email = email.trim(), username = username.trim(), passwordHash = passwordHash)
            repository.insertUserAccount(newAccount)
            _currentUser.value = newAccount
            sharedPrefs.edit().putString("logged_in_email", newAccount.email).apply()
            
            // Create a default clean user profile if none exists
            repository.saveUserProfile(UserProfile(id = 1))
            
            onSuccess()
        }
    }

    fun logout(onComplete: () -> Unit) {
        _currentUser.value = null
        sharedPrefs.edit().remove("logged_in_email").apply()
        onComplete()
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val pantryItems: StateFlow<List<PantryItem>> = repository.pantryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _suggestions = MutableStateFlow<List<MealSuggestion>>(emptyList())
    val suggestions: StateFlow<List<MealSuggestion>> = _suggestions.asStateFlow()

    private val _navigateToSuggestions = MutableStateFlow(false)
    val navigateToSuggestions: StateFlow<Boolean> = _navigateToSuggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch { repository.saveUserProfile(profile) }
    }

    fun addPantryItem(item: PantryItem) {
        viewModelScope.launch { repository.insertPantryItem(item) }
    }

    fun deletePantryItem(item: PantryItem) {
        viewModelScope.launch { repository.deletePantryItem(item) }
    }
    
    fun clearError() {
      _errorMessage.value = null
    }

    fun onNavigatedToSuggestions() {
        _navigateToSuggestions.value = false
    }

    fun generateSuggestions(mood: String, time: String, budget: String, mealType: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val profile = userProfile.value ?: UserProfile()
            val pantryList = pantryItems.value.joinToString("\n") { "- ${it.quantity} ${it.name} (Exp: ${it.expiryDate})" }
            
            val prompt = """
                You are a personal food assistant. Suggest 3 meal options for the user based on the context below.
                
                ### User Profile
                - Dietary restrictions: ${profile.restrictions}
                - Cuisine preferences: ${profile.cuisines}
                - Health goal: ${profile.healthGoal}
                - Cooking skill: ${profile.skillLevel}
                - Available equipment: ${profile.equipment}
                - Disliked foods: ${profile.dislikes}
                
                ### Available Ingredients
                $pantryList
                
                ### Today's Context
                - Meal type: $mealType
                - Mood: $mood
                - Time available: $time
                - Budget: ₹$budget
                
                ### Instructions
                - Prioritize ingredients that are expiring soon.
                - Match the mood and energy level.
                - Stay within the budget.
                - Use available equipment.
            """.trimIndent()
            
            try {
                val result = suggestMeals(prompt)
                if (result.isEmpty()) {
                    _errorMessage.value = "Could not generate suggestions. Please try again."
                } else {
                     _suggestions.value = result
                     _navigateToSuggestions.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
