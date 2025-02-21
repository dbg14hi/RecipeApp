package hbv601g.Recipe.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes"); // Firestore collection
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
}
