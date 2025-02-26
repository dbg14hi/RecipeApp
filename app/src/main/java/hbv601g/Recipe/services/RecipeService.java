package hbv601g.Recipe.services;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecipeService {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FragmentActivity activity; // Replace Context with FragmentActivity for Navigation

    public RecipeService(FragmentActivity activity) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.activity = activity;
    }
}
