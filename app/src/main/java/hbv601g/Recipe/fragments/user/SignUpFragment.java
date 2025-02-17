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

import java.net.MalformedURLException;

import hbv601g.Recipe.R;
import hbv601g.Recipe.services.UserService;

public class SignUpFragment extends Fragment {

    private UserService userService = new UserService();

    private EditText usernameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private Button signUpButton;

    public SignUpFragment() throws MalformedURLException {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        return view;
    }

    private void registerUser() {
            return;
        }

}
