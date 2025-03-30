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
 * A fragment for users login
 */
public class LoginFragment extends Fragment {
    private UserService userService;
    private EditText usernameInput, passwordInput;
    private Button loginButton, goToSignUpButton;

    /**
     * A constructor for Firestore
     */
    public LoginFragment() {
    }

    /**
     * A View to create and return the view associated with the fragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view for the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        userService = new UserService(requireActivity());

        usernameInput = view.findViewById(R.id.usernameField);
        passwordInput = view.findViewById(R.id.passwordField);
        loginButton = view.findViewById(R.id.loginButton);
        goToSignUpButton = view.findViewById(R.id.goToSignUpButton);

        loginButton.setOnClickListener(v -> loginUser());
        goToSignUpButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_signUpFragment)
        );

        return view;
    }

    /**
     * Handles users login
     */
    private void loginUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }
        userService.loginUser(username, password);

        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_navigation_profile);
    }

    /**
     * If the view is being destroyed, called to clean up
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
