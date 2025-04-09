package hbv601g.Recipe.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hbv601g.Recipe.entities.Review
import hbv601g.Recipe.repository.FirestoreRepository

/**
 * A viewModel for managing review related data between the UI and the Firestore.
 *
 */
class ReviewViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _reviewListLiveData = MutableLiveData<List<Review>>()
    val reviewListLiveData: LiveData<List<Review>> = _reviewListLiveData

    private val _singleReviewLiveData = MutableLiveData<Review?>()
    val singleReviewLiveData: LiveData<Review?> = _singleReviewLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    /**
     * Loads all reviews associated with a given recipe ID.
     *
     * @param recipeId The ID of the recipe for which to load reviews.
     */
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

    /**
     * Loads a single review by its unique ID.
     *
     * @param reviewId The ID of the review to load.
     */
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

    /**
     * Adds a review to Firestore.
     */
    fun addReview(review: Review) {
        repository.addReview(review)
    }

    /**
     * Updates the review.
     */
    fun updateReview(review: Review) {
        repository.updateReview(review.id, review.comment)
    }

    /**
     * Deletes a review by its Id.
     *
     */
    fun deleteReview(reviewId: String) {
        repository.deleteReview(reviewId)
    }
}

