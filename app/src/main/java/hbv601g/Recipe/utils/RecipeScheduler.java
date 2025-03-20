package hbv601g.Recipe.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import hbv601g.Recipe.entities.Recipe;
import hbv601g.Recipe.ui.notifications.RecipeNotificationWorker;

public class RecipeScheduler {
    private Context context;
    private Recipe recipe;
    private String recipeId;

    public RecipeScheduler(Context context, Recipe recipe, String recipeId) {
        this.context = context;
        this.recipe = recipe;
        this.recipeId = recipeId;
    }

    // Open Date & Time Picker
    public void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            context,
                            (view1, selectedHour, selectedMinute) -> {
                                Calendar selectedDateTime = Calendar.getInstance();
                                selectedDateTime.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);

                                saveScheduledRecipe(selectedDateTime.getTimeInMillis());
                            },
                            hour, minute, true
                    );
                    timePickerDialog.show();
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    // Save Scheduled Recipe to Firestore & Calendar
    private void saveScheduledRecipe(long timestamp) {
        long currentTime = System.currentTimeMillis();

        if (timestamp < currentTime) {
            Toast.makeText(context, "Cannot schedule a recipe in the past!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> scheduledRecipe = new HashMap<>();
        scheduledRecipe.put("recipeId", recipeId);
        scheduledRecipe.put("timestamp", timestamp);

        db.collection("users")
                .document(userId)
                .collection("scheduledRecipes")
                .document(recipeId)
                .set(scheduledRecipe)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Recipe Scheduled!", Toast.LENGTH_SHORT).show();
                    scheduleNotificationWithWorkManager(timestamp);
                    addRecipeToCalendar(recipe.getTitle(), timestamp);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to schedule recipe", Toast.LENGTH_SHORT).show();
                });
    }


    // Schedule Notification with WorkManager
    private void scheduleNotificationWithWorkManager(long timestamp) {
        long delay = timestamp - System.currentTimeMillis();
        if (delay <= 0) {
            Toast.makeText(context, "Scheduled time is in the past!", Toast.LENGTH_SHORT).show();
            return;
        }

        Data data = new Data.Builder()
                .putString("recipeTitle", recipe.getTitle())
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(RecipeNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(context).enqueue(notificationWork);
    }

    // Add Event to Google Calendar
    private void addRecipeToCalendar(String recipeTitle, long timestamp) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e("CalendarDebug", "Permission not granted. Cannot add event.");
            return;
        }

        long endTime = timestamp + (60 * 60 * 1000);  // 1-hour duration

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, timestamp);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.TITLE, "Cook: " + recipeTitle);
        values.put(CalendarContract.Events.DESCRIPTION, "Time to cook " + recipeTitle + "!");
        values.put(CalendarContract.Events.CALENDAR_ID, 2);  // Replace 2 with the correct Calendar ID
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        cr.insert(CalendarContract.Events.CONTENT_URI, values);
        Toast.makeText(context, "Recipe added to Calendar!", Toast.LENGTH_SHORT).show();
    }
}
