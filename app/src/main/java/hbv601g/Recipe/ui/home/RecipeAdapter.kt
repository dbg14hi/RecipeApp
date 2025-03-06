package hbv601g.Recipe.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hbv601g.Recipe.R
import hbv601g.Recipe.entities.Recipe

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val listener: OnRecipeClickListener
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    interface OnRecipeClickListener {
        fun onRecipeClick(recipe: Recipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe)
        holder.itemView.setOnClickListener {
            listener.onRecipeClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateData(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(recipe: Recipe) {
            itemView.findViewById<TextView>(R.id.recipe_name).text = recipe.title
            itemView.findViewById<TextView>(R.id.recipe_ingredients).text = recipe.ingredients.joinToString(", ")

            itemView.setOnClickListener {
                listener.onRecipeClick(recipe)
            }
        }
    }
}



