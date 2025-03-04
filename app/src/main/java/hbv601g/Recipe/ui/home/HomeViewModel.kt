package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Recipe
import hbv601g.Recipe.entities.Review
import hbv601g.Recipe.repository.FirestoreRepository

class HomeViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val _recipesLiveData = MutableLiveData<List<Recipe>>()

    val recipesLiveData: LiveData<List<Recipe>> get() = _recipesLiveData

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        repository.getRecipes(object : FirestoreRepository.FirestoreCallback {
            override fun onRecipesLoaded(recipes: List<Recipe>) {
                _recipesLiveData.postValue(recipes)
            }

            override fun onReviewsLoaded(reviews: List<Review>) {
                // Not needed for now, but required by the interface
            }

            override fun onFailure(e: Exception) {

                e.printStackTrace()
            }
        })
    }

}
