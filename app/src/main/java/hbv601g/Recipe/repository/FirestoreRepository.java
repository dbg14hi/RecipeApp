package hbv601g.Recipe.repository;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.entities.Review;

public class FirestoreRepository {

    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;
    private final CollectionReference reviewCollection;
    private final CollectionReference usersCollection;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes");
        reviewCollection = db.collection("reviews");
        usersCollection = db.collection("users");
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // ================
    // RECIPE OPERATIONS
    // ================

    public void addRecipe(Recipe recipe) {
        recipeCollection.add(recipe)
                .addOnSuccessListener(documentReference -> {
                    String recipeId = documentReference.getId();

                    documentReference.update("recipeId", recipeId)
                            .addOnSuccessListener(aVoid ->
                                    System.out.println("Recipe successfully added with ID: " + recipeId))
                            .addOnFailureListener(e ->
                                    System.err.println("Failed to update recipe ID: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        System.err.println("Error adding recipe: " + e.getMessage()));
    }


    public void getRecipes(RecipeCallback callback) {
        recipeCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Recipe> recipes = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    System.out.println("📄 Document data: " + document.getData());

                    String recipeId = document.getId();
                    String title = document.getString("title");  // Fetch title (instead of name)
                    String description = document.getString("description");  // Fetch description
                    int cookingTime = document.getLong("cookingTime") != null ? document.getLong("cookingTime").intValue() : 0;
                    Object ingredientsObj = document.get("ingredients");
                    Object dietaryRestrictionsObj = document.get("dietaryRestrictions");
                    Object mealCategoriesObj = document.get("mealCategories");
                    Timestamp timestamp = document.getTimestamp("timestamp"); // Get timestamp

                    List<String> ingredients = new ArrayList<>();
                    if (ingredientsObj instanceof String) {
                        ingredients = Arrays.asList(((String) ingredientsObj).split(",\\s*"));
                    } else if (ingredientsObj instanceof List) {
                        ingredients = (List<String>) ingredientsObj;
                    }

                    List<String> dietaryRestrictions = new ArrayList<>();
                    if (dietaryRestrictionsObj instanceof String) {
                        dietaryRestrictions = Arrays.asList(((String) dietaryRestrictionsObj).split(",\\s*"));
                    } else if (dietaryRestrictionsObj instanceof List) {
                        dietaryRestrictions = (List<String>) dietaryRestrictionsObj;
                    }

                    List<String> mealCategories = new ArrayList<>();
                    if (mealCategoriesObj instanceof String) {
                        mealCategories = Arrays.asList(((String) mealCategoriesObj).split(",\\s*"));
                    } else if (mealCategoriesObj instanceof List) {
                        mealCategories = (List<String>) mealCategoriesObj;
                    }

                    Recipe recipe = new Recipe();
                    recipe.setRecipeId(recipeId);
                    recipe.setTitle(title);
                    recipe.setDescription(description);
                    recipe.setCookingTime(cookingTime);
                    recipe.setIngredients(ingredients);
                    recipe.setDietaryRestrictions(dietaryRestrictions);
                    recipe.setMealCategories(mealCategories);
                    recipe.setTimestamp(timestamp);  // Set timestamp

                    recipes.add(recipe);
                }
                System.out.println("Loaded " + recipes.size() + " recipes");
                callback.onRecipesLoaded(recipes);
            } else {
                System.err.println("Failed to load recipes: " + task.getException());
                callback.onFailure(task.getException());
            }
        });
    }

    // ================
    // FAVORITES OPERATIONS
    // ================

    public void getFavoriteRecipeIds(String userId, FirestoreCallback<List<String>> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> ids = (List<String>) doc.get("favorites");
                        callback.onSuccess(ids != null ? ids : new ArrayList<>());
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void removeFavorite(String userId, String recipeId, FirestoreCallback<Void> callback) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> favorites = (List<String>) doc.get("favorites");
                if (favorites != null && favorites.remove(recipeId)) {
                    userRef.update("favorites", favorites)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure);
                }
            }
        }).addOnFailureListener(callback::onFailure);
    }

    public void getRecipesByIds(List<String> recipeIds, RecipeCallback callback) {
        if (recipeIds == null || recipeIds.isEmpty()) {
            callback.onRecipesLoaded(new ArrayList<>()); // Return empty list if no favorites
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Recipe> recipesList = new ArrayList<>();

        for (String recipeId : recipeIds) {
            db.collection("recipes").document(recipeId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Recipe recipe = documentSnapshot.toObject(Recipe.class);
                            recipesList.add(recipe);
                        }

                        if (recipesList.size() == recipeIds.size()) {
                            callback.onRecipesLoaded(recipesList);
                        }
                    })
                    .addOnFailureListener(e -> callback.onFailure(e));
        }
    }

    // ================
    // REVIEW OPERATIONS
    // ================

    public void addReview(Review review) {
        reviewCollection.add(review)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Review added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding review: " + e.getMessage()));
    }

    public void getReviewsByRecipe(String recipeId, ReviewCallback callback) {
        reviewCollection.whereEqualTo("recipeId", recipeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            reviews.add(review);
                        }
                        callback.onReviewsLoaded(reviews);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getReviewById(String reviewId, ReviewByIdCallback callback) {
        reviewCollection.document(reviewId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Review review = documentSnapshot.toObject(Review.class);
                        callback.onReviewLoaded(review);
                    } else {
                        callback.onReviewLoaded(null);  // Review not found
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateReview(String reviewId, String newComment) {
        reviewCollection.document(reviewId)
                .update("comment", newComment)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Review updated"))
                .addOnFailureListener(e ->
                        System.err.println("Error updating review: " + e.getMessage()));
    }

    public void deleteReview(String reviewId) {
        reviewCollection.document(reviewId).delete()
                .addOnSuccessListener(aVoid ->
                        System.out.println("Review deleted"))
                .addOnFailureListener(e ->
                        System.err.println("Error deleting review: " + e.getMessage()));
    }

    // ================
    // CALLBACK INTERFACES
    // ================

    public interface RecipeCallback {
        void onRecipesLoaded(List<Recipe> recipes);
        void onFailure(Exception e);
    }

    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }

    public interface ReviewCallback {
        void onReviewsLoaded(List<Review> reviews);
        void onFailure(Exception e);
    }

    public interface ReviewByIdCallback {
        void onReviewLoaded(Review review);
        void onFailure(Exception e);
    }

}
