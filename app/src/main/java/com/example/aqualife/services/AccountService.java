package com.example.aqualife.services;

import com.example.aqualife.payload.response.AccountResponse;
import com.example.aqualife.model.Response;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AccountService {
    @GET("/api/accounts/me")
    Call<Response<AccountResponse>> getCurrentAccount();
}