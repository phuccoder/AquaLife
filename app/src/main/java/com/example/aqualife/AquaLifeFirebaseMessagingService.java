package com.example.aqualife;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.request.FCMTokenRequest;
import com.example.aqualife.payload.response.FcmTokenResponse;
import com.example.aqualife.services.NotificationService;
import com.example.aqualife.util.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AquaLifeFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "AquaLifeFCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();

            // Show the notification
            NotificationHelper.showNotification(
                    this,
                    title != null ? title : "AquaLife Store",
                    message != null ? message : "",
                    (int) System.currentTimeMillis());
        }

        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // You can handle custom data here
            String customTitle = remoteMessage.getData().get("title");
            String customMessage = remoteMessage.getData().get("message");

            if (customTitle != null && customMessage != null) {
                NotificationHelper.showNotification(
                        this,
                        customTitle,
                        customMessage,
                        (int) System.currentTimeMillis());
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        int accountId = getAccountId();
        String jwtToken = getSharedPreferences("user_session", MODE_PRIVATE)
                .getString("jwt_token", null);

        if (accountId > 0 && jwtToken != null) {
            ApiClient.getAuthenticatedClient(this)
                    .create(NotificationService.class)
                    .registerToken(new FCMTokenRequest(jwtToken, token, "android"))
                    .enqueue(new Callback<FcmTokenResponse>() {
                        @Override
                        public void onResponse(Call<FcmTokenResponse> call, Response<FcmTokenResponse> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Token update successful");
                            } else {
                                Log.e(TAG, "Token update failed: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<FcmTokenResponse> call, Throwable t) {
                            Log.e(TAG, "Token update failed", t);
                        }
                    });
        }
    }

    private int getAccountId() {
        String userIdString = getSharedPreferences("user_session", MODE_PRIVATE)
                .getString("user_id", "0");
        try {
            return Integer.parseInt(userIdString);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing user ID", e);
            return 0;
        }
    }
}