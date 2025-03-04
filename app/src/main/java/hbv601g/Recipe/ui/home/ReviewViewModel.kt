package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import hbv601g.Recipe.entities.Recipe
import hbv601g.Recipe.entities.Review
import hbv601g.Recipe.repository.FirestoreRepository

class ReviewViewModel {
    private val repository = FirestoreRepository()
    private val reviewLiveData = MutableLiveData<List<Review>>()

    init {
        loadReview()
    }

    fun getReviewLiveData(): LiveData<List<Review>> {
        return getReviewLiveData()
    }

    private fun loadReview() {
        repository.getReviewsByRecipe() { recipes ->
            reviewLiveData.value = reviews
        }
    }

    fun addReview(review: Review) {
        repository.addReview(review)
    }
}
}