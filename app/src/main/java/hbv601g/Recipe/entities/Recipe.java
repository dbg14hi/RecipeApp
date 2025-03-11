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
    private boolean isVegan;
    private String category;

    public Recipe() {}

    public Recipe(String title, Object ingredients, String description, int cookingTime, String userId, boolean isVegan, String category) {
        this.title = title;
        setIngredients(ingredients);
        this.description = description;
        this.cookingTime = cookingTime;
        this.userId = userId;
        this.isVegan = isVegan;
        this.category = category;
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

    public boolean isVegan() { return isVegan; }
    public void setVegan(boolean vegan) { isVegan = vegan; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + recipeId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                ", cookingTime=" + cookingTime +
                ", userId='" + userId + '\'' +
                ", isVegan=" + isVegan +  //
                ", category='" + category + '\'' +
                '}';
    }
}
