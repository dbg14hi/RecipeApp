package hbv601g.Recipe.services;

import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthCredential;
import java.util.HashMap;
import java.util.Map;
import hbv601g.Recipe.R;

/**
 * A service class for user registration like logging on and out and updating user information.
 *
 */
public class UserService {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FragmentActivity activity;

    /**
     * A constructor for UserService instance.
     *
     * @param activity The activity for Firestore database.
     */
    public UserService(FragmentActivity activity) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.activity = activity;
    }

    /**
     * This is for registering a new user to the app with email and password.
     *
     * @param username The username of the user.
     * @param email The email of the user.
     * @param password The password for the account.
     */
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

    /**
     * Handles the login for the user.
     *
     * @param username The username for the user.
     * @param password The password.
     */
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

    /**
     * Handles the updates for the users information.
     *
     * @param newUsername The new username for the user.
     */
    public void updateUsername(String newUsername) {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            db.collection("users")
                    .whereEqualTo("username", newUsername)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(activity, "Username already taken. Choose another.", Toast.LENGTH_SHORT).show();
                        } else {
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

    /**
     * Handles the update for the users email.
     *
     * @param currentPassword The current password.
     * @param newEmail The new email.
     */
    public void updateEmail(String currentPassword, String newEmail) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    user.updateEmail(newEmail)
                            .addOnCompleteListener(emailUpdateTask -> {
                                if (emailUpdateTask.isSuccessful()) {
                                    db.collection("users").document(user.getUid())
                                            .update("email", newEmail)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(activity, "Email updated successfully!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(activity, "Failed to update email in Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                } else {
                                    Toast.makeText(activity, "Email update failed: " + emailUpdateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(activity, "Reauthentication failed: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(activity, "No logged-in user found!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the update for the users password.
     *
     * @param currentPassword The users current password.
     * @param newPassword The users new password.
     */
    public void updatePassword(String currentPassword, String newPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(passwordUpdateTask -> {
                                if (passwordUpdateTask.isSuccessful()) {
                                    Toast.makeText(activity, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, "Password update failed: " + passwordUpdateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(activity, "Reauthentication failed: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(activity, "No logged-in user found!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the logout for the user and reloads the profile fragment.
     *
     */
    public void logoutUser() {
        auth.signOut();

        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
        navController.navigate(R.id.action_navigation_profile_self);

        Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}
