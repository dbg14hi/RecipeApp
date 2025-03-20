package hbv601g.Recipe.services;

import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import hbv601g.Recipe.R;

public class UserService {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FragmentActivity activity; // Replace Context with FragmentActivity for Navigation

    public UserService(FragmentActivity activity) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.activity = activity;
    }

    // 🔹 Register New User
    public void registerUser(String username, String email, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(activity, "Username already taken. Choose another.", Toast.LENGTH_SHORT).show();
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null) {
                                            String userId = user.getUid();

                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("userID", userId);
                                            userData.put("username", username);
                                            userData.put("email", email);

                                            db.collection("users").document(userId).set(userData)
                                                    .addOnSuccessListener(aVoid -> Toast.makeText(activity, "Registration successful!", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e -> Toast.makeText(activity, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                        }
                                    } else {
                                        Toast.makeText(activity, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // 🔹 User Login
    public void loginUser(String username, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String email = querySnapshot.getDocuments().get(0).getString("email");

                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(activity, "Login successful!", Toast.LENGTH_SHORT).show();

                                        // ✅ Navigate to ProfileFragment only if not already there
                                        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
                                        if (navController.getCurrentDestination().getId() != R.id.navigation_profile) {
                                            navController.navigate(R.id.action_loginFragment_to_navigation_profile);
                                        }
                                    } else {
                                        Toast.makeText(activity, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(activity, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // 🔹 Update Username
    public void updateUsername(String newUsername) {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // 🔹 First, check if the username already exists
            db.collection("users")
                    .whereEqualTo("username", newUsername)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(activity, "Username already taken. Choose another.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 🔹 If username is unique, update it in Firestore
                            db.collection("users").document(userId)
                                    .update("username", newUsername)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(activity, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                                        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
                                        navController.navigate(R.id.navigation_profile); // Reload ProfileFragment
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(activity, "Failed to update username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity, "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(activity, "No logged-in user found!", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 Logout User → Reload ProfileFragment
    public void logoutUser() {
        auth.signOut();

        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
        navController.navigate(R.id.action_navigation_profile_self); // Refresh Profile Tab

        Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
