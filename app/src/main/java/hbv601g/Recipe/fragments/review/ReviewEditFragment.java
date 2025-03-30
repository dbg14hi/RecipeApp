package hbv601g.Recipe.fragments.review;

import android.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Review;
import hbv601g.Recipe.ui.home.ReviewViewModel;

/**
 * Fragment for ediding reviews
 */
public class ReviewEditFragment extends Fragment {
    private ReviewViewModel viewModel;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private String reviewId;

    /**
     * This is called when the fragment is first created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reviewId = getArguments().getString("reviewId");
        }
    }

    /**
     * Called to create and return the view
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        ratingBar = view.findViewById(R.id.ratingBar);
        commentEditText = view.findViewById(R.id.commentEditText);

        view.findViewById(R.id.saveButton).setOnClickListener(v -> updateReview());
        view.findViewById(R.id.deleteButton).setOnClickListener(v -> deleteReview());
        view.findViewById(R.id.cancelButton).setOnClickListener(v -> requireActivity().onBackPressed());

        loadReviewData();

        return view;
    }

    /**
     * Loads the ReviewData
     */
    private void loadReviewData() {
        viewModel.loadReviewById(reviewId); // 🔔 This is the correct method now!

        viewModel.getSingleReviewLiveData().observe(getViewLifecycleOwner(), review -> {
            if (review != null) {
                ratingBar.setRating(review.getRating().floatValue());
                commentEditText.setText(review.getComment());
            } else {
                Toast.makeText(getContext(), "Review not found", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the Review
     */
    private void updateReview() {
        String comment = commentEditText.getText().toString();
        int rating = (int) ratingBar.getRating();

        if (!comment.isEmpty() && rating > 0) {
            Review updatedReview = new Review(comment, rating, "user_id", "recipe_id", null);
            updatedReview.setId(reviewId);
            viewModel.updateReview(updatedReview);
            Toast.makeText(getContext(), "Review updated successfully", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        } else {
            Toast.makeText(getContext(), "Please provide a valid comment and rating", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes the review
     */
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
