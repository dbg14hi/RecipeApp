package hbv601g.Recipe.services;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hbv601g.Recipe.entities.Review;

/**
 * A service class that handles user reviews in Firestore database.
 * Handles the logic for adding reviews to a recipe.
 *
 */
public class ReviewService {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    /**
     * Constructs a new ReviewService and initializes Firebase instances.
     *
     */
    public ReviewService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Adds a review to a special recipe in the Firestore database.
     *
     * @param review he {@link Review} object to be added. Must contain a valid recipe ID.
     * @param listener The callback listener that reports success or failure of the operation.
     */
    public void addReview(Review review, OnReviewAddedListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onFailure("User not signed in.");
            return;
        }

        if (review.getRecipeId() == null || review.getRecipeId().isEmpty()) {
            listener.onFailure("Recipe ID is missing.");
            return;
        }

        review.setUserId(user.getUid());

        db.collection("recipes")
                .document(review.getRecipeId())
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Review added with ID: " + documentReference.getId());
                    listener.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding review", e);
                    listener.onFailure(e.getMessage());
                });
    }

    /**
     * Callback interface for adding a review to a recipe.
     *
     */
    public interface OnReviewAddedListener {
        void onSuccess(String reviewId);
        void onFailure(String errorMessage);
    }
}
