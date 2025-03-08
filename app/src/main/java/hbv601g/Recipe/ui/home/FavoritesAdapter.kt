package hbv601g.Recipe.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hbv601g.Recipe.databinding.ItemFavoriteRecipeBinding
import hbv601g.Recipe.entities.Recipe

class FavoritesAdapter(
    private val favoriteRecipes: MutableList<Recipe>,
    private val onRemoveClick: (Recipe) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    inner class FavoritesViewHolder(private val binding: ItemFavoriteRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipeTitle.text = recipe.title
            binding.recipeDescription.text = recipe.description

            binding.removeFavoriteButton.setOnClickListener {
                onRemoveClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val binding = ItemFavoriteRecipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FavoritesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        holder.bind(favoriteRecipes[position])
    }

    override fun getItemCount(): Int = favoriteRecipes.size

    // Remove a recipe from the list and notify adapter
    fun removeRecipe(recipe: Recipe) {
        val position = favoriteRecipes.indexOf(recipe)
        if (position != -1) {
            favoriteRecipes.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}

