package hbv601g.Recipe.fragments.user;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hbv601g.Recipe.repository.CloudinaryRepository;
import hbv601g.Recipe.ui.home.FavoritesAdapter;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.repository.FirestoreRepository;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import android.content.pm.PackageManager;

import android.os.Environment;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ImageView profileImageView;
    private TextView usernameText, emailText, noFavoritesText;
    private Button loginButton, registerButton, logoutButton, changeProfilePicButton, updateUsernameButton, updateEmailButton, updatePasswordButton;
    private EditText newUsernameField, newEmailField, currentPasswordField, newPasswordField;
    private UserService userService;

    private RecyclerView favoritesRecyclerView;
    private FavoritesAdapter favoritesAdapter;
    private List<Recipe> favoriteRecipes;
    private FirestoreRepository firestoreRepository;
    private CloudinaryRepository cloudinaryRepository;
    private Uri profileImageUri;
    private WeakReference<AlertDialog> dialogRef;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cloudinaryRepository = new CloudinaryRepository(requireContext());

        if (getActivity() != null) {
            userService = new UserService(getActivity());
        }
        firestoreRepository = new FirestoreRepository();

        // 🔹 Profile Picture Elements
        profileImageView = view.findViewById(R.id.profileImageView);
        changeProfilePicButton = view.findViewById(R.id.changeProfilePicButton);


        // 🔹 Initialize UI Elements
        usernameText = view.findViewById(R.id.usernameText);
        emailText = view.findViewById(R.id.emailText);
        noFavoritesText = view.findViewById(R.id.noFavoritesText);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        newUsernameField = view.findViewById(R.id.newUsernameField);
        updateUsernameButton = view.findViewById(R.id.updateUsernameButton);
        newEmailField = view.findViewById(R.id.newEmailField);
        updateEmailButton = view.findViewById(R.id.updateEmailButton);
        currentPasswordField = view.findViewById(R.id.currentPasswordField);
        newPasswordField = view.findViewById(R.id.newPasswordField);
        updatePasswordButton = view.findViewById(R.id.updatePasswordButton);


        // 🔹 Initialize RecyclerView for Favorites
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        favoriteRecipes = new ArrayList<>();
        favoritesAdapter = new FavoritesAdapter(favoriteRecipes, recipe -> {
            removeFromFavorites(recipe);
            return null;
        });

        favoritesRecyclerView.setAdapter(favoritesAdapter);

        // 🔹 Load profile picture
        loadProfilePicture();

        // 🔹 Change Profile Picture
        changeProfilePicButton.setOnClickListener(v -> showImagePickerDialog());

        // 🔹 Update UI when fragment is opened
        auth.addAuthStateListener(firebaseAuth -> {
            updateUI();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                Log.d("Auth", "User ID: " + user.getUid());
                Log.d("Auth", "User Email: " + user.getEmail());
                loadUserFavorites(user.getUid());
            } else {
                Log.d("Auth", "User is NULL");
            }
        });

        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            Log.d("AuthStateListener", "User: " + (user != null ? user.getEmail() : "NULL"));
            updateUI();
        });


        // 🔹 Navigate to Login
        loginButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_loginFragment)
        );

        // 🔹 Navigate to Register
        registerButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_signUpFragment)
        );

        // 🔹 Logout
        logoutButton.setOnClickListener(v -> userService.logoutUser());

        // 🔹 Update Username
        updateUsernameButton.setOnClickListener(v -> {
            String newUsername = newUsernameField.getText().toString().trim();

            if (newUsername.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a new username", Toast.LENGTH_SHORT).show();
                return;
            }

            userService.updateUsername(newUsername);
        });

        // 🔹 Update Email
        updateEmailButton.setOnClickListener(v -> {
            String newEmail = newEmailField.getText().toString().trim();
            String currentPassword = currentPasswordField.getText().toString().trim(); // Add a field for this!

            if (newEmail.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your current password and new email", Toast.LENGTH_SHORT).show();
                return;
            }
            userService.updateEmail(currentPassword, newEmail);
        });

        // 🔹 Update Password
        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordField.getText().toString().trim();
            String currentPassword = currentPasswordField.getText().toString().trim(); // Add a field for this!

            if (newPassword.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your current password and new password", Toast.LENGTH_SHORT).show();
                return;
            }
            userService.updatePassword(currentPassword, newPassword);
        });


        return view;
    }

    private void showImagePickerDialog() {
        if (getActivity() == null || !isAdded()) return;

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

        AlertDialog dialog = builder.create();
        dialogRef = new WeakReference<>(dialog);

        if (getActivity() != null && !getActivity().isFinishing()) {
            dialog.show();
        }
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
        profileImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, profileImageUri);
        takePictureLauncher.launch(takePictureIntent);
    }

    private void pickImageFromGallery() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(pickImageIntent);
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Camera Permission Required")
                .setMessage("This app needs camera permission to take photos for your profile picture. Please grant the permission to use this feature.")
                .setPositiveButton("OK", (dialog, which) -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA))
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

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && profileImageUri != null) {
                    Log.d("ProfileFragment", "Photo taken: " + profileImageUri.toString());
                    uploadImageToCloudinary(profileImageUri);
                } else {
                    Log.d("ProfileFragment", "Photo capture canceled or failed.");
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    Log.d("ProfileFragment", "Gallery image selected: " + imageUri.toString());
                    uploadImageToCloudinary(imageUri);
                } else {
                    Log.d("ProfileFragment", "Image selection canceled or failed.");
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    takePhoto();
                } else {
                    showPermissionDeniedDialog();
                }
            }
    );

    private void uploadImageToCloudinary(Uri imageUri) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        cloudinaryRepository.uploadImageToCloudinary(imageUri, user.getUid(), new CloudinaryRepository.CloudinaryCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d("ProfileFragment", "Image uploaded successfully: " + imageUrl);
                saveImageUrlToFirestore(imageUrl);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("ProfileFragment", "Cloudinary upload failed: " + errorMessage);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .update("profileImage", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Profile Picture Updated!", Toast.LENGTH_SHORT).show();
                        loadProfilePicture();
                    });
                })
                .addOnFailureListener(e -> Log.e("ProfileFragment", "Failed to update profile image", e));
    }

    private void loadProfilePicture() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("profileImage")) {
                String imageUrl = documentSnapshot.getString("profileImage");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(getActivity()).load(imageUrl).into(profileImageView);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dialogRef != null && dialogRef.get() != null) {
            dialogRef.get().dismiss();
        }
    }

    // 🔹 Update UI based on login state
    private void updateUI() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    usernameText.setText("Username: " + documentSnapshot.getString("username"));
                                    emailText.setText("Email: " + user.getEmail());
                                }
                            });

                    loadUserFavorites(userId);
                    loadProfilePicture();

                    profileImageView.setVisibility(View.VISIBLE);
                    changeProfilePicButton.setVisibility(View.VISIBLE);
                    usernameText.setVisibility(View.VISIBLE);
                    emailText.setVisibility(View.VISIBLE);
                    newUsernameField.setVisibility(View.VISIBLE);
                    updateUsernameButton.setVisibility(View.VISIBLE);
                    newEmailField.setVisibility(View.VISIBLE);
                    updateEmailButton.setVisibility(View.VISIBLE);
                    currentPasswordField.setVisibility(View.VISIBLE);
                    newPasswordField.setVisibility(View.VISIBLE);
                    updatePasswordButton.setVisibility(View.VISIBLE);
                    logoutButton.setVisibility(View.VISIBLE);
                    favoritesRecyclerView.setVisibility(View.VISIBLE);

                    loginButton.setVisibility(View.GONE);
                    registerButton.setVisibility(View.GONE);

                } else {
                    Toast.makeText(requireContext(), "Failed to reload user", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            profileImageView.setVisibility(View.GONE);
            changeProfilePicButton.setVisibility(View.GONE);
            usernameText.setVisibility(View.GONE);
            emailText.setVisibility(View.GONE);
            newUsernameField.setVisibility(View.GONE);
            updateUsernameButton.setVisibility(View.GONE);
            newEmailField.setVisibility(View.GONE);
            updateEmailButton.setVisibility(View.GONE);
            currentPasswordField.setVisibility(View.GONE);
            newPasswordField.setVisibility(View.GONE);
            updatePasswordButton.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.GONE);
            noFavoritesText.setVisibility(View.GONE);

            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
        }
    }


    // 🔹 Remove recipe from User favorites
    private void removeFromFavorites(Recipe recipe) {
        String userId = auth.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                if (favorites != null && favorites.contains(recipe.getRecipeId())) {
                    favorites.remove(recipe.getRecipeId());

                    // 🔹 Update Firestore
                    userRef.update("favorites", favorites)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();

                                // 🔹 Remove from local list & update RecyclerView
                                favoritesAdapter.removeRecipe(recipe);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(), "Failed to remove favorite", Toast.LENGTH_SHORT).show());
                }
            }
        }).addOnFailureListener(e ->
                Log.e("Favorites", "Error removing favorite", e));
    }

    // 🔹 Fetch and display favorite recipes
    private void loadUserFavorites(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> favoriteRecipeIds = (List<String>) documentSnapshot.get("favorites"); // ✅ Use "favorites" here

                        if (favoriteRecipeIds == null || favoriteRecipeIds.isEmpty()) {
                            Log.d("Favorites", "No favorites found.");
                            favoritesRecyclerView.setVisibility(View.GONE);
                            return;
                        }

                        Log.d("Favorites", "Favorite Recipe IDs: " + favoriteRecipeIds); // Debugging log

                        firestoreRepository.getRecipesByIds(favoriteRecipeIds, new FirestoreRepository.RecipeCallback() {
                            @Override
                            public void onRecipesLoaded(List<Recipe> recipes) {
                                favoriteRecipes.clear();
                                favoriteRecipes.addAll(recipes);
                                favoritesAdapter.notifyDataSetChanged();

                                if (!favoriteRecipes.isEmpty()) {
                                    favoritesRecyclerView.setVisibility(View.VISIBLE);
                                } else {
                                    favoritesRecyclerView.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Favorites", "Failed to fetch favorite recipes", e);
                                Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("Favorites", "Failed to fetch user data", e));
    }

}

