package hbv601g.Recipe.fragments.user;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import hbv601g.Recipe.ui.home.FavoritesAdapter;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.repository.FirestoreRepository;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView usernameText, emailText, noFavoritesText;
    private Button loginButton, registerButton, logoutButton, updateUsernameButton;
    private EditText newUsernameField;
    private UserService userService;

    private RecyclerView favoritesRecyclerView;
    private FavoritesAdapter favoritesAdapter;
    private List<Recipe> favoriteRecipes;
    private FirestoreRepository firestoreRepository;

    public ProfileFragment() {
        // Required empty public constructor
    }

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

        // 🔹 Initialize RecyclerView for Favorites
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        favoriteRecipes = new ArrayList<>();
        favoritesAdapter = new FavoritesAdapter(favoriteRecipes, recipe -> {
            removeFromFavorites(recipe);
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
        }
    }

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
