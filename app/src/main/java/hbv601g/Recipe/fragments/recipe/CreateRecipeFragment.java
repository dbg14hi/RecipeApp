package hbv601g.Recipe.fragments.recipe;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CheckBox;
import android.Manifest;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.provider.MediaStore;
import android.app.AlertDialog;
import android.content.DialogInterface;

import hbv601g.Recipe.R;


public class CreateRecipeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText titleInput, descriptionInput, ingredientsInput, cookingTimeInput;
    private LinearLayout dietaryRestrictionsContainer, mealCategoriesContainer;

    // Recipe Image
    private ImageView recipeImageView;
    private Button recipeImageButton;
    private Uri recipeImageUri;

    // Camera permission launcher
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    takePhoto();
                } else {
                    showPermissionDeniedDialog();
                }
            });

    // Image selection launcher
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    recipeImageView.setImageURI(imageUri);
                    recipeImageUri = imageUri;
                }
            });

    // Camera intent launcher
    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    recipeImageView.setImageURI(recipeImageUri);
                }
            });


    // Submit button
    private Button submitRecipeButton;

    private final List<String> allDietaryRestrictions = Arrays.asList("Nut-free", "Vegan", "Vegetarian", "Gluten-free", "Dairy-free"); // Example data
    private final List<String> allMealCategories = Arrays.asList("Breakfast", "Lunch", "Dinner", "Snacks");

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
        dietaryRestrictionsContainer = view.findViewById(R.id.dietaryRestrictionsContainer);
        mealCategoriesContainer = view.findViewById(R.id.mealCategoriesContainer);

        recipeImageView = view.findViewById(R.id.recipeImageView);
        recipeImageButton = view.findViewById(R.id.recipeImageButton);

        submitRecipeButton = view.findViewById(R.id.submitRecipeButton);

        // Setup checkboxes for filters
        for (String restriction : allDietaryRestrictions) {
            CheckBox checkBox = new CheckBox(this.getContext());
            checkBox.setText(restriction);
            dietaryRestrictionsContainer.addView(checkBox);
        }

        for (String category : allMealCategories) {
            CheckBox checkBox = new CheckBox(this.getContext());
            checkBox.setText(category);
            mealCategoriesContainer.addView(checkBox);
        }

        // Setup listener for image
        recipeImageView.setOnClickListener(v -> showImagePickerDialog());
        recipeImageButton.setOnClickListener(v -> showImagePickerDialog());

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

        List<String> selectedDietaryRestrictions = new ArrayList<>();
        for (int i = 0; i < dietaryRestrictionsContainer.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) dietaryRestrictionsContainer.getChildAt(i);
            if (checkBox.isChecked()) {
                selectedDietaryRestrictions.add(checkBox.getText().toString());
            }
        }

        List<String> selectedMealCategories = new ArrayList<>();
        for (int i = 0; i < mealCategoriesContainer.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) mealCategoriesContainer.getChildAt(i);
            if (checkBox.isChecked()) {
                selectedMealCategories.add(checkBox.getText().toString());
            }
        }

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
        recipe.put("dietaryRestrictions", selectedDietaryRestrictions);
        recipe.put("mealCategories", selectedMealCategories);
        recipe.put("timestamp", FieldValue.serverTimestamp());
        recipe.put("userId", userId);

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

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                requestCameraPermission();
            } else if (which == 1) {
                pickImageFromGallery();
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showPermissionRationaleDialog();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a temporary Uri for the image
        recipeImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new android.content.ContentValues());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, recipeImageUri);
        takePictureLauncher.launch(takePictureIntent);
    }

    private void pickImageFromGallery() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Camera Permission Required")
                .setMessage("This app needs camera permission to take photos for your recipes. Please grant the permission to use this feature.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Camera Permission Denied")
                .setMessage("Camera permission is required to take photos. You can enable it in the app's settings.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
}

