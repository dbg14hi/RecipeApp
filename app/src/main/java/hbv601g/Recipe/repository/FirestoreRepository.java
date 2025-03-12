package hbv601g.Recipe.repository;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                    Integer cookingTime = document.getLong("cookingTime") != null ? document.getLong("cookingTime").intValue() : 0;
                    Object ingredientsObj = document.get("ingredients");

                    List<String> ingredients = new ArrayList<>();
                    if (ingredientsObj instanceof String) {
                        ingredients = Arrays.asList(((String) ingredientsObj).split(",\\s*"));
                    } else if (ingredientsObj instanceof List) {
                        ingredients = (List<String>) ingredientsObj;
                    }

                    Recipe recipe = new Recipe();
                    recipe.setRecipeId(recipeId);
                    recipe.setTitle(title);
                    recipe.setDescription(description);
                    recipe.setCookingTime(cookingTime);
                    recipe.setIngredients(ingredients);

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

    public void addRecipeToFavorites(String userId, Recipe recipe) {
        if (recipe == null || recipe.getRecipeId() == null || recipe.getRecipeId().isEmpty()) {
            Log.e("FirestoreRepository", "Error: Recipe object is null or missing an ID");
            return;
        }

        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("recipeId", recipe.getRecipeId());
        favoriteData.put("title", recipe.getTitle());
        favoriteData.put("description", recipe.getDescription());
        favoriteData.put("cookingTime", recipe.getCookingTime());
        favoriteData.put("ingredients", recipe.getIngredients());

        usersCollection.document(userId)
                .collection("favorites")
                .document(recipe.getRecipeId())  // Creates a document inside 'favorites'
                .set(favoriteData)  // Stores the full recipe data
                .addOnSuccessListener(aVoid ->
                        Log.d("FirestoreRepository", "Recipe added to favorites for user: " + userId))
                .addOnFailureListener(e ->
                        Log.e("FirestoreRepository", "Error adding recipe to favorites: " + e.getMessage()));
    }


    public void removeRecipeFromFavorites(String userId, String recipeId) {
        if (userId == null || userId.isEmpty() || recipeId == null || recipeId.isEmpty()) {
            Log.e("FirestoreRepository", "Error: User ID or Recipe ID is null or empty!");
            return;
        }

        usersCollection.document(userId)
                .collection("favorites")
                .document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("FirestoreRepository", "Recipe removed from favorites for user: " + userId))
                .addOnFailureListener(e ->
                        Log.e("FirestoreRepository", "Error removing recipe from favorites: " + e.getMessage()));
    }


    public void getUserFavorites(String userId, RecipeCallback callback) {
        if (userId == null || userId.isEmpty()) {
            System.err.println("User ID is null or empty!");
            callback.onFailure(new Exception("User ID is required"));
            return;
        }

        usersCollection.document(userId)
                .collection("favorites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Recipe> favorites = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            favorites.add(recipe);
                        }
                        callback.onRecipesLoaded(favorites);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void isRecipeFavorite(String userId, String recipeId, FavoriteCallback callback) {
        if (userId == null || userId.isEmpty() || recipeId == null || recipeId.isEmpty()) {
            System.err.println("User ID or Recipe ID is null or empty!");
            callback.onResult(false);
            return;
        }

        usersCollection.document(userId)
                .collection("favorites")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onResult(documentSnapshot.exists());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error checking favorite status: " + e.getMessage());
                    callback.onResult(false);
                });
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
                        callback.onReviewLoaded(null);
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
    // Filter OPERATIONS
    // ================

    //skoða betur, ekki búið
    public void getFilteredRecipes(@NotNull String title, int cookingTime, @NotNull FirestoreRepository.RecipeCallback callback) {
        Query query = db.collection("recipes");

        if (!title.isEmpty()) {
            query = query.whereEqualTo("title", title);
        }
        if (cookingTime > 0) {
            query = query.whereEqualTo("cookingTime", cookingTime);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        recipes.add(recipe);
                    }
                    callback.onRecipesLoaded(recipes);
                })
                .addOnFailureListener(callback::onFailure);
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
