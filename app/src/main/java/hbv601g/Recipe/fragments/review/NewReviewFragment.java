package hbv601g.Recipe.fragments.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Review;
import hbv601g.Recipe.services.ReviewService;

/**
 * A fragment that allows users to submit a new review for a recipe.
 */
public class NewReviewFragment extends Fragment {

    private RatingBar ratingBar;
    private EditText commentEditText;
    private ReviewService reviewService;

    /**
     * Called to create the fragment's view.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The created view for the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        reviewService = new ReviewService();
        ratingBar = view.findViewById(R.id.ratingBar);
        commentEditText = view.findViewById(R.id.commentEditText);

        view.findViewById(R.id.submitButton).setOnClickListener(v -> submitReview());
        view.findViewById(R.id.cancelButton).setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    /**
     * Handles the submission of a new review.
     * Validates input fields and sends the review data to the ReviewService.
     */
    private void submitReview() {
        String comment = commentEditText.getText().toString();
        int rating = (int) ratingBar.getRating();
        String recipeId = getArguments() != null ? getArguments().getString("recipe_id") : null;

        if (recipeId == null) {
            Toast.makeText(getContext(), "Error: Missing recipe ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!comment.isEmpty() && rating > 0) {
            Review newReview = new Review(comment, rating, null, recipeId, null);
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
