package com.example.aqualife.function.uploadAvatar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AvatarImageHandler {
    public interface ImageSelectedCallback {
        void onImageSelected(Uri imageUri);
    }

    private Activity activity;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageSelectedCallback callback;

    public AvatarImageHandler(Activity activity) {
        this.activity = activity;
    }

    public void setupImagePicker(ImageSelectedCallback callback) {
        this.callback = callback;

        imagePickerLauncher = ((AppCompatActivity) activity).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            if (callback != null) {
                                callback.onImageSelected(selectedImageUri);
                            }
                        }
                    }
                }
        );
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    public Uri getSelectedImageUri() {
        return selectedImageUri;
    }

    public void resetSelectedImage() {
        selectedImageUri = null;
    }

    public void cleanup() {
        selectedImageUri = null;
        callback = null;
    }
}