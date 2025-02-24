package hbv601g.Recipe.fragments.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class SignUpFragment extends Fragment {

    private UserService userService;
    private EditText usernameInput, emailInput, passwordInput;
    private Button signUpButton, goToLoginButton;

    public SignUpFragment() {
        // Empty public constructor required for Fragments
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        userService = new UserService(requireActivity());

        usernameInput = view.findViewById(R.id.usernameField);
        emailInput = view.findViewById(R.id.emailField);
        passwordInput = view.findViewById(R.id.passwordField);
        signUpButton = view.findViewById(R.id.registerButton);
        goToLoginButton = view.findViewById(R.id.goToLoginButton);

        signUpButton.setOnClickListener(v -> registerUser());
        goToLoginButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_signUpFragment_to_loginFragment)
        );

        return view;
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        userService.registerUser(username, email, password);
        Toast.makeText(requireContext(), "Account created!", Toast.LENGTH_SHORT).show();

        // Navigate to login screen after successful registration
        Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_loginFragment);
    }
}
