package hbv601g.Recipe.repository;

import android.content.Context;
import android.net.Uri;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import hbv601g.Recipe.BuildConfig;

public class CloudinaryRepository {

    private final Cloudinary cloudinary;
    private final Context context;

    public CloudinaryRepository(Context context) {
        this.context = context;
        Map<String, String> config = ObjectUtils.asMap(
                "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key", BuildConfig.CLOUDINARY_API_KEY,
                "api_secret", BuildConfig.CLOUDINARY_API_SECRET
        );
        this.cloudinary = new Cloudinary(config);
    }

    public void uploadRecipeImage(Uri imageUri, String recipeId, CloudinaryCallback callback) {
        new Thread(() -> { // Perform upload in background thread
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.asMap(
                        "folder", "recipe_images",
                        "public_id", "recipe_" + recipeId
                ));
                String imageUrl = (String) uploadResult.get("secure_url");
                callback.onSuccess(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void getRecipeImage(String recipeId, CloudinaryCallback callback) {
        new Thread(() -> { // Perform fetch in background thread
            try {
                ApiResponse result = cloudinary.search()
                        .expression("folder:recipe_images AND public_id:recipe_" + recipeId)
                        .execute();

                List<Map> resources = (List<Map>) result.get("resources");

                if (resources != null && !resources.isEmpty()) {
                    String imageUrl = (String) resources.get(0).get("secure_url");
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onError("Image not found");
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public interface CloudinaryCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }
}
