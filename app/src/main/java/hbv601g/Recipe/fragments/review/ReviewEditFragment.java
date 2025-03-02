package hbv601g.Recipe.fragments.review;

import android.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Review;
import hbv601g.Recipe.ui.home.ReviewViewModel;

public class ReviewEditFragment extends Fragment {

    private ReviewViewModel viewModel;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private String reviewId; // ID of the review to edit

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reviewId = getArguments().getString("reviewId"); // Get review ID from arguments
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_review, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        ratingBar = view.findViewById(R.id.ratingBar);
        commentEditText = view.findViewById(R.id.commentEditText);

        view.findViewById(R.id.saveButton).setOnClickListener(v -> updateReview());
        view.findViewById(R.id.deleteButton).setOnClickListener(v -> deleteReview());
        view.findViewById(R.id.cancelButton).setOnClickListener(v -> requireActivity().onBackPressed());

        // Load existing review data
        loadReviewData();

        return view;
    }

    private void loadReviewData() {
        // Load review data using ViewModel or repository and populate fields
        viewModel.getReviewById(reviewId).observe(getViewLifecycleOwner(), review -> {
            if (review != null) {
                ratingBar.setRating(review.getRating().floatValue());
                commentEditText.setText(review.getComment());
            } else {
                Toast.makeText(getContext(), "Review not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReview() {
        String comment = commentEditText.getText().toString();
        int rating = (int) ratingBar.getRating();

        if (!comment.isEmpty() && rating > 0) {
            Review updatedReview = new Review(comment, rating, "user_id", "recipe_id", null);
            updatedReview.setId(reviewId); // Set the ID of the review to be updated

            // Use the ViewModel to update the review
            viewModel.updateReview(updatedReview);
            Toast.makeText(getContext(), "Review updated successfully", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        } else {
            Toast.makeText(getContext(), "Please provide a valid comment and rating", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteReview() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Review")
                .setMessage("Are you sure you want to delete this review?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.deleteReview(reviewId);
                    Toast.makeText(getContext(), "Review deleted successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
