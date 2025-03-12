package hbv601g.Recipe.services;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hbv601g.Recipe.entities.Review;

public class ReviewService {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public ReviewService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void addReview(Review review, OnReviewAddedListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            listener.onFailure("User not signed in.");
            return;
        }

        review.setUserId(user.getUid()); // Ensure user ID is set
        db.collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Review added with ID: " + documentReference.getId());
                    listener.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding review", e);
                    listener.onFailure(e.getMessage());
                });
    }

    public interface OnReviewAddedListener {
        void onSuccess(String reviewId);
        void onFailure(String errorMessage);
    }
}
