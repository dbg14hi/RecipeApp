package hbv601g.Recipe.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;
    private final CollectionReference reviewCollection; // Add this line for reviews

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes"); // Firestore collection for recipes
        reviewCollection = db.collection("reviews"); // Firestore collection for reviews
    }

    // 🔹 Add a new Recipe
    public void addRecipe(String name, String ingredients) {
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("name", name);
        recipe.put("ingredients", ingredients);

        recipeCollection.add(recipe)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Recipe added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding recipe: " + e.getMessage()));
    }

    // 🔹 Retrieve all Recipes
    public void getRecipes() {
        recipeCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            System.out.println(document.getId() + " => " + document.getData());
                        }
                    } else {
                        System.err.println("Error getting recipes: " + task.getException());
                    }
                });
    }

    // 🔹 Update a Recipe
    public void updateRecipe(String recipeId, String newName) {
        recipeCollection.document(recipeId)
                .update("name", newName)
                .addOnSuccessListener(aVoid -> System.out.println("Recipe updated"))
                .addOnFailureListener(e -> System.err.println("Error updating recipe"));
    }

    // 🔹 Delete a Recipe
    public void deleteRecipe(String recipeId) {
        recipeCollection.document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> System.out.println("Recipe deleted"))
                .addOnFailureListener(e -> System.err.println("Error deleting recipe"));
    }

    // 🔹 Add a new Review
    public void addReview(String comment, int rating, String userId, String recipeId) {
        Map<String, Object> review = new HashMap<>();
        review.put("comment", comment);
        review.put("rating", rating);
        review.put("userId", userId);
        review.put("recipeId", recipeId);

        reviewCollection.add(review)
                .addOnSuccessListener(documentReference ->
                        System.out.println("Review added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding review: " + e.getMessage()));
    }

    // 🔹 Retrieve all Reviews for a specific recipe with a callback
    public void getReviewsByRecipe(String recipeId, FirestoreReviewCallback callback) {
        reviewCollection.whereEqualTo("recipeId", recipeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            review.setId(document.getId()); // Set the document ID
                            reviews.add(review);
                        }
                        callback.onReviewsLoaded(reviews); // Call the callback with the loaded reviews
                    } else {
                        System.err.println("Error getting reviews: " + task.getException());
                        callback.onReviewsLoaded(new ArrayList<>()); // Pass empty list on error
                    }
                });
    }

    // 🔹 Update a Review
    public void updateReview(String reviewId, String newComment) {
        reviewCollection.document(reviewId)
                .update("comment", newComment)
                .addOnSuccessListener(aVoid -> System.out.println("Review updated"))
                .addOnFailureListener(e -> System.err.println("Error updating review"));
    }

    // 🔹 Delete a Review
    public void deleteReview(String reviewId) {
        reviewCollection.document(reviewId)
                .delete()
                .addOnSuccessListener(aVoid -> System.out.println("Review deleted"))
                .addOnFailureListener(e -> System.err.println("Error deleting review"));
    }
}
