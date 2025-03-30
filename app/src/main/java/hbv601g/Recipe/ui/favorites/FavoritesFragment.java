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

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private FirebaseAuth auth;
    private FirestoreRepository firestoreRepository;
    private RecyclerView favoritesRecyclerView;
    private List<Recipe> favoriteRecipes;
    private FavoritesAdapter favoritesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = FirebaseAuth.getInstance();

        firestoreRepository = new FirestoreRepository();

        // Set up RecyclerView
        favoritesRecyclerView = binding.favoritesRecyclerView;
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        favoriteRecipes = new ArrayList<>();
        favoritesAdapter = new FavoritesAdapter(favoriteRecipes, this::removeFromFavorites, this::navigateToRecipeDetails);
        favoritesRecyclerView.setAdapter(favoritesAdapter);

        // Load user's favorites
        if (auth.getCurrentUser() != null) {
            loadUserFavorites(auth.getCurrentUser().getUid());
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Navigation to the recipe clicked
    private void navigateToRecipeDetails(Recipe recipe) {
        Bundle bundle = new Bundle();
        bundle.putString("recipeId", recipe.getRecipeId());

        Navigation.findNavController(requireView()).navigate(R.id.action_navigation_favorites_to_recipeDetailFragment, bundle);
    }

    // Remove recipe from User favorites
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

    // Fetch and display favorite recipes
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
