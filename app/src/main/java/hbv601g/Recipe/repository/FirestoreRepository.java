package hbv601g.Recipe.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.entities.Review;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;
    private final CollectionReference reviewCollection;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes");
        reviewCollection = db.collection("reviews"); // Corrected this line
    }

    // 🔹 Add a new Recipe
    public void addRecipe(Recipe recipe) {
        recipeCollection.add(recipe)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Recipe added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding recipe: " + e.getMessage()));
    }

    // 🔹 Retrieve all Recipes (callback for ViewModel)
    public interface FirestoreCallback {
        void onRecipesLoaded(List<Recipe> recipes);
        void onReviewsLoaded(List<Review> reviews);
    }

    public void getRecipes(FirestoreCallback callback) {
        recipeCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Recipe> recipes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Recipe recipe = document.toObject(Recipe.class);

                                // Handle incorrectly stored ingredients
                                Object ingredientsObj = document.get("ingredients");
                                if (ingredientsObj instanceof String) {
                                    List<String> fixedIngredients = Arrays.asList(((String) ingredientsObj).split(", "));
                                    recipe.setIngredients(fixedIngredients);
                                }

                                recipes.add(recipe);
                            } catch (Exception e) {
                                System.err.println("Error parsing recipe: " + e.getMessage());
                            }
                        }
                        callback.onRecipesLoaded(recipes);
                    } else {
                        System.err.println("Error getting recipes: " + task.getException());
                    }
                });
    }

    /** Fetch reviews by recipe */
    public void getReviewsByRecipe(String recipeId, FirestoreCallback callback) {
        reviewCollection.whereEqualTo("recipeId", recipeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            reviews.add(review);
                        }
                        callback.onReviewsLoaded(reviews); // You may want to create a new callback for reviews
                    } else {
                        System.err.println("Error getting reviews: " + task.getException());
                    }
                });
    }

    /** Fetch reviews by rating */
    public void getReviewsByRating(int rating, FirestoreCallback callback) {
        reviewCollection.whereEqualTo("rating", rating)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            reviews.add(review);
                        }
                        callback.onRecipesLoaded(reviews); // You may want to create a new callback for reviews
                    } else {
                        System.err.println("Error getting reviews: " + task.getException());
                    }
                });
    }

    /** Add new review */
    public void addReview(Review review) {
        reviewCollection.add(review)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Review added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding review: " + e.getMessage()));
    }

    /** Update existing review */
    public void updateReview(String reviewId, String newComment) {
        reviewCollection.document(reviewId)
                .update("comment", newComment)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Review updated"))
                .addOnFailureListener(e ->
                        System.err.println("Error updating review: " + e.getMessage()));
    }

    /** Delete a review */
    public void deleteReview(String reviewId) {
        reviewCollection.document(reviewId).delete()
                .addOnSuccessListener(aVoid ->
                        System.out.println("Review deleted"))
                .addOnFailureListener(e ->
                        System.err.println("Error deleting review: " + e.getMessage()));
    }
}
