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

/**
 * A fragment for handling the sign up for the user
 */
public class SignUpFragment extends Fragment {
    private UserService userService;
    private EditText usernameInput, emailInput, passwordInput;
    private Button signUpButton, goToLoginButton;

    /**
     * Fragment constructor for Firestore
     */
    public SignUpFragment() {
    }

    /**
     * View for users sign up
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view for the sign up
     */
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

    /**
     * Handles the registration for the users sign up
     */
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

        Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_loginFragment);
    }
}
