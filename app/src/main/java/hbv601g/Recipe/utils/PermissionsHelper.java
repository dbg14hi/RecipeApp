package hbv601g.Recipe.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import hbv601g.Recipe.fragments.recipe.RecipeDetailFragment;

public class PermissionsHelper {

    private static final int NOTIFICATION_PERMISSION_CODE = 1;
    private static final int CALENDAR_PERMISSION_CODE = 3;

    // Request all necessary permissions (calendar + notifications)
    public static void requestNecessaryPermissions(Fragment fragment) {
        requestNotificationPermission(fragment);
        requestCalendarPermissions(fragment);
    }

    // Request Notification permission (Android 13+)
    public static void requestNotificationPermission(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    // Request Calendar permissions
    public static void requestCalendarPermissions(Fragment fragment) {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.READ_CALENDAR)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (fragment instanceof RecipeDetailFragment) {
                ((RecipeDetailFragment) fragment).requestCalendarPermissions();
            }
        }
    }
}
