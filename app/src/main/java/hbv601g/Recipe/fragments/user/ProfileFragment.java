package hbv601g.Recipe.fragments.user;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView usernameText, emailText;
    private Button loginButton, registerButton, logoutButton, updateUsernameButton;
    private EditText newUsernameField;
    private UserService userService;

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

        // 🔹 Initialize UI Elements
        usernameText = view.findViewById(R.id.usernameText);
        emailText = view.findViewById(R.id.emailText);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        newUsernameField = view.findViewById(R.id.newUsernameField);
        updateUsernameButton = view.findViewById(R.id.updateUsernameButton);

        // 🔹 Update UI when fragment is opened
        auth.addAuthStateListener(firebaseAuth -> updateUI());

        // 🔹 Navigate to Login
        loginButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_loginFragment)
        );

        // 🔹 Navigate to Register
        registerButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_signUpFragment)
        );

        // 🔹 Logout
        logoutButton.setOnClickListener(v -> {
            userService.logoutUser();
        });

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

            // 🔹 Show profile elements
            usernameText.setVisibility(View.VISIBLE);
            emailText.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            newUsernameField.setVisibility(View.VISIBLE);
            updateUsernameButton.setVisibility(View.VISIBLE);
        } else {
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
}
