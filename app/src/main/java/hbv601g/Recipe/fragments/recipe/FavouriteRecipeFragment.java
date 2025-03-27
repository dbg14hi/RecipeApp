package hbv601g.Recipe.fragments.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.repository.FirestoreRepository;
import hbv601g.Recipe.ui.home.RecipeAdapter;

/**
 * A Fragment that displays the user's favorite recipes using Firestore.
 */
public class FavouriteRecipeFragment extends Fragment {

    private FirestoreRepository repository;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> favoriteRecipes;
    private String userId;

    /**
     * Initializes the fragment and retrieves the current user's ID.
     *
     * @param savedInstanceState A Bundle containing the saved state of the fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new FirestoreRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get logged-in user ID
    }

    /**
     * Inflates the layout for this fragment and sets up the RecyclerView.
     *
     * @param inflater           The LayoutInflater used to inflate views.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Saved state bundle for restoring state.
     * @return The created View instance for the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_recipe, container, false);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteRecipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(favoriteRecipes, this::removeFromFavorites);
        recyclerView.setAdapter(recipeAdapter);

        // Load the user's favorite recipes from Firestore
        loadFavoriteRecipes();

        return view;
    }

    /**
     * Loads the user's favorite recipes from Firestore and updates the RecyclerView.
     */
    private void loadFavoriteRecipes() {
        repository.getUserFavorites(userId, new FirestoreRepository.RecipeCallback() {
            @Override
            public void onRecipesLoaded(List<Recipe> recipes) {
                favoriteRecipes.clear();
                favoriteRecipes.addAll(recipes);
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Removes a recipe from the user's favorites in Firestore and updates the UI.
     *
     * @param recipe The recipe to be removed from favorites.
     */
    private void removeFromFavorites(Recipe recipe) {
        repository.removeRecipeFromFavorites(userId, recipe.getRecipeId());
        favoriteRecipes.remove(recipe);
        recipeAdapter.notifyDataSetChanged();
    }
}
