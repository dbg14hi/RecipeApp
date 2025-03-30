package hbv601g.Recipe.fragments.recipe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import hbv601g.Recipe.R;
import hbv601g.Recipe.databinding.FragmentRecipeDetailBinding;
import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.entities.Review;
import hbv601g.Recipe.fragments.review.NewReviewFragment;
import hbv601g.Recipe.fragments.review.ReviewAdapter;
import hbv601g.Recipe.repository.CloudinaryRepository;
import hbv601g.Recipe.repository.FirestoreRepository;
import hbv601g.Recipe.ui.notifications.RecipeNotificationWorker;
import hbv601g.Recipe.utils.PermissionsHelper;
import hbv601g.Recipe.utils.RecipeScheduler;

/**
 * Fragment that displays detailed information about a recipe,
 * allows users to mark recipes as favorites, add reviews, and schedule cooking.
 */
public class RecipeDetailFragment extends Fragment {

    private TextView titleTextView, descriptionTextView, ingredientsTextView, cookingTimeTextView, dietaryRestrictionsTextView, mealCategoriesTextView;
    private ImageButton favoriteButton;

    private ImageView recipeImageView;
    private CloudinaryRepository cloudinaryRepository;

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    private FirestoreRepository repository;
    private String userId, recipeId;
    private boolean isFavorite = false;
    private Recipe recipe;
    private FragmentRecipeDetailBinding _binding;

