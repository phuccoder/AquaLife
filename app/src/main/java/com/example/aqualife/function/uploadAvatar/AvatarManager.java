package com.example.aqualife.function.uploadAvatar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.aqualife.R;
import com.example.aqualife.function.uploadAvatar.AvatarUploader;
import com.example.aqualife.function.uploadAvatar.AvatarUIController;
import com.example.aqualife.function.uploadAvatar.AvatarImageHandler;

public class AvatarManager {
    private static final String PREFS_NAME = "AquaLifePrefs";
    private static final String AVATAR_URL_KEY = "avatar_url";
    private static final String TAG = "AvatarManager";

    private Activity activity;
    private String apiToken;
    private SharedPreferences sharedPreferences;
    private AvatarUploader avatarUploader;
    private AvatarImageHandler imageHandler;
    private AvatarUIController uiController;

    public AvatarManager(Activity activity, String apiToken) {
        this.activity = activity;
        this.apiToken = apiToken;
        this.sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        this.avatarUploader = new AvatarUploader(apiToken);
        this.imageHandler = new AvatarImageHandler(activity);
        this.uiController = new AvatarUIController(activity, this);
    }

    public void initialize() {
        uiController.initializeViews();
        imageHandler.setupImagePicker(uiController::onImageSelected);
        loadSavedAvatar();
    }

    public void selectImage() {
        imageHandler.selectImage();
    }

    public void uploadImage() {
        Uri selectedImageUri = imageHandler.getSelectedImageUri();

        if (selectedImageUri == null) {
            Toast.makeText(activity, "Vui lòng chọn ảnh trước bạn nhé!", Toast.LENGTH_SHORT).show();
            return;
        }

        uiController.showUploadProgress(true);

        avatarUploader.uploadImage(activity, selectedImageUri, new AvatarUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                activity.runOnUiThread(() -> {
                    uiController.showUploadProgress(false);
                    Log.d(TAG, "Upload thành công với URL: " + imageUrl);
                    Toast.makeText(activity, "Upload thành công!", Toast.LENGTH_SHORT).show();

                    saveAvatarUrl(imageUrl);
                    uiController.loadAvatarFromUrl(imageUrl);
                    imageHandler.resetSelectedImage();
                });
            }

            @Override
            public void onError(String error) {
                activity.runOnUiThread(() -> {
                    uiController.showUploadProgress(false);
                    Log.e(TAG, "Upload thất bại: " + error);
                    Toast.makeText(activity, "Upload thất bại: " + error, Toast.LENGTH_LONG).show();

                    String currentAvatarUrl = getCurrentAvatarUrl();
                    if (!currentAvatarUrl.isEmpty()) {
                        uiController.loadAvatarFromUrl(currentAvatarUrl);
                    } else {
                        uiController.showDefaultAvatar();
                    }
                });
            }
        });
    }

    private void loadSavedAvatar() {
        String savedAvatarUrl = getCurrentAvatarUrl();
        if (!savedAvatarUrl.isEmpty()) {
            Log.d(TAG, "Loading saved avatar: " + savedAvatarUrl);
            uiController.loadAvatarFromUrl(savedAvatarUrl);
        } else {
            Log.d(TAG, "No saved avatar found, using default");
            uiController.showDefaultAvatar();
        }
    }

    private void saveAvatarUrl(String imageUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AVATAR_URL_KEY, imageUrl);
        editor.apply();
        Log.d(TAG, "Avatar URL saved: " + imageUrl);
    }

    public String getCurrentAvatarUrl() {
        return sharedPreferences.getString(AVATAR_URL_KEY, "");
    }

    public void clearSavedAvatar() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(AVATAR_URL_KEY);
        editor.apply();
        uiController.showDefaultAvatar();
        Log.d(TAG, "Avatar cleared");
    }

    public void cleanup() {
        if (imageHandler != null) {
            imageHandler.cleanup();
        }
    }
}