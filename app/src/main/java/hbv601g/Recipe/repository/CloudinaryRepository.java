package hbv601g.Recipe.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import hbv601g.Recipe.BuildConfig;

/**
 * A repository for handling cloudinary connections for image uploading and retrievals
 */
public class CloudinaryRepository {
    private final Cloudinary cloudinary;
    private final Context context;

    /**
     * Constructs a CloudinaryRepository instance.
     *
     * @param context The application context used for accessing content resolver.
     */
    public CloudinaryRepository(Context context) {
        this.context = context;
        Map config = new HashMap();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);
        this.cloudinary = new Cloudinary(config);
    }

    /**
     * Constructor for uploading image to Cloudinary.
     *
     * @param imageUri The URI of the image to be uploaded.
     * @param recipeId The ID of the recipe to associate with the image.
     * @param callback The callback handling for the image upload.
     */
    public void uploadImageToCloudinary(Uri imageUri, String recipeId, CloudinaryCallback callback) {
        Log.d("CloudinaryRepository", "Cloud Name: " + BuildConfig.CLOUDINARY_CLOUD_NAME);
        Log.d("CloudinaryRepository", "API Key: " + BuildConfig.CLOUDINARY_API_KEY);
        Log.d("CloudinaryRepository", "API Secret: " + BuildConfig.CLOUDINARY_API_SECRET);

        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.asMap(
                        "folder", "recipe_images",
                        "public_id", "recipe_" + recipeId
                ));
                String imageUrl = (String) uploadResult.get("secure_url");
                Log.d("ProfileFragment", "Image uploaded: " + imageUrl);
                callback.onSuccess(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Retrieves the image url for Cloudinary.
     *
     * @param recipeId The Id of the recipe that the image is for.
     * @param callback The callback for the getting the image from Cloudinary.
     */
    public void getImageFromCloudinary(String recipeId, CloudinaryCallback callback) {
        new Thread(() -> {
            try {
                String imageUrl = cloudinary.url().generate("recipe_images/recipe_" + recipeId).replace("http://", "https://");
                //Attempt to load the image and see if it is there.
                HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                Log.e("CloudinaryRepository", "Cloudinary get image URL error", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * The interface for the Cloudinary callback.
     */
    public interface CloudinaryCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }
}
