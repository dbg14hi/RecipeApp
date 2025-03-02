package hbv601g.Recipe.entities;

import android.media.Rating;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Recipe {
    private Long recipeId;
    private String title;
    private String description;
    private List<String> ingredients;
    private int cookingTime;
    private User user;
//    private HashSet<MealCategory> mealCategory;
//    private HashSet<DietaryRestriction> dieteryRestrictions;
//    private final String recipePictureUrl;
//    private List<Review> reviews;

//    private List<User> usersWhoFavorited;
//    private int rating;
//    private List<Integer> ratings;

    public Recipe(){

    }

//

    public Recipe(String title, List<String> ingredients, String description, int cookingTime) {
        this.title = title;
        this.ingredients = ingredients;
        this.description = description;
        this.cookingTime = cookingTime;
        this.user = user;
//        this.usersWhoFavorited = new List<>();
//        this.reviews = new List<Review>();
//        this.ratings = new HashSet<>();
//
//        this.dietaryRestrictions = dietaryRestrictions;
//        this.mealCategories = mealCategories;
//
//        this.uploadTime = LocalDateTime.now();
//        this.recipePictureUrl = recipePictureUrl;

    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookTime(int cookTime) {
        this.cookingTime = cookingTime;
    }

//    public Set<DietaryRestriction> getDietaryRestrictions() {
//        return dietaryRestrictions;
//    }
//
//    public void setDietaryRestrictions(Set<DietaryRestriction> dietaryRestrictions) {
//        this.dietaryRestrictions = dietaryRestrictions;
//    }
//
//    public Set<MealCategory> getMealCategories() {
//        return mealCategories;
//    }
//
//    public void setMealCategories(Set<MealCategory> mealCategories) {
//        this.mealCategories = mealCategories;
//    }
//    public void addRating(int rating) {
//        this.ratings.add(rating);
//        updateRatings();
//    }
//
//    public void updateRatings() {
//
//        int ratingSum = 0;
//        for (int i : ratings) {
//            ratingSum += i;
//        }
//
//        double ratingAvg = (double) ratingSum / ratings.size();
//        if (ratingAvg > 4.5) {
//            this.rating = 5;
//        }
//        else if (ratingAvg > 3.5) {
//            this.rating = 4;
//        }
//        else if (ratingAvg > 2.5) {
//            this.rating = 3;
//        }
//        else if (ratingAvg > 1.5) {
//            this.rating = 2;
//        }
//        else {
//            this.rating = 1;
//        }
//    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

//    public HashSet<User> getUsersWhoFavorited() {
//        return usersWhoFavorited;
//    }
//
//    public void setUsersWhoFavorited(HashSet<User> usersWhoFavorited) {
//        this.usersWhoFavorited = usersWhoFavorited;
//    }

//    public LocalDateTime getUploadTime() {
//        return uploadTime;
//    }
//
//    public void setUploadTime(LocalDateTime uploadTime) {
//        this.uploadTime = uploadTime;
//    }
//
//    public String getRecipePictureUrl() {
//        return recipePictureUrl;
//    }
//
//    public void setRecipePictureUrl(String recipePictureUrl) {
//        this.recipePictureUrl = recipePictureUrl;
//    }
//
//    public HashSet<Review> getReviews() {
//        return reviews;
//    }
//
//    public void setReviews(HashSet<Review> reviews) {
//        this.reviews = reviews;
//    }
//
//    public HashSet<Integer> getRatings() { return ratings; }
//
//    public void setRatings(HashSet<Integer> ratings) { this.ratings = ratings; }
//
//    public int getRating() {
//        return rating;
//    }
//    public void setRating(int rating) {
//        this.rating = rating;
//    }

    @Override
    public String toString() {
        return "Id: " + recipeId + ", Title: " + title + ", Ingredients: " + ingredients + ", Cook Time: " + cookingTime + ", Description: " + description;
    }
}

