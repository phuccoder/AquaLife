package com.example.aqualife.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class FirebaseStorageHelper {
    private static final String TAG = "FirebaseStorageHelper";
    private static final String MEDICINE_IMAGES_PATH = "medicine_images/";
    private static final String AQUARIUM_IMAGES_PATH = "aquarium_images/";
    private static final String FISH_IMAGES_PATH = "fish_images/";
    private static final String FOOD_IMAGES_PATH = "food_images/";
    private static final String AVATAR_IMAGES_PATH = "avatar_images/";

    // Replace with your actual Firebase Storage bucket URL
    private static final String STORAGE_BUCKET_URL = "gs://aqualife-ba90a.firebasestorage.app";

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Context context;

    public FirebaseStorageHelper(Context context) {
        this.context = context;
        initFirebaseStorage();
    }

    // Keep the default constructor for backward compatibility
    public FirebaseStorageHelper() {
        initFirebaseStorage();
    }

    private void initFirebaseStorage() {
        try {
            // Ensure Firebase is initialized
            if (FirebaseApp.getApps(context != null ? context : null).isEmpty()) {
                Log.e(TAG, "Firebase is not initialized. Make sure FirebaseApp.initializeApp() is called first");
                throw new IllegalStateException("Firebase is not initialized");
            }

            // Get Firebase Storage instance with explicit bucket URL
            storage = FirebaseStorage.getInstance(STORAGE_BUCKET_URL);
            storageRef = storage.getReference();

            Log.d(TAG, "Firebase Storage initialized successfully with bucket: " + STORAGE_BUCKET_URL);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Storage", e);
            throw new IllegalStateException("Error initializing Firebase Storage: " + e.getMessage());
        }
    }

    public interface OnImageUploadListener {
        void onUploadStart();
        void onUploadProgress(int progress);
        void onUploadSuccess(String downloadUrl);
        void onUploadFailure(String error);
    }

    /**
     * Upload medicine image to Firebase Storage
     * @param imageUri URI of the image to upload
     * @param listener Callback listener for upload events
     */
    public void uploadMedicineImage(Uri imageUri, OnImageUploadListener listener) {
        String fileName = "medicine_" + UUID.randomUUID().toString() + ".jpg";
        uploadImage(imageUri, MEDICINE_IMAGES_PATH, fileName, listener);
    }

    /**
     * Upload aquarium image to Firebase Storage
     * @param imageUri URI of the image to upload
     * @param listener Callback listener for upload events
     */
    public void uploadAquariumImage(Uri imageUri, OnImageUploadListener listener) {
        String fileName = "aquarium_" + UUID.randomUUID().toString() + ".jpg";
        uploadImage(imageUri, AQUARIUM_IMAGES_PATH, fileName, listener);
    }

    /**
     * Upload fish image to Firebase Storage
     * @param imageUri URI of the image to upload
     * @param listener Callback listener for upload events
     */
    public void uploadFishImage(Uri imageUri, OnImageUploadListener listener) {
        String fileName = "fish_" + UUID.randomUUID().toString() + ".jpg";
        uploadImage(imageUri, FISH_IMAGES_PATH, fileName, listener);
    }

    /**
     * Upload food image to Firebase Storage
     * @param imageUri URI of the image to upload
     * @param listener Callback listener for upload events
     */
    public void uploadFoodImage(Uri imageUri, OnImageUploadListener listener) {
        String fileName = "food_" + UUID.randomUUID().toString() + ".jpg";
        uploadImage(imageUri, FOOD_IMAGES_PATH, fileName, listener);
    }

    /**
     * Upload avatar image to Firebase Storage
     * @param imageUri URI of the image to upload
     * @param listener Callback listener for upload events
     */
    public void uploadAvatarImage(Uri imageUri, OnImageUploadListener listener) {
        String fileName = "avatar_" + UUID.randomUUID().toString() + ".jpg";
        uploadImage(imageUri, AVATAR_IMAGES_PATH, fileName, listener);
    }

    /**
     * Generic method to upload image to Firebase Storage
     * @param imageUri URI of the image to upload
     * @param storagePath Firebase Storage path
     * @param fileName Name of the file to save
     * @param listener Callback listener for upload events
     */
    private void uploadImage(Uri imageUri, String storagePath, String fileName, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onUploadFailure("Image URI is null");
            return;
        }

        if (storage == null || storageRef == null) {
            listener.onUploadFailure("Firebase Storage is not initialized");
            return;
        }

        StorageReference imageRef = storageRef.child(storagePath + fileName);

        Log.d(TAG, "Starting upload to path: " + storagePath + fileName);

        // Notify upload start
        listener.onUploadStart();

        // Start upload
        UploadTask uploadTask = imageRef.putFile(imageUri);

        // Monitor upload progress
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            listener.onUploadProgress((int) progress);
            Log.d(TAG, "Upload progress: " + progress + "%");
        });

        // Handle upload completion
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Image uploaded successfully");

            // Get download URL
            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String downloadUrl = downloadUri.toString();
                Log.d(TAG, "Download URL: " + downloadUrl);
                listener.onUploadSuccess(downloadUrl);
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Failed to get download URL", exception);
                listener.onUploadFailure("Failed to get download URL: " + exception.getMessage());
            });
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Image upload failed", exception);
            listener.onUploadFailure("Upload failed: " + exception.getMessage());
        });
    }

    /**
     * Delete image from Firebase Storage
     * @param imageUrl URL of the image to delete
     * @param listener Callback for delete operation
     */
    public void deleteImage(String imageUrl, OnImageDeleteListener listener) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            listener.onDeleteFailure("Image URL is empty");
            return;
        }

        if (storage == null) {
            listener.onDeleteFailure("Firebase Storage is not initialized");
            return;
        }

        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Image deleted successfully");
                listener.onDeleteSuccess();
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Failed to delete image", exception);
                listener.onDeleteFailure("Delete failed: " + exception.getMessage());
            });
        } catch (Exception e) {
            Log.e(TAG, "Invalid image URL", e);
            listener.onDeleteFailure("Invalid image URL: " + e.getMessage());
        }
    }

    public interface OnImageDeleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(String error);
    }
}