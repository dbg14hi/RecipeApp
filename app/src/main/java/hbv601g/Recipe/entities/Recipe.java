package hbv601g.Recipe.entities;

import android.media.Rating;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Recipe {
    private String recipeId;  // Not stored in Firestore - set manually
    private String title;
    private String description;
    private List<String> ingredients;
    private int cookingTime;
    private String userId;  // Fixed from userID

    public Recipe() {}

    public Recipe(String title, List<String> ingredients, String description, int cookingTime, String userId) {
        this.title = title;
        this.ingredients = ingredients;
        this.description = description;
        this.cookingTime = cookingTime;
        this.userId = userId;
    }

    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + recipeId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                ", cookingTime=" + cookingTime +
                ", userId='" + userId + '\'' +
                '}';
    }
}
