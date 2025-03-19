package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Review
import hbv601g.Recipe.repository.FirestoreRepository

class ReviewViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _reviewListLiveData = MutableLiveData<List<Review>>()
    val reviewListLiveData: LiveData<List<Review>> = _reviewListLiveData

    private val _singleReviewLiveData = MutableLiveData<Review?>()
    val singleReviewLiveData: LiveData<Review?> = _singleReviewLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    fun loadReviewsByRecipe(recipeId: String) {
        repository.getReviewsByRecipe(recipeId, object : FirestoreRepository.ReviewCallback {
            override fun onReviewsLoaded(reviews: List<Review>) {
                _reviewListLiveData.postValue(reviews)
            }

            override fun onFailure(e: Exception) {
                _errorLiveData.postValue("Failed to load reviews: ${e.message}")
            }
        })
    }

    fun loadReviewById(reviewId: String) {
        repository.getReviewById(reviewId, object : FirestoreRepository.ReviewByIdCallback {
            override fun onReviewLoaded(review: Review?) {
                _singleReviewLiveData.postValue(review)
            }

            override fun onFailure(e: Exception) {
                _errorLiveData.postValue("Failed to load review: ${e.message}")
            }
        })
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
}

