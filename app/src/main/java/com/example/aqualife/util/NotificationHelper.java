package com.example.aqualife.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.aqualife.MainActivity;
import com.example.aqualife.R;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.request.FCMTokenRequest;
import com.example.aqualife.payload.response.FcmTokenResponse;
import com.example.aqualife.payload.response.NotificationResponse;
import com.example.aqualife.services.NotificationService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NotificationService.CHANNEL_ID,
                    NotificationService.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(NotificationService.CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, String title, String message, int notificationId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }

    public static void registerFCMToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);

                    // Get JWT token from session
                    UserSessionManager sessionManager = new UserSessionManager(context);
                    String jwtToken = sessionManager.getToken();

                    // Register token with server
                    ApiClient.getAuthenticatedClient(context)
                            .create(NotificationService.class)
                            .registerToken(new FCMTokenRequest(jwtToken, token, "android"))
                            .enqueue(new Callback<FcmTokenResponse>() {
                                @Override
                                public void onResponse(Call<FcmTokenResponse> call, Response<FcmTokenResponse> response) {
                                    if (response.isSuccessful()) {
                                        Log.d(TAG, "FCM Token registered successfully");
                                        FcmTokenResponse fcmResponse = response.body();
                                        if (fcmResponse != null) {
                                            Log.d(TAG, "FCM Token ID: " + fcmResponse.getFcmTokenId());
                                        }
                                    } else {
                                        Log.e(TAG, "FCM Token registration failed: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<FcmTokenResponse> call, Throwable t) {
                                    Log.e(TAG, "FCM Token registration failed", t);
                                }
                            });
                });
    }

    public static void getNotifications(Context context, int accountId, NotificationCallback callback) {
        ApiClient.getAuthenticatedClient(context)
                .create(NotificationService.class)
                .getNotifications(accountId)
                .enqueue(new Callback<List<NotificationResponse>>() {
                    @Override
                    public void onResponse(Call<List<NotificationResponse>> call, Response<List<NotificationResponse>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Notifications retrieved successfully");
                            callback.onSuccess(response.body());
                        } else {
                            Log.e(TAG, "Failed to retrieve notifications: " + response.code());
                            callback.onFailure("Failed to retrieve notifications");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                        Log.e(TAG, "Failed to retrieve notifications", t);
                        callback.onFailure(t.getMessage());
                    }
                });
    }

    public interface NotificationCallback {
        void onSuccess(List<NotificationResponse> notifications);
        void onFailure(String error);
    }
}