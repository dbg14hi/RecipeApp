package hbv601g.Recipe.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a user in the Firestore database.
 * A user has an ID, username, email, password, profile picture, a list of their recipes,
 * and a list of their favorite recipes.
 *
 * <p>This class is used for Firestore document mapping.</p>
 */
public class User {
    private long userID;
    private String username;
    private String email;
    private String password;
    private String userPicture;
    private HashSet<Long> userRecipes;
    private ArrayList<Long> userFavourites;

    /**
     *  Default onstructor without arguments for Firestore database.
     *
     */
    public User() {
    }

    /**
     * Creates a new User object with the provided details.
     *
     * @param username The username of the user.
     * @param email The email address of the user.
     * @param password The password of the user.
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        userFavourites = new ArrayList<>();
        userRecipes = new HashSet<>();
    }

    /**
     * Getters and setters
     */
    public long getUserID() {
        return userID;
    }
    public void setUserID(long userID) {
        this.userID = userID;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUserPicture() {
        return userPicture;
    }
    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }
    public HashSet<Long> getUserRecipes() {
        return userRecipes;
    }
    public void setUserRecipes(HashSet<Long> userRecipes) {
        this.userRecipes = userRecipes;
    }
    public List<Long> getUserFavoritesList() {
        if(userFavourites == null) {
            userFavourites = new ArrayList<>();
        }
        return userFavourites;
    }
    public void setUserFavourites (ArrayList<Long> userFavourites) {
        this.userFavourites = userFavourites;
    }
    public void addToUserFavourites(Long recipeId) {
        if(userFavourites == null) {
            userFavourites = new ArrayList<>();
        }
        userFavourites.add(recipeId);
    }
    public void removeFromUserFavourites(Long recipeId) {
        userFavourites.remove(recipeId);
    }
}