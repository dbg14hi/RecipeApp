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
import androidx.lifecycle.ViewModelProvider;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Review;
import hbv601g.Recipe.ui.home.ReviewViewModel;

public class NewReviewFragment extends Fragment {

    private ReviewViewModel viewModel; // Skoða betur
    private RatingBar ratingBar;
    private EditText commentEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_review, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

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
            // Create a new review object
            Review newReview = new Review(comment, rating, "user_id", "recipe_id", null);
            viewModel.addReview(newReview); // Use the ViewModel to add the review
            Toast.makeText(getContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        } else {
            Toast.makeText(getContext(), "Please provide a valid comment and rating", Toast.LENGTH_SHORT).show();
        }
    }
}
