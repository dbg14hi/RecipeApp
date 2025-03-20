package hbv601g.Recipe.fragments.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Review;
import hbv601g.Recipe.services.ReviewService;

public class NewReviewFragment extends Fragment {

    private RatingBar ratingBar;
    private EditText commentEditText;
    private ReviewService reviewService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        reviewService = new ReviewService(); // Initialize ReviewService
        ratingBar = view.findViewById(R.id.ratingBar);
        commentEditText = view.findViewById(R.id.commentEditText);

        view.findViewById(R.id.submitButton).setOnClickListener(v -> submitReview());
        view.findViewById(R.id.cancelButton).setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }


    private void submitReview() {
        String comment = commentEditText.getText().toString();
        int rating = (int) ratingBar.getRating();

        if (!comment.isEmpty() && rating > 0) {
            Review newReview = new Review(comment, rating, null, "recipe_id", null); // Use actual recipe ID
            reviewService.addReview(newReview, new ReviewService.OnReviewAddedListener() {
                @Override
                public void onSuccess(String reviewId) {
                    Toast.makeText(getContext(), "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(getContext(), "Failed to submit review: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please enter a comment and rating", Toast.LENGTH_SHORT).show();
        }
    }
}
