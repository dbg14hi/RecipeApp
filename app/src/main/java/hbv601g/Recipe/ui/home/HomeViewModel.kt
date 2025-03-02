package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Recipe
import hbv601g.Recipe.repository.FirestoreRepository

class HomeViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val _recipesLiveData = MutableLiveData<List<Recipe>>()

    val recipesLiveData: LiveData<List<Recipe>> get() = _recipesLiveData

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
       repository.getRecipes { recipes ->
           _recipesLiveData.postValue(recipes)
        }
    }
}
