package hbv601g.Recipe;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Review;
//import hbv601g.Recipe.viewmodels.ReviewViewModel;

import java.util.List;

public class RecipePageActivity extends AppCompatActivity {

   // private ReviewViewModel reviewViewModel; // ViewModel for managing reviews
    private String recipeId; // ID of the recipe
    private RatingBar ratingBar;
    private EditText commentEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_page); // Make sure this layout file exists

        // Initialize the ViewModel
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        // Assuming you get the recipe ID from intent extras
        recipeId = getIntent().getStringExtra("RECIPE_ID");

        // Initialize UI elements
        ratingBar = findViewById(R.id.ratingBar);
        commentEditText = findViewById(R.id.commentEditText);
        Button submitReviewButton = findViewById(R.id.submitReviewButton);

        // Fetch and observe reviews for the recipe
        reviewViewModel.getReviewsByRecipeId(recipeId).observe(this, new Observer<List<Review>>() {
            @Override
            public void onChanged(List<Review> reviews) {
                // Update the UI with the reviews
                // For example, populate a RecyclerView or ListView with the reviews
            }
        });

        // Set up the button to submit a new review
        submitReviewButton.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        String comment = commentEditText.getText().toString();
        int rating = (int) ratingBar.getRating();

        if (!comment.isEmpty() && rating > 0) {
            Review newReview = new Review(comment, rating, "user_id", recipeId, null);
            reviewViewModel.addReview(newReview);
            Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
            // Optionally clear the input fields or refresh reviews
        } else {
            Toast.makeText(this, "Please provide a valid comment and rating", Toast.LENGTH_SHORT).show();
        }
    }
}

