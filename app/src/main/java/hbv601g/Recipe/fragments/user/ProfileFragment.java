package hbv601g.Recipe.fragments.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class ProfileFragment extends Fragment {

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // UI Elements
    private ImageView profileImageView;
    private TextView usernameText, emailText;
    private Button loginButton, registerButton, logoutButton;
    private Button changeProfilePicButton, updateUsernameButton, updateEmailButton, updatePasswordButton;
    private EditText newUsernameField, newEmailField, currentPasswordField, newPasswordField;
    private LinearLayout loggedInContainer;

    // Services
    private UserService userService;

    // Image
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    // Cloudinary Credentials
    private static final String CLOUDINARY_CLOUD_NAME = "dmvi22sp2";
    private static final String CLOUDINARY_API_KEY = "467191768881654";
    private static final String CLOUDINARY_API_SECRET = "J5-uGDut7KJo7EDBEEYlCheEvAI";

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

        if (getActivity() != null) {
            userService = new UserService(getActivity());
        }

        // Profile Picture Elements
        profileImageView = view.findViewById(R.id.profileImageView);
        changeProfilePicButton = view.findViewById(R.id.changeProfilePicButton);

        // Initialize UI Elements
        usernameText = view.findViewById(R.id.usernameText);
        emailText = view.findViewById(R.id.emailText);
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
        loggedInContainer = view.findViewById(R.id.loggedInContainer);

        // Change Profile Picture
        changeProfilePicButton.setOnClickListener(v -> openFileChooser());
        profileImageView.setOnClickListener(v -> openFileChooser());

        // Navigate to Login
        loginButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_loginFragment)
        );

        // Navigate to Register
        registerButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_signUpFragment)
        );

        // Update UI when fragment is opened
        auth.addAuthStateListener(firebaseAuth -> updateUI());

        // Logout
        logoutButton.setOnClickListener(v -> userService.logoutUser());

        // Update Username
        updateUsernameButton.setOnClickListener(v -> {
            String newUsername = newUsernameField.getText().toString().trim();

            if (newUsername.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a new username", Toast.LENGTH_SHORT).show();
                return;
            }
            userService.updateUsername(newUsername);
        });

        // Update Email
        updateEmailButton.setOnClickListener(v -> {
            String newEmail = newEmailField.getText().toString().trim();
            String currentPassword = currentPasswordField.getText().toString().trim();
            if (newEmail.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your current password and new email", Toast.LENGTH_SHORT).show();
                return;
            }
            userService.updateEmail(currentPassword, newEmail);
            updateUI();
        });

        // Update Password
        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordField.getText().toString().trim();
            String currentPassword = currentPasswordField.getText().toString().trim();

            if (newPassword.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your current password and new password", Toast.LENGTH_SHORT).show();
                return;
            }
            userService.updatePassword(currentPassword, newPassword);
        });

        return view;
    }

    // Opens the file chooster to pick a new profile picture
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
            uploadImageToCloudinary();
        } else {
            Log.d("ProfileFragment", "Image selection failed or canceled");
        }
    }

    private void uploadImageToCloudinary() {
        if (imageUri == null) {
            Log.e("ProfileFragment", "uploadImageToCloudinary: No image URI found");
            return;
        }

        Log.d("ProfileFragment", "Uploading image to Cloudinary...");

        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            Map config = new HashMap();
            config.put("cloud_name", CLOUDINARY_CLOUD_NAME);
            config.put("api_key", CLOUDINARY_API_KEY);
            config.put("api_secret", CLOUDINARY_API_SECRET);

            Cloudinary cloudinary = new Cloudinary(config);

            new Thread(() -> {
                try {
                    Map uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap("folder", "profile_pictures"));
                    String imageUrl = (String) uploadResult.get("secure_url");

                    Log.d("ProfileFragment", "Image uploaded: " + imageUrl);  // ✅ Debugging
                    saveImageUrlToFirestore(imageUrl);
                } catch (Exception e) {
                    Log.e("ProfileFragment", "Cloudinary upload failed", e);
                }
            }).start();

        } catch (IOException e) {
            Log.e("ProfileFragment", "Error reading image file", e);
        }
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
                });
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

    // Update UI based on login state
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
                    loadProfilePicture();

                    loggedInContainer.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.GONE);
                    registerButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(requireContext(), "Failed to reload user", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Not logged in — hide all user-specific views
            loggedInContainer.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
        }
    }
}