package hbv601g.Recipe.entities;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;



public class Review {
    @DocumentId
    private String id; // Use String for Firestore document ID
    private String comment;
    private Integer rating;
    private String userId;
    private String recipeId;
    private String parentReviewId;

    // Default constructor required for Firestore
    public Review() {
    }

    // Constructor with parameters
    public Review(String comment, Integer rating, String userId, String recipeId, String parentReviewId) {
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.recipeId = recipeId;
        this.parentReviewId = parentReviewId;
    }

    // Getters and setters //
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
