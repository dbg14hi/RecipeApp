package hbv601g.Recipe.fragments.recipe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.entities.Review;
import hbv601g.Recipe.fragments.review.NewReviewFragment;
import hbv601g.Recipe.fragments.review.ReviewAdapter;
import hbv601g.Recipe.repository.CloudinaryRepository;
import hbv601g.Recipe.repository.FirestoreRepository;

public class RecipeDetailFragment extends Fragment {

    private TextView titleTextView, descriptionTextView, ingredientsTextView, cookingTimeTextView, dietaryRestrictionsTextView, mealCategoriesTextView;
    private ImageButton favoriteButton;

    //Recipe Image
    private ImageView recipeImageView;
    private CloudinaryRepository cloudinaryRepository;

    private ReviewAdapter reviewAdapter; //Arna
    private List<Review> reviewList; //Arna

    private FirestoreRepository repository;
    private String userId, recipeId;
    private boolean isFavorite = false;
    private Recipe recipe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = new FirestoreRepository();
        cloudinaryRepository = new CloudinaryRepository(requireContext());

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.recipe_title);
        descriptionTextView = view.findViewById(R.id.recipe_description);
        ingredientsTextView = view.findViewById(R.id.recipe_ingredients);
        cookingTimeTextView = view.findViewById(R.id.recipe_cooking_time);
        dietaryRestrictionsTextView = view.findViewById(R.id.recipe_dietary_restrictions);
        mealCategoriesTextView = view.findViewById(R.id.recipe_meal_categories);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        recipeImageView = view.findViewById(R.id.recipeImage);
        //Arna
        Button reviewButton; //Arna
        reviewButton = view.findViewById(R.id.reviewButton);//Arna
        RecyclerView reviewRecyclerView; //Arna
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView); //Arna
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext())); //Arna
        reviewList = new ArrayList<>(); //Arna
        reviewAdapter = new ReviewAdapter(reviewList); //Arna
        reviewRecyclerView.setAdapter(reviewAdapter); //Arna

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("recipeTitle");
            String description = args.getString("recipeDescription");
            ArrayList<String> ingredients = args.getStringArrayList("recipeIngredients");
            int cookingTime = args.getInt("recipeCookingTime");
            recipeId = args.getString("recipeId");
            if (recipeId == null || recipeId.isEmpty()) {
                Toast.makeText(getContext(), "Error: Recipe ID is missing", Toast.LENGTH_SHORT).show();
                return view;
            }

            recipe = new Recipe(title, ingredients, description, cookingTime, null, null, null, userId);

            titleTextView.setText(title);
            descriptionTextView.setText(description);
            ingredientsTextView.setText(TextUtils.join(", ", ingredients));
            cookingTimeTextView.setText("Cooking Time: " + cookingTime + " minutes");

            checkIfFavorite();

            favoriteButton.setOnClickListener(v -> toggleFavorite(userId, recipeId));
            reviewButton.setOnClickListener(v -> openReviewFragment());

            displayRecipeImage();
            fetchReviews();
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        NavHostFragment.findNavController(RecipeDetailFragment.this).navigateUp();
                    }
                }
        );

        return view;
    }

    private void displayRecipeImage() {
        cloudinaryRepository.getImageFromCloudinary(recipeId, new CloudinaryRepository.CloudinaryCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d("RecipeDetailFragment", "Cloudinary Image URL: " + imageUrl);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .into(recipeImageView);
                    });

                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        recipeImageView.setImageResource(R.drawable.ic_launcher_background);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("RecipeDetailFragment", "Failed to load image from Cloudinary: " + errorMessage);
                Toast.makeText(getContext(), "Failed to load recipe image", Toast.LENGTH_SHORT).show();
                recipeImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        });
    }

    private void checkIfFavorite() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                isFavorite = favorites != null && favorites.contains(recipeId);
            } else {
                isFavorite = false;
            }
            updateFavoriteButtonUI(isFavorite);  // Update UI after checking
        }).addOnFailureListener(e -> Log.e("Favorites", "Failed to check favorite", e));
    }


    private void toggleFavorite(String userId, String recipeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> favorites = (List<String>) documentSnapshot.get("favorites");

                if (favorites == null) {
                    favorites = new ArrayList<>();
                }

                if (favorites.contains(recipeId)) {
                    // Remove from favorites
                    favorites.remove(recipeId);
                    isFavorite = false;
                } else {
                    // Add to favorites
                    favorites.add(recipeId);
                    isFavorite = true;
                }

                // Update Firestore and UI **only if Firestore update is successful**
                userRef.update("favorites", favorites)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Favorites", "Updated successfully");
                            updateFavoriteButtonUI(isFavorite);
                        })
                        .addOnFailureListener(e -> Log.e("Favorites", "Error updating", e));

            } else {
                // If user doc doesn't exist, create it and add favorite
                Map<String, Object> userData = new HashMap<>();
                userData.put("favorites", Arrays.asList(recipeId));

                userRef.set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Favorites", "User created with favorite");
                            isFavorite = true;
                            updateFavoriteButtonUI(isFavorite);
                        })
                        .addOnFailureListener(e -> Log.e("Favorites", "Error creating user", e));
            }
        });
    }

    // Function to update UI state
    private void updateFavoriteButtonUI(boolean isFavorited) {
        if (isFavorited) {
            favoriteButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.favorite_active));
        } else {
            favoriteButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.favorite_inactive));
        }
        updateFavoriteIcon(isFavorite);
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        favoriteButton.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_empty);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    //Virkni á add review takka.
    private void openReviewFragment() {
        NewReviewFragment newReviewFragment = new NewReviewFragment();
        Bundle args = new Bundle();
        args.putString("recipe_id", recipeId);
        newReviewFragment.setArguments(args);

        NavHostFragment.findNavController(this).navigate(R.id.newReviewFragment, args);
    }

    private void fetchReviews() {
        if (recipeId != null && !recipeId.isEmpty()) {
            repository.getReviewsByRecipe(recipeId, new FirestoreRepository.ReviewCallback() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onReviewsLoaded(List<Review> reviews) {
                    reviewList.clear();
                    reviewList.addAll(reviews);
                    reviewAdapter.notifyDataSetChanged();

                    Log.d("RecipeDetailFragment", "Reviews loaded: " + reviews.size());
                    //bætti þessu við til að ath rétta virkni
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}



