package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Recipe
import hbv601g.Recipe.repository.FirestoreRepository

class HomeViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private var allRecipes: List<Recipe> = emptyList()

    private val _recipesLiveData = MutableLiveData<List<Recipe>>()
    val recipesLiveData: LiveData<List<Recipe>> = _recipesLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        repository.getRecipes(object : FirestoreRepository.RecipeCallback {
            override fun onRecipesLoaded(recipes: List<Recipe>) {
                allRecipes = recipes
                _recipesLiveData.postValue(allRecipes)
            }

            override fun onFailure(e: Exception) {
                _errorLiveData.postValue("Failed to load recipes: ${e.message}")
            }
        })
    }
}
