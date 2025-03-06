package hbv601g.Recipe.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Recipe
import hbv601g.Recipe.repository.FirestoreRepository

//class HomeViewModel : ViewModel() {
//
//    private val repository = FirestoreRepository()
//    private val _recipesLiveData = MutableLiveData<List<Recipe>>()
//    private val _errorLiveData = MutableLiveData<String>()
//
//    val recipesLiveData: LiveData<List<Recipe>> get() = _recipesLiveData
//    val errorLiveData: LiveData<String> get() = _errorLiveData
//
//    init {
//        loadRecipes()
//    }
//
//    private fun loadRecipes() {
//        repository.getRecipes(object : FirestoreRepository.FirestoreCallback {
//            override fun onRecipesLoaded(recipes: List<Recipe>) {
//                _recipesLiveData.value = recipes
//            }
//
//            override fun onError(exception: Exception) {
//                _errorLiveData.value = exception.message ?: "Unknown error"
//            }
//        })
//    }
//}


class HomeViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val _recipesLiveData = MutableLiveData<List<Recipe>>()
    private val _errorLiveData = MutableLiveData<String>()

    val recipesLiveData: LiveData<List<Recipe>> get() = _recipesLiveData
    val errorLiveData: LiveData<String> get() = _errorLiveData


    init {
        loadRecipes()
    }

    private fun loadRecipes() {

        // Temporary hardcoded recipes
        val sampleRecipes = listOf(
            Recipe("Spaghetti Bolognese", listOf("Spaghetti", "Beef", "Tomato Sauce"), "A classic Italian dish", 45),
            Recipe("Chicken Curry", listOf("Chicken", "Curry Paste", "Coconut Milk"), "Spicy and creamy curry", 60)
        )
        _recipesLiveData.value = sampleRecipes
    }
}
