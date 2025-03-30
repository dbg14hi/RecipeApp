package hbv601g.Recipe.entities;

import java.util.ArrayList;
import com.google.firebase.Timestamp;
import java.util.Arrays;
import java.util.List;
/**
 * Represents a recipe in the Firestore database.
 * A recipe has recipeId, title, description, ingredients, cookingTime, dietaryRestrictions, mealCategories,
 * timestamp and userId.
 *
 * <p>This class is used for Firestore document mapping.
 */
public class Recipe {
    private String recipeId;
    private String title;
    private String description;
    private List<String> ingredients;
    private int cookingTime;
    private List<String> dietaryRestrictions;
    private List<String> mealCategories;
    private Timestamp timestamp;
    private String userId;
    private String imageUrl;

    /**
     * Default constructor required for Firestore
     */
    public Recipe() {
    }

    /**
     * Creates a new Recipe object.
     *
     * @param title The title of the recipe.
     * @param ingredients A list of ingredients or a comma-separated string.
     * @param description A brief description of the recipe.
     * @param cookingTime The cooking time in minutes.
     * @param dietaryRestrictions A list of dietary restrictions or a comma-separated string.
     * @param mealCategories A list of meal categories or a comma-separated string.
     * @param timestamp The timestamp when the recipe was created.
     * @param userId The ID of the user who created the recipe.
     */
    public Recipe (String title, Object ingredients, String description, int cookingTime, Object dietaryRestrictions, Object mealCategories, Timestamp timestamp, String userId) {
        this.title = title;
        this.userId = userId;
        this.description = description;
        this.cookingTime = cookingTime;
        setDietaryRestrictions(dietaryRestrictions);
        setMealCategories(mealCategories);
        setIngredients(ingredients);
    }

    /**
    * Getters and setters
     */
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
    public int getCookingTime() {return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(Object dietaryRestrictions) {
        if (dietaryRestrictions instanceof List<?>) {
            List<?> potentialList = (List<?>) dietaryRestrictions;
            if (potentialList.stream().allMatch(item -> item instanceof String)) {
                this.dietaryRestrictions = new ArrayList<>();
                for (Object item : potentialList) {
                    this.dietaryRestrictions.add((String) item);
                }
            }
        } else if (dietaryRestrictions instanceof String) {
            this.dietaryRestrictions = Arrays.asList(((String) dietaryRestrictions).split("\\s*,\\s*"));
        } else {
            this.dietaryRestrictions = null;
        }
    }
    public List<String> getMealCategories() { return mealCategories; }
    public void setMealCategories(Object mealCategories) {
        if (mealCategories instanceof List<?>) {
            List<?> potentialList = (List<?>) mealCategories;
            if (potentialList.stream().allMatch(item -> item instanceof String)) {
                this.mealCategories = new ArrayList<>();
                for (Object item : potentialList) {
                    this.mealCategories.add((String) item);
                }
            }
        } else if (mealCategories instanceof String) {
            this.mealCategories = Arrays.asList(((String) mealCategories).split("\\s*,\\s*"));
        } else {
            this.mealCategories = null;
        }
    }

    public Timestamp getTimestamp() { return timestamp; }  // Getter for timestamp
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }  // Setter for timestamp

    /**
     * Returns a string representation of the Recipe object.
     * @return A string containing all recipe details.
     */
    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + recipeId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                ", cookingTime=" + cookingTime +
                ", dietaryRestrictions=" + dietaryRestrictions +
                ", mealCategories=" + mealCategories +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
