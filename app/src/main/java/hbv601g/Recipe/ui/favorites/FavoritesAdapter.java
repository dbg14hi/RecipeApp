package hbv601g.Recipe.ui.favorites;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

import hbv601g.Recipe.R;
import hbv601g.Recipe.databinding.ItemFavoriteRecipeBinding;
import hbv601g.Recipe.entities.Recipe;

/**
 * Adapter for displaying a list of favorite recipes in a RecyclerView.
 * Each recipe includes title, description, image, and a remove-from-favorites button.
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {
    private final List<Recipe> favoriteRecipes;
    private final OnRemoveClickListener onRemoveClick;
    private final OnRecipeClickListener onRecipeClick;

    /**
     * Interface for remove button action.
     *
     */
    public interface OnRemoveClickListener {
        void onRemoveClick(Recipe recipe);
    }

    /**
     * Interface to link to recipe.
     *
     */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    /**
     * Constructor for the adapter for the favorites list
     *
     * @param favoriteRecipes Favorite recipe.
     * @param onRemoveClick The listener for remove button.
     * @param onRecipeClick The listener for item click action.
     */
    public FavoritesAdapter(List<Recipe> favoriteRecipes, OnRemoveClickListener onRemoveClick, OnRecipeClickListener onRecipeClick) {
        this.favoriteRecipes = favoriteRecipes;
        this.onRemoveClick = onRemoveClick;
        this.onRecipeClick = onRecipeClick;
    }

    @Override
    public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFavoriteRecipeBinding binding = ItemFavoriteRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoritesViewHolder(binding, onRemoveClick, onRecipeClick);
    }

    @Override
    public void onBindViewHolder(FavoritesViewHolder holder, int position) {
        holder.bind(favoriteRecipes.get(position));
    }

    @Override
    public int getItemCount() {
        return favoriteRecipes.size();
    }

    /**
     * Removes a recipe from the list and updates the RecyclerView.
     *
     */
    public void removeRecipe(Recipe recipe) {
        int position = favoriteRecipes.indexOf(recipe);
        if (position != -1) {
            favoriteRecipes.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * ViewHolder class for binding individual favorite recipe cards.
     *
     */
    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavoriteRecipeBinding binding;
        private final OnRemoveClickListener onRemoveClick;
        private final OnRecipeClickListener onRecipeClick;

        public FavoritesViewHolder(ItemFavoriteRecipeBinding binding, OnRemoveClickListener onRemoveClick, OnRecipeClickListener onRecipeClick) {
            super(binding.getRoot());
            this.binding = binding;
            this.onRemoveClick = onRemoveClick;
            this.onRecipeClick = onRecipeClick;
        }

        /**
         * For binding the recipe data to the view.
         *
         * @param recipe The recipe to bind.
         */
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

            binding.getRoot().setOnClickListener(v -> onRecipeClick.onRecipeClick(recipe));
        }
    }
}
