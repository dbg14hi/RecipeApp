package hbv601g.Recipe.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final CollectionReference recipeCollection;

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        recipeCollection = db.collection("recipes");
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

    // 🔹 Retrieve all Recipes (callback for ViewModel)
    public interface FirestoreCallback {
        void onRecipesLoaded(List<Map<String, Object>> recipes);
    }

    public void getRecipes(FirestoreCallback callback) {
        recipeCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> recipes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            recipes.add(document.getData());
                        }
                        callback.onRecipesLoaded(recipes);
                    } else {
                        System.err.println("Error getting recipes: " + task.getException());
                    }
                });
    }
}
