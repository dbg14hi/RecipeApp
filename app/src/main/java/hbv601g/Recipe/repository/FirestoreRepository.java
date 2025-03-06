package hbv601g.Recipe.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;

import org.jetbrains.annotations.Nullable;

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

    public void getReviewsByRecipe(@Nullable FirestoreRepository.FirestoreCallback firestoreCallback) {

    }


    // 🔹 Retrieve all Recipes (callback for ViewModel)
    public interface FirestoreCallback {
        void onRecipesLoaded(List<Recipe> recipes);
        void onReviewsLoaded(List<Review> reviews);
        void onFailure(Exception e); // Notify the caller of failures
        void onError(Exception e);
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
                        callback.onError(task.getException());
                    }
                });
    }


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
                        callback.onReviewsLoaded(reviews);
                    } else {
                        System.err.println("Error getting reviews: " + task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

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
                        callback.onReviewsLoaded(reviews);
                    } else {
                        System.err.println("Error getting reviews: " + task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void addReview(Review review) {
        reviewCollection.add(review)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Review added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding review: " + e.getMessage()));

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

    public void getReviewById(String reviewId, ReviewCallback callback) {
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


    public interface ReviewCallback {
        void onReviewLoaded(Review review);
        void onFailure(Exception e);
    }
}
