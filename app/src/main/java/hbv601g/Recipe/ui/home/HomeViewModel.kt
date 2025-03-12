package hbv601g.Recipe.ui.home

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

    private val _selectedCategories = MutableLiveData<List<String>>(emptyList())
    val selectedCategories: LiveData<List<String>> = _selectedCategories

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

    fun filterRecipes(query: String, selectedCategories: List<String>) {
        _searchQuery.value = query;
        val recipes = _recipesLiveData.value ?: emptyList()
        val filtered = recipes.filter { recipe ->
            val keywordMatch = query.isEmpty() ||
                    recipe.title.contains(query, ignoreCase = true) ||
                    recipe.description.contains(query, ignoreCase = true) ||
                    recipe.ingredients.any { it.contains(query, ignoreCase = true) }

            val categoryMatch = selectedCategories.isEmpty() ||
                    selectedCategories.all { selectedCategory ->
                        recipe.dietaryRestrictions.contains(selectedCategory)
                    }

            keywordMatch && categoryMatch
        }
        _filteredRecipesLiveData.postValue(filtered)
    }

    fun setSelectedCategories(categories: List<String>) {
        _selectedCategories.value = categories
        filterRecipes(_searchQuery.value.orEmpty(), _selectedCategories.value.orEmpty())
    }
}