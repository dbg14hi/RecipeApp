package hbv601g.Recipe.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import hbv601g.Recipe.entities.Recipe;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes");
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

}
