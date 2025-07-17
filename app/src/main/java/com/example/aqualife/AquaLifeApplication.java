package com.example.aqualife;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.example.aqualife.util.NotificationHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;

public class AquaLifeApplication extends Application {
    private static final String TAG = "AquaLifeStoreApp";

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        try {
            String firebaseId = FirebaseApp.getInstance().getOptions().getProjectId();
            Log.d("Firebase", "Connected to Firebase project: " + firebaseId);
            Log.d("Firebase", "Connected to Firebase: " + firebaseId);
            NotificationHelper.createNotificationChannel(this);
        } catch (Exception e) {
            Log.e("Firebase", "Error connecting to Firebase", e);
            Toast.makeText(this, "Firebase connection error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}