package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Recipe
import hbv601g.Recipe.entities.Review
import hbv601g.Recipe.repository.FirestoreRepository

class ReviewViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val reviewLiveData = MutableLiveData<List<Review>>()
    private val singleReviewLiveData = MutableLiveData<Review>() // For single review data

    init {
        loadReviews("defaultRecipeId") // 🔹 Replace with an actual recipe ID or pass dynamically
    }

    fun getReviewLiveData(): LiveData<List<Review>> {
        return reviewLiveData
    }

    fun getSingleReviewLiveData(): LiveData<Review> {
        return singleReviewLiveData
    }

    fun addReview(review: Review) {
        repository.addReview(review)
    }

    fun updateReview(review: Review) {
        repository.updateReview(review.id, review.comment)
    }

    fun deleteReview(reviewId: String) {
        repository.deleteReview(reviewId)
    }

    private fun loadReviews(recipeId: String) {
        repository.getReviewsByRecipe(recipeId, object : FirestoreRepository.FirestoreCallback {
            override fun onReviewsLoaded(reviews: List<Review>) {
                reviewLiveData.postValue(reviews)
            }

            override fun onRecipesLoaded(recipes: List<Recipe>) {
                // Not needed here
            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun getReviewById(reviewId: String) {
        repository.getReviewById(reviewId, object : FirestoreRepository.ReviewCallback {
            override fun onReviewLoaded(review: Review?) {
                singleReviewLiveData.postValue(review)
            }

            override fun onFailure(e: Exception) {
                e.printStackTrace()
            }
        })
    }
}
