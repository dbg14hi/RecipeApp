package hbv601g.Recipe.fragments.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hbv601g.Recipe.R;
import hbv601g.Recipe.entities.Review;

/**
 * An adapter class for displaying a list of reviews in a Recyclerview
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;

    /**
     * Constructor for ReviewAdater
     *
     * @param reviews List of reviews to be displayed
     */
    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    /**
     * Inflates the layout for individual review items
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return new ReviewHolder for reviews
     */
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    /**
     * Binds the review data to the ViewHolder
     *
      * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.reviewComment.setText(review.getComment());
        holder.reviewRating.setRating(review.getRating());
    }

    /**
     * Returns total number of reviews in the list
     *
     * @return The number of review items
     */
    @Override
    public int getItemCount() {
        return reviews.size();
    }

    /**
     * ViewHolder class for holding review item views
     */
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView reviewComment;
        RatingBar reviewRating;

        /**
         * Constructor for ReviewHolder
         *
         * @param itemView The view of the review item
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewComment = itemView.findViewById(R.id.reviewComment);
            reviewRating = itemView.findViewById(R.id.reviewRating);
        }
    }
}