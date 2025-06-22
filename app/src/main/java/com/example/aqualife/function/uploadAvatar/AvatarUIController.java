package com.example.aqualife.function.uploadAvatar;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.aqualife.R;
import com.example.aqualife.function.uploadAvatar.AvatarManager;

public class AvatarUIController {
    private static final String TAG = "AvatarUIController";

    private Activity activity;
    private AvatarManager avatarManager;

    private Button btnSelectImage;
    private Button btnUpload;
    private ImageView avatarView;
    private ProgressBar progressBar;

    public AvatarUIController(Activity activity, AvatarManager avatarManager) {
        this.activity = activity;
        this.avatarManager = avatarManager;
    }

    public void initializeViews() {
        btnSelectImage = activity.findViewById(R.id.btnSelectImage);
        btnUpload = activity.findViewById(R.id.btnUpload);
        avatarView = activity.findViewById(R.id.avatarView);
        progressBar = activity.findViewById(R.id.progressBar);

        btnSelectImage.setOnClickListener(v -> avatarManager.selectImage());
        btnUpload.setOnClickListener(v -> avatarManager.uploadImage());
        btnUpload.setEnabled(false);
    }

    public void onImageSelected(Uri imageUri) {
        showImagePreview(imageUri);
        btnUpload.setEnabled(true);
    }

    public void showImagePreview(Uri imageUri) {
        Glide.with(activity)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .into(avatarView);
    }

    public void loadAvatarFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            showDefaultAvatar();
            return;
        }

        Glide.with(activity)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .timeout(10000) // 10 seconds timeout
                .into(avatarView);

        Log.d(TAG, "Loading avatar from URL: " + imageUrl);
    }

    public void showDefaultAvatar() {
        avatarView.setImageResource(R.drawable.circle_background);
    }

    public void showUploadProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
        }
    }
}