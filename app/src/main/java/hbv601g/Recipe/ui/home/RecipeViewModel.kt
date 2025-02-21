package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.repository.FirestoreRepository

class RecipeViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val recipesLiveData = MutableLiveData<List<Map<String, Any>>>()

    init {
        loadRecipes()
    }

    fun getRecipesLiveData(): LiveData<List<Map<String, Any>>> {
        return recipesLiveData
    }

    private fun loadRecipes() {
        repository.getRecipes { recipes ->
            recipesLiveData.value = recipes
        }
    }

    fun addRecipe(name: String, ingredients: String) {
        repository.addRecipe(name, ingredients)
    }
}
