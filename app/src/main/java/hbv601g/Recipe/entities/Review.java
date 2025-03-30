package hbv601g.Recipe.entities;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
/**
 * Represents a review for a recipe in the Firestore database.
 * A review contains a comment, rating, user ID, recipe ID, and an optional parent review ID.
 *
 * <p>This class is used for Firestore document mapping.
 */
public class Review {
    @DocumentId
    private String id; // Use String for Firestore document ID
    private String comment;
    private Integer rating;
    private String userId;
    private String recipeId;
    private String parentReviewId;

    /**
     * Default constructor required for Firestore database.
     */
    public Review() {
    }

    /**
     * Creates a new Review object.
     *
     * @param comment The text content of the review.
     * @param rating The rating given in the review (e.g., 1-5 stars).
     * @param userId The ID of the user who submitted the review.
     * @param recipeId The ID of the recipe that this review belongs to.
     * @param parentReviewId The ID of the parent review (for nested replies), can be null.
     */
    public Review(String comment, Integer rating, String userId, String recipeId, String parentReviewId) {
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.recipeId = recipeId;
        this.parentReviewId = parentReviewId;
    }

    /**
     * Getters and setters for review
     */
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getRecipeId() {
        return recipeId;
    }
    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
    public String getParentReviewId() {
        return parentReviewId;
    }
    public void setParentReviewId(String parentReviewId) {
        this.parentReviewId = parentReviewId;
    }
}
