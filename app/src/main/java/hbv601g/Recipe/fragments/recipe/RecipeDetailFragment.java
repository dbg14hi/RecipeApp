package hbv601g.Recipe.fragments.recipe;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import hbv601g.Recipe.R;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import hbv601g.Recipe.repository.FirestoreRepository;
import hbv601g.Recipe.entities.Recipe;

public class RecipeDetailFragment extends Fragment {

    private TextView titleTextView, descriptionTextView, ingredientsTextView, cookingTimeTextView;
    private ImageButton favoriteButton;
    private FirestoreRepository repository;
    private String userId;
    private Recipe recipe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = new FirestoreRepository();

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        titleTextView = view.findViewById(R.id.recipe_title);
        descriptionTextView = view.findViewById(R.id.recipe_description);
        ingredientsTextView = view.findViewById(R.id.recipe_ingredients);
        cookingTimeTextView = view.findViewById(R.id.recipe_cooking_time);
        favoriteButton = view.findViewById(R.id.favoriteButton); // The favorite button

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("recipeTitle");
            String description = args.getString("recipeDescription");
            ArrayList<String> ingredients = args.getStringArrayList("recipeIngredients");
            int cookingTime = args.getInt("recipeCookingTime");
            String recipeId = args.getString("recipeId");


            recipe = new Recipe(title, ingredients, description, cookingTime, recipeId);


            titleTextView.setText(title);
            descriptionTextView.setText(description);
            ingredientsTextView.setText(TextUtils.join(", ", ingredients));
            cookingTimeTextView.setText("Cooking Time: " + cookingTime + " minutes");


            repository.isRecipeFavorite(userId, recipeId, isFavorite -> {
                updateFavoriteIcon(isFavorite);

                favoriteButton.setOnClickListener(v -> {
                    if (isFavorite) {
                        repository.removeRecipeFromFavorites(userId, recipeId);
                        updateFavoriteIcon(false);
                    } else {
                        repository.addRecipeToFavorites(userId, recipe);  // Pass the Recipe object
                        updateFavoriteIcon(true);
                    }
                });
            });
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

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_heart_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_heart_empty);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
}


