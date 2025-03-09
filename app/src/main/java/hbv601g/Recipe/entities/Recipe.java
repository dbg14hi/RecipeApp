package hbv601g.Recipe.entities;

import java.util.Arrays;
import java.util.List;

public class Recipe {
    private String recipeId;
    private String title;
    private String description;
    private List<String> ingredients;
    private int cookingTime;
    private String userId;

    public Recipe() {}

    public Recipe(String title, Object ingredients, String description, int cookingTime, String userId) {
        this.title = title;
        setIngredients(ingredients);
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
    public void setIngredients(Object ingredients) {
        if (ingredients instanceof List) {
            this.ingredients = (List<String>) ingredients;
        } else if (ingredients instanceof String) {
            this.ingredients = Arrays.asList(((String) ingredients).split("\\s*,\\s*"));
        } else {
            this.ingredients = null;
        }
    }

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
