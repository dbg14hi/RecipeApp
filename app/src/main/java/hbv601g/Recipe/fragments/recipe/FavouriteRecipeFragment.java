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

public class FavouriteRecipeFragment extends Fragment {
    private FirestoreRepository repository;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> favoriteRecipes;
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new FirestoreRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get logged-in user
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_recipe, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        favoriteRecipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(favoriteRecipes, this::removeFromFavorites);
        recyclerView.setAdapter(recipeAdapter);

        loadFavoriteRecipes();
        return view;
    }

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

    private void removeFromFavorites(Recipe recipe) {
        repository.removeRecipeFromFavorites(userId, recipe.getRecipeId());
        favoriteRecipes.remove(recipe);
        recipeAdapter.notifyDataSetChanged();
    }
}

