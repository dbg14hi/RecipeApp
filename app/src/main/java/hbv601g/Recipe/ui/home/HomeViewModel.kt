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

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        repository.getRecipes(object : FirestoreRepository.RecipeCallback {
            override fun onRecipesLoaded(recipes: List<Recipe>) {
                _recipesLiveData.postValue(recipes)
            }

            override fun onFailure(e: Exception) {
                _errorLiveData.postValue("Failed to load recipes: ${e.message}")
            }
        })
    }
}
