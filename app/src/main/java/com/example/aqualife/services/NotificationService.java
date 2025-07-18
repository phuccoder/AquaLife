package com.example.aqualife.services;

import com.example.aqualife.payload.request.FCMTokenRequest;
import com.example.aqualife.payload.request.NotificationRequest;
import com.example.aqualife.payload.response.FcmTokenResponse;
import com.example.aqualife.payload.response.NotificationResponse;
import com.example.aqualife.model.Response;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NotificationService {

    String CHANNEL_ID = "aquaLifeStoreChannel";
    String CHANNEL_NAME = "AquaLife Notifications";
    String CHANNEL_DESCRIPTION = "Notifications for AquaLife Store";

    @POST("api/fcm/token")
    Call<FcmTokenResponse> registerToken(@Body FCMTokenRequest request);

    @GET("api/notifications")
    Call<Response<List<NotificationResponse>>> getNotifications(@Query("accountId") int accountId);

    @POST("api/notifications")
    Call<Void> sendNotification(@Body NotificationRequest request);
}