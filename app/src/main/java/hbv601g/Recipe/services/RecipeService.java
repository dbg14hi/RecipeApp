package hbv601g.Recipe.services;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Service class that handles interactions with Firebase for recipe-related functionality.
 * It initializes instances of Firebase Authentication and Firestore, and holds a reference
 * to the activity from which it was created.
 */
public class RecipeService {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FragmentActivity activity;

    /**
     * Constructor for new RecipeService instance
     *
     * @param activity The {@link FragmentActivity} that creates this service instance.
     * Used as a context for Firebase operations.
     */
    public RecipeService(FragmentActivity activity) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.activity = activity;
    }
}
