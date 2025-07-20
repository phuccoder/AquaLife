package com.example.aqualife.services;

import com.example.aqualife.model.Response;
import com.example.aqualife.payload.request.ChatMessageRequest;
import com.example.aqualife.payload.response.ChatMessageReponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatMessageService {
    @POST("api/chat/send")
    Call<Response<ChatMessageReponse>> sendMessage(@Body ChatMessageRequest request);

    @GET("api/chat/messages")
    Call<Response<List<ChatMessageReponse>>> getMessages(
            @Query("receiverId") Integer receiverId,
            @Query("senderId") Integer senderId
    );
}