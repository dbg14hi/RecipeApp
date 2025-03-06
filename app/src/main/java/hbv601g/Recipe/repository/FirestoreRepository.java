package hbv601g.Recipe.repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.entities.Review;

public class FirestoreRepository {

    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;
    private final CollectionReference reviewCollection;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes");
        reviewCollection = db.collection("reviews");
    }

    // ================
    // RECIPE OPERATIONS
    // ================

    public void addRecipe(Recipe recipe) {
        recipeCollection.add(recipe)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Recipe added with ID: " + documentReference.getId()))
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
                    Integer cookingTime = document.getLong("cookingTime") != null ? document.getLong("cookingTime").intValue() : 0;  // Fetch cookingTime as int
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

    public interface ReviewCallback {
        void onReviewsLoaded(List<Review> reviews);
        void onFailure(Exception e);
    }

    public interface ReviewByIdCallback {
        void onReviewLoaded(Review review);
        void onFailure(Exception e);
    }
}
