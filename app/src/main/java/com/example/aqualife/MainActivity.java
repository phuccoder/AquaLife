package com.example.aqualife;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.aqualife.function.AvatarUploader;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 100;
    private static final String FIVEMANAGE_API_TOKEN = "72tWDks9mNkmH40xsIaiklh1CeoOxu18";
    private static final String PREFS_NAME = "AquaLifePrefs";
    private static final String AVATAR_URL_KEY = "avatar_url";
    private static final String TAG = "MainActivity";

    private Button btnSelectImage, btnUpload;
    private ImageView avatarView;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private AvatarUploader avatarUploader;
    private String currentAvatarUrl = ""; // Lưu URL avatar hiện tại
    private SharedPreferences sharedPreferences;

    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();

                        // Hiển thị ảnh đã chọn trực tiếp ở avatarView để preview
                        showImagePreview(selectedImageUri);

                        btnUpload.setEnabled(true);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupImageUploader();
        setupSharedPreferences();
        checkPermissions();

        // Load avatar đã lưu trước đó
        loadSavedAvatar();
    }

    private void initViews() {
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpload = findViewById(R.id.btnUpload);
        avatarView = findViewById(R.id.avatarView);
        progressBar = findViewById(R.id.progressBar);

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnUpload.setOnClickListener(v -> uploadImage());
        btnUpload.setEnabled(false);
    }

    private void setupImageUploader() {
        avatarUploader = new AvatarUploader(FIVEMANAGE_API_TOKEN);
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showImagePreview(Uri imageUri) {
        Log.d(TAG, "Showing image preview for URI: " + imageUri);
        // Hiển thị ảnh đã chọn ở avatarView với dạng hình tròn
        Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        Log.e(TAG, "Failed to load preview image from URI: " + imageUri, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                   com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Preview image loaded successfully from: " + dataSource);
                        return false;
                    }
                })
                .into(avatarView);
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh trước bạn nhé!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        avatarUploader.uploadImage(this, selectedImageUri, new AvatarUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpload.setEnabled(true);

                    Log.d(TAG, "Upload thành công với URL: " + imageUrl);
                    Toast.makeText(MainActivity.this,
                            "Upload thành công!",
                            Toast.LENGTH_SHORT).show();

                    // Lưu URL avatar mới vào SharedPreferences
                    saveAvatarUrl(imageUrl);
                    currentAvatarUrl = imageUrl;

                    // Load ảnh từ URL để hiển thị (thay vì preview từ URI)
                    loadAvatarFromUrl(imageUrl);

                    // Reset selected image URI
                    selectedImageUri = null;
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpload.setEnabled(true);

                    Log.e(TAG, "Upload thất bại: " + error);
                    Toast.makeText(MainActivity.this,
                            "Upload thất bại: " + error,
                            Toast.LENGTH_LONG).show();

                    // Nếu upload thất bại, load lại avatar cũ nếu có
                    if (!currentAvatarUrl.isEmpty()) {
                        loadAvatarFromUrl(currentAvatarUrl);
                    } else {
                        // Hoặc hiển thị placeholder mặc định
                        avatarView.setImageResource(R.drawable.circle_background);
                    }
                });
            }
        });
    }

    private void loadSavedAvatar() {
        // Load avatar URL đã lưu từ SharedPreferences
        currentAvatarUrl = sharedPreferences.getString(AVATAR_URL_KEY, "");

        Log.d(TAG, "Loading saved avatar. Current URL: '" + currentAvatarUrl + "'");

        if (!currentAvatarUrl.isEmpty()) {
            Log.d(TAG, "Found saved avatar URL, loading...");
            loadAvatarFromUrl(currentAvatarUrl);
        } else {
            Log.d(TAG, "No saved avatar found, using default placeholder");
            avatarView.setImageResource(R.drawable.circle_background);
        }

        // Test: Load một ảnh mẫu để kiểm tra Glide hoạt động
        testGlideWithSampleImage();
    }

    private void testGlideWithSampleImage() {
        // Test load một ảnh online để xem Glide có hoạt động không
        String testUrl = "https://via.placeholder.com/150x150/FF0000/FFFFFF?text=TEST";
        Log.d(TAG, "Testing Glide with sample image: " + testUrl);

        // Uncomment dòng dưới để test Glide
        // loadAvatarFromUrl(testUrl);
    }

    private void loadAvatarFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Log.d(TAG, "Image URL is null or empty, using placeholder");
            avatarView.setImageResource(R.drawable.circle_background);
            return;
        }

        Log.d(TAG, "Starting to load avatar from URL: " + imageUrl);

        // Sử dụng Glide để load ảnh từ URL với các tối ưu hóa
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.circle_background)
                .error(R.drawable.circle_background)
                // Thêm cache strategy để load nhanh hơn
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                // Thêm timeout và retry
                .timeout(10000) // 10 seconds timeout
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        Log.e(TAG, "Glide load failed for URL: " + imageUrl, e);
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Không thể tải ảnh avatar", Toast.LENGTH_SHORT).show();
                        });
                        return false; // Return false để Glide vẫn set error drawable
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                   com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Glide successfully loaded avatar from: " + dataSource);
                        return false; // Return false để Glide vẫn set image vào ImageView
                    }
                })
                .into(avatarView);
    }

    private void saveAvatarUrl(String imageUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AVATAR_URL_KEY, imageUrl);
        editor.apply();
        Log.d(TAG, "Avatar URL saved: " + imageUrl);
    }

    private void clearSavedAvatar() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(AVATAR_URL_KEY);
        editor.apply();
        currentAvatarUrl = "";
        avatarView.setImageResource(R.drawable.circle_background);
        Log.d(TAG, "Avatar cleared");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear Glide memory cache khi activity bị destroy
        Glide.get(this).clearMemory();
    }
}