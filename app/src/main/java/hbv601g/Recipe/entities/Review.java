package hbv601g.Recipe.entities;

public class Review {
    private long id;
    private String comment;
    private Integer rating;
    private User user;
    private Recipe recipe;
    private Review review;

    /* Constructors */
    public Review() {
    }

    public Review(String comment, Integer rating, User user, Recipe recipe, Review review) {
        this.comment = comment;
        this.rating = rating;
        this.user = user;
        this.recipe = recipe;
        this.review = review;
    }

    /* Getters and setters */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Review getReview(long id) {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}

