package hbv601g.Recipe.entities;

public class NotificationModel {
    private String recipeTitle;
    private long timestamp;

    // Empty constructor for Firestore deserialization
    public NotificationModel() {}

    public NotificationModel(String recipeTitle, long timestamp) {
        this.recipeTitle = recipeTitle;
        this.timestamp = timestamp;
    }

    public String getRecipeTitle() { return recipeTitle; }
    public long getTimestamp() { return timestamp; }
}
