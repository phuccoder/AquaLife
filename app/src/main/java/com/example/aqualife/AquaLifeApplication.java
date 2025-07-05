package com.example.aqualife;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;

public class AquaLifeApplication extends Application {
    private static final String TAG = "AquaLifeStoreApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);

            // Initialize Firebase Storage explicitly
            FirebaseStorage storage = FirebaseStorage.getInstance();
            Log.d(TAG, "Firebase initialized successfully");
            Log.d(TAG, "Storage bucket: " + storage.getReference().getBucket());
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }
    }
}