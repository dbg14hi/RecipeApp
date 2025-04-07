package hbv601g.Recipe.ui.favorites;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import hbv601g.Recipe.R;
import hbv601g.Recipe.databinding.FragmentFavoritesBinding;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.repository.FirestoreRepository;
import hbv601g.Recipe.ui.favorites.FavoritesAdapter;

/**
 * A fragment for the favorites recipes of the user, for seeing the favorite recipe and removing it
 * from favorites.
 */
public class FavoritesFragment extends Fragment {
    private FragmentFavoritesBinding binding;
    private FirebaseAuth auth;
    private FirestoreRepository firestoreRepository;
    private RecyclerView favoritesRecyclerView;
    private List<Recipe> favoriteRecipes;
    private FavoritesAdapter favoritesAdapter;

    /**
     * Inflates the layout and sets up necessary components.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = FirebaseAuth.getInstance();

        firestoreRepository = new FirestoreRepository();

        favoritesRecyclerView = binding.favoritesRecyclerView;
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        favoriteRecipes = new ArrayList<>();
        favoritesAdapter = new FavoritesAdapter(favoriteRecipes, this::removeFromFavorites, this::navigateToRecipeDetails);
        favoritesRecyclerView.setAdapter(favoritesAdapter);

        if (auth.getCurrentUser() != null) {
            loadUserFavorites(auth.getCurrentUser().getUid());
        }
        return root;
    }

    /**
     * Clears the binding reference when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Navigation to the recipe clicked.
     *
     * @param recipe The recipe that was clicked.
     */
    private void navigateToRecipeDetails(Recipe recipe) {
        Bundle bundle = new Bundle();
        bundle.putString("recipeId", recipe.getRecipeId());

        Navigation.findNavController(requireView()).navigate(R.id.action_navigation_favorites_to_recipeDetailFragment, bundle);
    }

    /**
     * Remove recipe from User favorites
     *
     * @param recipe The recipe that was moved from the users favorite.
     */
    private void removeFromFavorites(Recipe recipe) {
        String userId = auth.getCurrentUser().getUid();
        firestoreRepository.removeFavorite(userId, recipe.getRecipeId(), new FirestoreRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                favoritesAdapter.removeRecipe(recipe);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                Log.e("Favorites", "Error removing favorite", e);
            }
        });
    }

    /**
     * Fetch and display favorite recipes.
     *
     * @param userId The users Id.
     */
    private void loadUserFavorites(String userId) {
        firestoreRepository.getFavoriteRecipeIds(userId, new FirestoreRepository.FirestoreCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> favoriteRecipeIds) {
                if (favoriteRecipeIds.isEmpty()) {
                    Log.d("Favorites", "No favorites found.");
                    favoritesRecyclerView.setVisibility(View.GONE);
                    return;
                }
                firestoreRepository.getRecipesByIds(favoriteRecipeIds, new FirestoreRepository.RecipeCallback() {
                    @Override
                    public void onRecipesLoaded(List<Recipe> recipes) {
                        favoriteRecipes.clear();
                        favoriteRecipes.addAll(recipes);
                        favoritesAdapter.notifyDataSetChanged();
                        favoritesRecyclerView.setVisibility(
                                favoriteRecipes.isEmpty() ? View.GONE : View.VISIBLE
                        );
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Favorites", "Failed to fetch recipes", e);
                        Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("Favorites", "Failed to fetch favorite IDs", e);
            }
        });
    }
}
