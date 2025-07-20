package com.example.aqualife;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.request.FCMTokenRequest;
import com.example.aqualife.payload.response.FcmTokenResponse;
import com.example.aqualife.services.NotificationService;
import com.example.aqualife.util.NotificationHelper;
import com.example.aqualife.util.UserSessionManager;
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
        Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String title = remoteMessage.getNotification().getTitle();
            String message = remoteMessage.getNotification().getBody();

            // Show the notification
            NotificationHelper.showNotification(
                    this,
                    title != null ? title : "AquaLife",
                    message != null ? message : "",
                    (int) System.currentTimeMillis());
        }

        // Handle data payload (for chat messages and custom notifications)
        if (remoteMessage.getData().size() > 0) {
            // Check if this is a chat message (has senderId, message, chatMessageId)
            String senderId = remoteMessage.getData().get("senderId");
            String chatMessage = remoteMessage.getData().get("message");
            String chatMessageId = remoteMessage.getData().get("chatMessageId");

            if (senderId != null && chatMessage != null && chatMessageId != null) {
                Log.d(TAG, "Received chat message from user " + senderId + ": " + chatMessage);

                // Get current user ID
                UserSessionManager sessionManager = new UserSessionManager(this);
                String currentUserId = sessionManager.getUserId();

                // Only show notification if sender is NOT current user
                if (currentUserId == null || !currentUserId.equals(senderId)) {
                    String senderName = getSenderName(senderId);

                    Intent intent = new Intent("com.example.aqualife.NEW_CHAT_MESSAGE");
                    intent.putExtra("senderId", senderId);
                    intent.putExtra("message", chatMessage);
                    intent.putExtra("messageId", chatMessageId);
                    sendBroadcast(intent);

                    // Show chat notification
                    NotificationHelper.showNotification(
                            this,
                            senderName != null ? senderName : "Tin nhắn mới",
                            chatMessage,
                            Integer.parseInt(chatMessageId));
                }
                return;
            }

            // Handle other custom data messages (existing logic)
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

        // Use UserSessionManager for consistency
        UserSessionManager sessionManager = new UserSessionManager(this);
        String userIdString = sessionManager.getUserId();
        String jwtToken = sessionManager.getToken();

        if (userIdString != null && jwtToken != null) {
            try {
                int accountId = Integer.parseInt(userIdString);
                if (accountId > 0) {
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
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing user ID: " + userIdString, e);
            }
        } else {
            Log.w(TAG, "No user session found, cannot register token");
        }
    }

    private String getSenderName(String senderId) {
        // TODO: Implement this method to get sender name from your API or local cache
        // For now, return a default message
        return "AquaLife Store hỗ trợ";
    }
}