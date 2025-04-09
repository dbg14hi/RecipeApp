package hbv601g.Recipe.fragments.recipe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import hbv601g.Recipe.R;

/**
 * Fragment that allows users to edit an existing recipe.
 * It pre-fills the current recipe data and updates it in Firestore upon confirmation.
 */
public class EditRecipeFragment extends Fragment {

    private EditText titleEditText, descriptionEditText, ingredientsEditText, cookingTimeEditText;
    private Button saveButton;
    private String recipeId;

    private FirebaseFirestore db;

    public EditRecipeFragment() {
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_recipe, container, false);

        titleEditText = view.findViewById(R.id.edit_recipe_title);
        descriptionEditText = view.findViewById(R.id.edit_recipe_description);
        ingredientsEditText = view.findViewById(R.id.edit_recipe_ingredients);
        cookingTimeEditText = view.findViewById(R.id.edit_recipe_cooking_time);
        saveButton = view.findViewById(R.id.button_save_recipe);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            recipeId = getArguments().getString("recipeId");
            titleEditText.setText(getArguments().getString("recipeTitle"));
            descriptionEditText.setText(getArguments().getString("recipeDescription"));
            ingredientsEditText.setText(TextUtils.join(", ", getArguments().getStringArrayList("recipeIngredients")));
            cookingTimeEditText.setText(String.valueOf(getArguments().getInt("recipeCookingTime")));
        }

        saveButton.setOnClickListener(v -> saveChanges());

        return view;
    }

    /**
     * Saves the modified recipe data back to Firestore.
     * Validates inputs before performing the update.
     */
    private void saveChanges() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String ingredientsInput = ingredientsEditText.getText().toString().trim();
        String cookingTimeStr = cookingTimeEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || ingredientsInput.isEmpty() || cookingTimeStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int cookingTime;
        try {
            cookingTime = Integer.parseInt(cookingTimeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid cooking time", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference recipeRef = db.collection("recipes").document(recipeId);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", title);
        updatedData.put("description", description);
        updatedData.put("ingredients", Arrays.asList(ingredientsInput.split("\\s*,\\s*")));
        updatedData.put("cookingTime", cookingTime);

        recipeRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update recipe", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
