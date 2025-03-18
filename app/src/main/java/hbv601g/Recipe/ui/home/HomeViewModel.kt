package hbv601g.Recipe.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Recipe
import hbv601g.Recipe.repository.FirestoreRepository

class HomeViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _recipesLiveData = MutableLiveData<List<Recipe>>()
    val recipesLiveData: LiveData<List<Recipe>> = _recipesLiveData

    private val _filteredRecipesLiveData = MutableLiveData<List<Recipe>>()
    val filteredRecipesLiveData: LiveData<List<Recipe>> = _filteredRecipesLiveData

    private val _selectedDietaryRestrictions = MutableLiveData<List<String>>(emptyList())
    val selectedDietaryRestrictions: LiveData<List<String>> = _selectedDietaryRestrictions

    private val _selectedMealCategories = MutableLiveData<List<String>>(emptyList())
    val selectedMealCategories: LiveData<List<String>> = _selectedMealCategories

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        repository.getRecipes(object : FirestoreRepository.RecipeCallback {
            override fun onRecipesLoaded(recipes: List<Recipe>) {
                _recipesLiveData.postValue(recipes)
                _filteredRecipesLiveData.postValue(recipes) // Initially, filtered is full list
            }

            override fun onFailure(e: Exception) {
                _errorLiveData.postValue("Failed to load recipes: ${e.message}")
            }
        })
    }

    fun filterRecipes(query: String, selectedDietaryRestrictions: List<String>, selectedMealCategories: List<String>) {
        _searchQuery.value = query;
        val recipes = _recipesLiveData.value ?: emptyList()
        val filtered = recipes.filter { recipe ->
            val keywordMatch = query.isEmpty() ||
                    recipe.title.contains(query, ignoreCase = true) ||
                    recipe.description.contains(query, ignoreCase = true) ||
                    recipe.ingredients.any { it.contains(query, ignoreCase = true) }

            val dietaryRestrictionsMatch = selectedDietaryRestrictions.isEmpty() ||
                    selectedDietaryRestrictions.all { selectedCategory ->
                        recipe.dietaryRestrictions.contains(selectedCategory)
                    }

            val mealCategoriesMatch = selectedMealCategories.isEmpty() ||
                    selectedMealCategories.all { selectedCategory ->
                        recipe.mealCategories.contains(selectedCategory)
                    }

            keywordMatch && dietaryRestrictionsMatch && mealCategoriesMatch
        }
        _filteredRecipesLiveData.postValue(filtered)
    }

    fun sortRecipes(option: String) {
        val recipes = _filteredRecipesLiveData.value ?: emptyList()
        val sortedList = when (option) {
            "Name" -> recipes.sortedBy { it.title }
            "Date Added" -> recipes.sortedByDescending { it.timestamp }
            else -> recipes
        }
        _filteredRecipesLiveData.postValue(sortedList)
    }

    fun setSelectedCategories(dietaryRestrictions: List<String>, mealCategories: List<String>) {
        _selectedDietaryRestrictions.value = dietaryRestrictions
        _selectedMealCategories.value = mealCategories
        filterRecipes(_searchQuery.value.orEmpty(), _selectedDietaryRestrictions.value.orEmpty(), _selectedMealCategories.value.orEmpty())
    }
}