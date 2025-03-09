package hbv601g.Recipe.fragments.recipe;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

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

            checkIfFavorite();

            favoriteButton.setOnClickListener(v -> toggleFavorite());
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
        repository.isRecipeFavorite(userId, recipeId, isFav -> {
            isFavorite = isFav;
            updateFavoriteIcon(isFavorite);
        });
    }

    private void toggleFavorite() {
        if (isFavorite) {
            repository.removeRecipeFromFavorites(userId, recipeId);
            isFavorite = false;
        } else {
            repository.addRecipeToFavorites(userId, recipe);
            isFavorite = true;
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
}



