package hbv601g.Recipe.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class User {
    private long userID;
    private String username;
    private String email;
    private String password;
    private String userPicture;
    private HashSet<Long> userRecipes;
    private HashSet<Long> userFavourites;

    /* Constructor without arguments */
    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        userFavourites = new HashSet<>();
        userRecipes = new HashSet<>();
    }

    /* Getters and setters */
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

    public HashSet<Long> getUserFavourites() {
        if (userFavourites == null) {
            userFavourites = new HashSet<>();
        }
        return userFavourites;
    }

    public void setUserFavourites(HashSet<Long> userFavourites) {
        this.userFavourites = userFavourites;
    }

    public void addToUserFavourites(Long recipeId) {
        if(userFavourites == null) {
            userFavourites = new HashSet<>();
        }
        userFavourites.add(recipeId);
    }

    public void removeFromUserFavourites(Long recipeId) {
        userFavourites.remove(recipeId);
    }



}