package hbv601g.Recipe.ui.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import hbv601g.Recipe.R;

public class RecipeNotificationWorker extends Worker {

    public RecipeNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String recipeTitle = getInputData().getString("recipeTitle");

        if (recipeTitle != null) {
            showNotification(recipeTitle);
            return Result.success();
        } else {
            return Result.failure();
        }
    }

    // Displays a push notification for the scheduled recipe
    private void showNotification(String recipeTitle) {
        Context context = getApplicationContext();
        String channelId = "recipe_notifications";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Recipe Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Reminders for scheduled recipes");
            notificationManager.createNotificationChannel(channel);
        }

        // Opens NotificationsFragment when the user taps the notification
        Intent intent = new Intent(context, NotificationsFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time to Cook!")
                .setContentText("Your scheduled recipe: " + recipeTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());

        saveNotificationToFirestore(recipeTitle);
    }

    // Saves the notification data in Firestore under the user's notifications
    private void saveNotificationToFirestore(String recipeTitle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("recipeTitle", recipeTitle);
        notificationData.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notificationData);
    }
}
