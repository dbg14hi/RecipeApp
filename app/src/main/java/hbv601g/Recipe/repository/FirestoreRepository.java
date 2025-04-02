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

/**
 * A repository for handling Firestore database operations for recipe, reviews and favorites.
 *
 */
public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;
    private final CollectionReference reviewCollection;
    private final CollectionReference usersCollection;

    /**
     * Initalizes the Firestore repository.
     */
    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes");
        reviewCollection = db.collection("reviews");
        usersCollection = db.collection("users");
    }

    /**
     * Interface for Firestore callback.
     *
     * @param <T> The type of result expected from the operation.
     */
    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // ================
    // RECIPE OPERATIONS
    // ================

    /**
     * Handles the adding of a recipe to the Firestore database.
     *
     * @param recipe The recipe added to the database.
     */
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

    /**
     * Gets the recipe from the Firestore database.
     *
     * @param callback Handles the callback for getting the recipe.
     */
    public void getRecipes(RecipeCallback callback) {
        recipeCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Recipe> recipes = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    System.out.println("📄 Document data: " + document.getData());

                    String recipeId = document.getId();
                    String title = document.getString("title");
                    String description = document.getString("description");
                    int cookingTime = document.getLong("cookingTime") != null ? document.getLong("cookingTime").intValue() : 0;
                    Object ingredientsObj = document.get("ingredients");
                    Object dietaryRestrictionsObj = document.get("dietaryRestrictions");
                    Object mealCategoriesObj = document.get("mealCategories");
                    Timestamp timestamp = document.getTimestamp("timestamp");

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
                    recipe.setTimestamp(timestamp);

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

    /**
     * For getting the Id of the recipe that is users favorite.
     *
     * @param userId The Id of the user that has the favorite recipe.
     * @param callback The callback for the retrieval of the favorite recipe.
     */
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

    /**
     * Handles the removal of the favorite recipe from the database.
     *
     * @param userId The Id of the user who has the favorite recipe.
     * @param recipeId The Id of the recipe.
     * @param callback The callback for the removal of the favorite recipe.
     */
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

    /**
     * Handles getting the list of recipes from the database.
     *
     * @param recipeIds The list of recipe Ids from the database.
     * @param callback Callback for getting the list of recipe Ids.
     */
    public void getRecipesByIds(List<String> recipeIds, RecipeCallback callback) {
        if (recipeIds == null || recipeIds.isEmpty()) {
            callback.onRecipesLoaded(new ArrayList<>());
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

    /**
     * Adds a review to the Firestore database.
     *
     * @param review The review added to the database.
     */
    public void addReview(Review review) {
        reviewCollection.add(review)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Review added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding review: " + e.getMessage()));
    }

    /**
     * Gets the reviews for every recipe from the database.
     *
     */
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

    /**
     * Get the Id of the review from the database.
     *
     * @param reviewId The Id of the review.
     * @param callback The callback for getting the Review Id.
     */
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

    /**
     * Updates the review in the database.
     *
     * @param reviewId The updated reviewId.
     * @param newComment The new comment for the review.
     */
    public void updateReview(String reviewId, String newComment) {
        reviewCollection.document(reviewId)
                .update("comment", newComment)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Review updated"))
                .addOnFailureListener(e ->
                        System.err.println("Error updating review: " + e.getMessage()));
    }

    /**
     * Deletes the review from the database.
     *
     * @param reviewId The review Id.
     */
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

    /**
     * The callback interface for the recipe for the Firestore database.
     */
    public interface RecipeCallback {
        void onRecipesLoaded(List<Recipe> recipes);
        void onFailure(Exception e);
    }

    /**
     * The callback interface for the favorites recipe for the Firestore database.
     */
    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }

    /**
     * The callback interface for the review for the Firestore database.
     */
    public interface ReviewCallback {
        void onReviewsLoaded(List<Review> reviews);
        void onFailure(Exception e);
    }

    /**
     * The callback interface for the review by Id for the Firestore database.
     */
    public interface ReviewByIdCallback {
        void onReviewLoaded(Review review);
        void onFailure(Exception e);
    }
}
