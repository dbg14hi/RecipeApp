package hbv601g.Recipe.fragments.recipe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.repository.FirestoreRepository;

public class RecipeDetailFragment extends Fragment {

    private TextView titleTextView, descriptionTextView, ingredientsTextView, cookingTimeTextView;
    private ImageButton favoriteButton;
    private FirestoreRepository repository;
    private String userId, recipeId;
    private boolean isFavorite = false;
    private Recipe recipe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = new FirestoreRepository();

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.recipe_title);
        descriptionTextView = view.findViewById(R.id.recipe_description);
        ingredientsTextView = view.findViewById(R.id.recipe_ingredients);
        cookingTimeTextView = view.findViewById(R.id.recipe_cooking_time);
        favoriteButton = view.findViewById(R.id.favoriteButton);

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

            recipe = new Recipe(title, ingredients, description, cookingTime, userId);

            titleTextView.setText(title);
            descriptionTextView.setText(description);
            ingredientsTextView.setText(TextUtils.join(", ", ingredients));
            cookingTimeTextView.setText("Cooking Time: " + cookingTime + " minutes");

            // Check if recipe is already a favorite and update UI
            checkIfFavorite();

            // Handle favorite button click
            favoriteButton.setOnClickListener(v -> toggleFavorite(userId, recipeId));
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
}