    /**
     * Inflates the layout and initializes UI elements for the recipe details.
     *
     * @param inflater           LayoutInflater to inflate views.
     * @param container          Parent view container.
     * @param savedInstanceState Previously saved instance state.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);  // Initialize View Binding
        View view = _binding.getRoot();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        repository = new FirestoreRepository();
        cloudinaryRepository = new CloudinaryRepository(requireContext());

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        PermissionsHelper.requestNecessaryPermissions(this);
        setupScheduleButton();

        titleTextView = view.findViewById(R.id.recipe_title);
        descriptionTextView = view.findViewById(R.id.recipe_description);
        ingredientsTextView = view.findViewById(R.id.recipe_ingredients);
        cookingTimeTextView = view.findViewById(R.id.recipe_cooking_time);
        dietaryRestrictionsTextView = view.findViewById(R.id.recipe_dietary_restrictions);
        mealCategoriesTextView = view.findViewById(R.id.recipe_meal_categories);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        recipeImageView = view.findViewById(R.id.recipeImage);
        Button reviewButton;
        reviewButton = view.findViewById(R.id.reviewButton);
        RecyclerView reviewRecyclerView; //Arna
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewList = new ArrayList<>(); //Arna
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewRecyclerView.setAdapter(reviewAdapter);

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("recipeTitle");
            String description = args.getString("recipeDescription");
            ArrayList<String> ingredients = args.getStringArrayList("recipeIngredients");
            int cookingTime = args.getInt("recipeCookingTime");
            recipeId = args.getString("recipeId");
            if (recipeId == null || recipeId.isEmpty()) {
                Toast.makeText(getContext(), "Error: Recipe ID is missing", Toast.LENGTH_SHORT).show();
                return view;
            }

            recipe = new Recipe(title, ingredients, description, cookingTime, null, null, null, userId);

            titleTextView.setText(title);
            descriptionTextView.setText(description);
            if (ingredients != null && !ingredients.isEmpty()) {
                Log.d("RecipeDetail", "Ingredients: " + ingredients);
                ingredientsTextView.setText(TextUtils.join(", ", ingredients));
            } else {
                ingredientsTextView.setText("No ingredients listed");
            }

            cookingTimeTextView.setText("Cooking Time: " + cookingTime + " minutes");

            checkIfFavorite();

            favoriteButton.setOnClickListener(v -> toggleFavorite(userId, recipeId));
            reviewButton.setOnClickListener(v -> openReviewFragment());

            displayRecipeImage();
            fetchReviews();
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        NavHostFragment.findNavController(RecipeDetailFragment.this).navigateUp();
                    }
                }
        );

        return view;
    }

    private void displayRecipeImage() {
        cloudinaryRepository.getImageFromCloudinary(recipeId, new CloudinaryRepository.CloudinaryCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d("RecipeDetailFragment", "Cloudinary Image URL: " + imageUrl);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .into(recipeImageView);
                    });

                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        recipeImageView.setImageResource(R.drawable.ic_launcher_background);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("RecipeDetailFragment", "Failed to load image from Cloudinary: " + errorMessage);
                Toast.makeText(getContext(), "Failed to load recipe image", Toast.LENGTH_SHORT).show();
                recipeImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        });
    }

    /**
     * Checks if the recipe is marked as users favorite recipe
     */
    private void checkIfFavorite() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> favorites = (List<String>) documentSnapshot.get("favorites");
                isFavorite = favorites != null && favorites.contains(recipeId);
            } else {
                isFavorite = false;
            }
            updateFavoriteButtonUI(isFavorite);  // Update UI after checking
        }).addOnFailureListener(e -> Log.e("Favorites", "Failed to check favorite", e));
    }

    /**
     * Toogles favorite recipes for users
     * @param userId
     * @param recipeId
     */
    private void toggleFavorite(String userId, String recipeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> favorites = (List<String>) documentSnapshot.get("favorites");

                if (favorites == null) {
                    favorites = new ArrayList<>();
                }

                if (favorites.contains(recipeId)) {
                    // Remove from favorites
                    favorites.remove(recipeId);
                    isFavorite = false;
                } else {
                    // Add to favorites
                    favorites.add(recipeId);
                    isFavorite = true;
                }

                userRef.update("favorites", favorites)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Favorites", "Updated successfully");
                            updateFavoriteButtonUI(isFavorite);
                        })
                        .addOnFailureListener(e -> Log.e("Favorites", "Error updating", e));

            } else {
                Map<String, Object> userData = new HashMap<>();
                userData.put("favorites", Arrays.asList(recipeId));

                userRef.set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Favorites", "User created with favorite");
                            isFavorite = true;
                            updateFavoriteButtonUI(isFavorite);
                        })
                        .addOnFailureListener(e -> Log.e("Favorites", "Error creating user", e));
            }
        });
    }

    /**
     * Updates the User interface to show that the recipe is users favorite
     * @param isFavorited
     */
    private void updateFavoriteButtonUI(boolean isFavorited) {
        if (!isAdded() || getView() == null) return;

        Context context = getContext();
        if (context == null || favoriteButton == null) return;

        int color = isFavorited ? R.color.favorite_active : R.color.favorite_inactive;
        favoriteButton.setColorFilter(ContextCompat.getColor(context, color));

        updateFavoriteIcon(isFavorited);
    }

    /**
     * Set a red light in the icon for showing it is the users favorite recipe in UI
     * @param isFavorite
     */
    private void updateFavoriteIcon(boolean isFavorite) {
        favoriteButton.setImageResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_empty);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Opens the review fragment for adding a review
     */
    private void openReviewFragment() {
        NewReviewFragment newReviewFragment = new NewReviewFragment();
        Bundle args = new Bundle();
        args.putString("recipe_id", recipeId);
        newReviewFragment.setArguments(args);

        NavHostFragment.findNavController(this).navigate(R.id.newReviewFragment, args);
    }

    /**
     * Fetch reviews for recipes
     */
    private void fetchReviews() {
        if (recipeId != null && !recipeId.isEmpty()) {
            repository.getReviewsByRecipe(recipeId, new FirestoreRepository.ReviewCallback() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onReviewsLoaded(List<Review> reviews) {
                    reviewList.clear();
                    reviewList.addAll(reviews);
                    reviewAdapter.notifyDataSetChanged();

                    Log.d("RecipeDetailFragment", "Reviews loaded: " + reviews.size());
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Setup for scheduling for the shcedule recipe button
     */
    private void setupScheduleButton() {
        _binding.scheduleRecipeButton.setOnClickListener(v -> {
            RecipeScheduler scheduler = new RecipeScheduler(requireContext(), recipe, recipeId);
            scheduler.openDateTimePicker();
        });
    }

    /**
     * Launches permission request for reading and writing calendar events.
     */
    public void requestCalendarPermissions() {
        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
        });
    }

    /**
     * Handles multiple permission requests for reading and writing calendar events.
     */
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean calendarReadGranted = result.getOrDefault(Manifest.permission.READ_CALENDAR, false);
                Boolean calendarWriteGranted = result.getOrDefault(Manifest.permission.WRITE_CALENDAR, false);

                if (calendarReadGranted && calendarWriteGranted) {
                    Log.d("CalendarDebug", "Calendar permissions granted!");
                } else {
                    Log.e("CalendarDebug", "Calendar permissions denied!");
                    Toast.makeText(getContext(), "Calendar permission needed to list calendars!", Toast.LENGTH_SHORT).show();
                }
            });
}



