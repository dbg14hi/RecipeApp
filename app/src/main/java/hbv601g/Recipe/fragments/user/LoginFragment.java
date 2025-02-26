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

public class LoginFragment extends Fragment {

    private UserService userService;
    private EditText usernameInput, passwordInput;
    private Button loginButton, goToSignUpButton;

    public LoginFragment() {
        // Empty public constructor required for Fragments
    }

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

    private void loginUser() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        userService.loginUser(username, password);

        // Navigate to ProfileFragment
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_navigation_profile);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
