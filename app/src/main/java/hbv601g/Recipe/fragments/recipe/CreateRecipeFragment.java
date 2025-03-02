package hbv601g.Recipe.fragments.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import hbv601g.Recipe.R;

public class CreateRecipeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText titleInput, descriptionInput, ingredientsInput, cookingTimeInput;
    private Button submitRecipeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_recipe, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        titleInput = view.findViewById(R.id.titleInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        ingredientsInput = view.findViewById(R.id.ingredientsInput);
        cookingTimeInput = view.findViewById(R.id.cookingTimeInput);
        submitRecipeButton = view.findViewById(R.id.submitRecipeButton);

        submitRecipeButton.setOnClickListener(v -> createRecipe());

        return view;
    }

    private void createRecipe() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String ingredients = ingredientsInput.getText().toString().trim();
        String cookingTimeStr = cookingTimeInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || ingredients.isEmpty() || cookingTimeStr.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        int cookingTime = Integer.parseInt(cookingTimeStr);
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> recipe = new HashMap<>();
        recipe.put("title", title);
        recipe.put("description", description);
        recipe.put("ingredients", ingredients);
        recipe.put("cookingTime", cookingTime);
        recipe.put("userId", userId);

        db.collection("recipes").add(recipe)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Recipe added!", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add recipe", Toast.LENGTH_SHORT).show());
    }
}

