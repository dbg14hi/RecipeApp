package hbv601g.Recipe.fragments.recipe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import hbv601g.Recipe.R;


public class CreateRecipeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText titleInput, descriptionInput, ingredientsInput, cookingTimeInput;
    private Button submitRecipeButton;

    private CheckBox checkboxVegan;

    private Spinner categorySpinner;



    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_recipe, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        titleInput = view.findViewById(R.id.titleInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        ingredientsInput = view.findViewById(R.id.ingredientsInput);
        cookingTimeInput = view.findViewById(R.id.cookingTimeInput);
        submitRecipeButton = view.findViewById(R.id.submitRecipeButton);
        categorySpinner = view.findViewById(R.id.categorySpinner);

        String[] categories = {"Select category", "Vegetarian", "Vegan", "Nut-free", "Gluten-free", "Dairy-free"};


        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(adapter);


        submitRecipeButton.setOnClickListener(v -> createRecipe());

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        NavHostFragment.findNavController(CreateRecipeFragment.this).navigateUp();
                    }
                }
        );

        return view;
    }

    private void createRecipe() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String ingredientsText = ingredientsInput.getText().toString().trim();
        String cookingTimeStr = cookingTimeInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || ingredientsText.isEmpty() || cookingTimeStr.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> ingredients = Arrays.asList(ingredientsText.split("\\s*,\\s*"));


        int cookingTime;
        try {
            cookingTime = Integer.parseInt(cookingTimeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Cooking time must be a number!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();


        Map<String, Object> recipe = new HashMap<>();
        recipe.put("title", title);
        recipe.put("description", description);
        recipe.put("ingredients", ingredients);
        recipe.put("cookingTime", cookingTime);
        recipe.put("userId", userId);
        recipe.put("timestamp", FieldValue.serverTimestamp());

        db.collection("recipes").add(recipe)
                .addOnSuccessListener(documentReference -> {
                    String recipeId = documentReference.getId();

                    documentReference.update("recipeId", recipeId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Recipe added!", Toast.LENGTH_SHORT).show();

                                // Navigate back to HomeFragment
                                NavHostFragment.findNavController(CreateRecipeFragment.this)
                                        .navigate(R.id.action_createRecipeFragment_to_navigation_home);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to update recipe ID", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to add recipe", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

}

