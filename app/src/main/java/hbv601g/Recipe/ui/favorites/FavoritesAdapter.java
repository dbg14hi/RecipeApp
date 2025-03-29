package hbv601g.Recipe.ui.favorites;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

import hbv601g.Recipe.R;
import hbv601g.Recipe.databinding.ItemFavoriteRecipeBinding;
import hbv601g.Recipe.entities.Recipe;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private final List<Recipe> favoriteRecipes;
    private final OnRemoveClickListener onRemoveClick;

    public FavoritesAdapter(List<Recipe> favoriteRecipes, OnRemoveClickListener onRemoveClick) {
        this.favoriteRecipes = favoriteRecipes;
        this.onRemoveClick = onRemoveClick;
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFavoriteRecipeBinding binding = ItemFavoriteRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoritesViewHolder(binding, onRemoveClick);
    }

    @Override
    public void onBindViewHolder(FavoritesViewHolder holder, int position) {
        holder.bind(favoriteRecipes.get(position));
    }

    @Override
    public int getItemCount() {
        return favoriteRecipes.size();
    }

    // Removes a recipe from the list and updates the RecyclerView
    public void removeRecipe(Recipe recipe) {
        int position = favoriteRecipes.indexOf(recipe);
        if (position != -1) {
            favoriteRecipes.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ViewHolder class for binding individual favorite recipe cards
    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavoriteRecipeBinding binding;
        private final OnRemoveClickListener onRemoveClick;

        public FavoritesViewHolder(ItemFavoriteRecipeBinding binding, OnRemoveClickListener onRemoveClick) {
            super(binding.getRoot());
            this.binding = binding;
            this.onRemoveClick = onRemoveClick;
        }

        public void bind(Recipe recipe) {
            binding.recipeTitle.setText(recipe.getTitle());
            binding.recipeDescription.setText(recipe.getDescription());

            Glide.with(binding.recipeImage.getContext())
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.recipeImage);

            binding.removeFavoriteButton.setOnClickListener(
                    v -> onRemoveClick.onRemoveClick(recipe)
            );
        }
    }

    // Interface for remove button action
    public interface OnRemoveClickListener {
        void onRemoveClick(Recipe recipe);
    }
}
