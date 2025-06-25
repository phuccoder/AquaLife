package com.example.aqualife.services;

import com.example.aqualife.payload.request.SignInRequest;
import com.example.aqualife.payload.request.SignUpRequest;
import com.example.aqualife.payload.response.SignInResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/v1/auth/register")
    Call<Void> signup(@Body SignUpRequest signupRequest);

    @POST("api/v1/auth/login")
    Call<SignInResponse> signIn(@Body SignInRequest request);

}
