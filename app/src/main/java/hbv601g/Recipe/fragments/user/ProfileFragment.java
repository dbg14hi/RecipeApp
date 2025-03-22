package hbv601g.Recipe.fragments.user;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import hbv601g.Recipe.ui.home.FavoritesAdapter;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.repository.FirestoreRepository;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
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
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;


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
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

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
        newEmailText = view.findViewById(R.id.newEmailText);
        updateEmailButton = view.findViewById(R.id.updateEmailButton);
        newPasswordText = view.findViewById(R.id.newPasswordText);
        updatePasswordButton = view.findViewById(R.id.updatePasswordButton);


        //
        updateEmailButton.setOnClickListener(v -> updateEmail());
        updatePasswordButton.setOnClickListener(v -> updatePassword());

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
        changeProfilePicButton.setOnClickListener(v -> openFileChooser());
        profileImageView.setOnClickListener(v -> openFileChooser());

        // 🔹 Update UI when fragment is opened
        auth.addAuthStateListener(firebaseAuth -> {
            updateUI();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                loadUserFavorites(user.getUid());
            }
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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri == null || currentUser == null) return;

        String userId = currentUser.getUid();
        StorageReference fileRef = storageRef.child("profile_pictures/" + userId + ".jpg");

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = fileRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                db.collection("users").document(userId)
                        .update("profileImage", imageUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(), "Profile Picture Updated!", Toast.LENGTH_SHORT).show();
                            loadProfilePicture();
                        });
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProfilePicture() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("profileImage")) {
                String imageUrl = documentSnapshot.getString("profileImage");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(getActivity()).load(imageUrl).into(profileImageView);
                }
            }
        });
    }

    private void updateEmail() {
        FirebaseUser user = auth.getCurrentUser();
        String newEmail = newEmailText.getText().toString().trim();
        String currentPassword = "USER_CURRENT_PASSWORD";

        if (user == null || newEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint("Enter current password");

        new AlertDialog.Builder(requireContext())
                .setTitle("Re-authentication Required")
                .setMessage("Please enter your current password to proceed.")
                .setView(passwordInput)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordInput.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

                    user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {

                            user.updateEmail(newEmail)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("ProfileUpdate", "Email updated to: " + newEmail);

                                            db.collection("users").document(user.getUid())
                                                    .update("email", newEmail)
                                                    .addOnSuccessListener(aVoid ->
                                                            Toast.makeText(requireContext(), "Email updated successfully!", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e ->
                                                            Log.e("Firestore", "Error updating Firestore email", e));

                                        } else {
                                            Toast.makeText(requireContext(), "Failed to update email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e("ProfileUpdate", "Error updating email", task.getException());
                                        }
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Re-authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("AuthError", "Re-authentication failed", authTask.getException());
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updatePassword() {
        FirebaseUser user = auth.getCurrentUser();
        String newPassword = newPasswordText.getText().toString().trim();

        if (user == null || newPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a valid password", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint("Enter current password");

        new AlertDialog.Builder(requireContext())
                .setTitle("Re-authentication Required")
                .setMessage("Please enter your current password to proceed.")
                .setView(passwordInput)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String currentPassword = passwordInput.getText().toString().trim();
                    if (currentPassword.isEmpty()) {
                        Toast.makeText(requireContext(), "Current password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

                    user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("ProfileUpdate", "Password updated successfully");

                                            Toast.makeText(requireContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e("ProfileUpdate", "Error updating password", task.getException());
                                        }
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Re-authentication failed: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("AuthError", "Re-authentication failed", authTask.getException());
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }



    // 🔹 Update UI based on login state
    private void updateUI() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            user.reload().addOnCompleteListener(task -> { // 🔹 Ensure the latest user data is fetched
                if (task.isSuccessful()) {
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    usernameText.setText("Username: " + documentSnapshot.getString("username"));
                                    emailText.setText("Email: " + user.getEmail());
                                }
                            });

                    loadUserFavorites(userId);

                    usernameText.setVisibility(View.VISIBLE);
                    emailText.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.GONE);
                    registerButton.setVisibility(View.GONE);
                    logoutButton.setVisibility(View.VISIBLE);
                    newUsernameField.setVisibility(View.VISIBLE);
                    updateUsernameButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(requireContext(), "Failed to reload user", Toast.LENGTH_SHORT).show();
                }
            });
            // 🔹 Show profile elements
            usernameText.setVisibility(View.VISIBLE);
            emailText.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            newUsernameField.setVisibility(View.VISIBLE);
            updateUsernameButton.setVisibility(View.VISIBLE);
            newEmailText.setVisibility(View.VISIBLE);
            updateEmailButton.setVisibility(View.VISIBLE);
            newPasswordText.setVisibility(View.VISIBLE);
            updatePasswordButton.setVisibility(View.VISIBLE);
        } else {
            // 🔹 Hide favorites when logged out
            favoritesRecyclerView.setVisibility(View.GONE);

            // 🔹 Show login/register elements
            usernameText.setVisibility(View.GONE);
            emailText.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            newUsernameField.setVisibility(View.GONE);
            updateUsernameButton.setVisibility(View.GONE);
            newEmailText.setVisibility(View.GONE);
            updateEmailButton.setVisibility(View.GONE);
            newPasswordText.setVisibility(View.GONE);
            updatePasswordButton.setVisibility(View.GONE);
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

