package hbv601g.Recipe.fragments.user;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import hbv601g.Recipe.ui.home.FavoritesAdapter;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.repository.FirestoreRepository;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView usernameText, emailText, noFavoritesText, passwordText;
    private Button loginButton, registerButton, logoutButton, updateUsernameButton, updateEmailButton, updatePasswordButton;
    private EditText newUsernameField, newEmailText, newPasswordText;
    private UserService userService;

    private LoginFragment loginFragment; //skoða

    private RecyclerView favoritesRecyclerView;
    private FavoritesAdapter favoritesAdapter;
    private List<Recipe> favoriteRecipes;
    private FirestoreRepository firestoreRepository;

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
        userService = new UserService(requireActivity());
        firestoreRepository = new FirestoreRepository();

        // 🔹 Initialize UI Elements
        usernameText = view.findViewById(R.id.usernameText);
        emailText = view.findViewById(R.id.emailText);
        noFavoritesText = view.findViewById(R.id.noFavoritesText);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        newUsernameField = view.findViewById(R.id.newUsernameField);
        updateUsernameButton = view.findViewById(R.id.updateUsernameButton);
        newEmailText = view.findViewById(R.id.newEmailText);
        updateEmailButton = view.findViewById(R.id.updateEmailButton);
        passwordText = view.findViewById(R.id.newPasswordText);
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
            Toast.makeText(requireContext(), "Clicked on: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
            return null;
        });
        favoritesRecyclerView.setAdapter(favoritesAdapter);

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

        return view;
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

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            usernameText.setText("Username: " + documentSnapshot.getString("username"));
                            emailText.setText("Email: " + documentSnapshot.getString("email"));
                        }
                    });

            // 🔹 Fetch user's favorite recipes
            loadUserFavorites(userId);

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

    // 🔹 Fetch and display favorite recipes
    // 🔹 Fetch and display favorite recipes
    private void loadUserFavorites(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> favoriteRecipeIds = (List<String>) documentSnapshot.get("userFavourites");

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
