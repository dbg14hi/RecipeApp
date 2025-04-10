package hbv601g.Recipe.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.NotificationModel;

/**
 * RecyclerView Adapter for displaying a list of recipe notifications.
 * Each item displays the recipe title and the time it was added.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> notifications;

    /**
     * A constructor for Notification Adapter for displaying a list of recipe notifications.
     *
     * @param notifications List of objects to display.
     */
    public NotificationAdapter(List<NotificationModel> notifications) {
        this.notifications = notifications;
    }

    /**
     * The view holder for the individual notification item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new viewholder that holds the view.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder for the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.recipeTitleTextView.setText(notification.getRecipeTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        holder.timestampTextView.setText(sdf.format(new Date(notification.getTimestamp())));
    }

    /**
     * Gets the number of items in the dataset.
     *
     * @return the size of the notification list.
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * ViewHolder class for notification items.
     *
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recipeTitleTextView, timestampTextView;

        /**
         * ViewHolder for new ViewHolder and finds its subviews.
         *
         * @param itemView The view of the list item.
         */
        ViewHolder(View itemView) {
            super(itemView);
            recipeTitleTextView = itemView.findViewById(R.id.notification_recipe_title);
            timestampTextView = itemView.findViewById(R.id.notification_timestamp);
        }
    }
}
