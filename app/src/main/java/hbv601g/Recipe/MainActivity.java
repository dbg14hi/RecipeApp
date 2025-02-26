package hbv601g.Recipe;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import hbv601g.Recipe.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get reference to BottomNavigationView
        BottomNavigationView navView = binding.navView;

        // Get reference to NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Connect BottomNavigationView with NavController
        NavigationUI.setupWithNavController(navView, navController);

        //Handle navigation for "Create Recipe" button
        binding.navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_create_recipe) {
                navController.navigate(R.id.createRecipeFragment);
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        MenuItem createRecipeItem = binding.navView.getMenu().findItem(R.id.navigation_create_recipe);
        createRecipeItem.setVisible(auth.getCurrentUser() != null);
    }
}
