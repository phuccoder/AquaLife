package com.example.aqualife.services;

import com.example.aqualife.payload.request.UpdateAccountRequest;
import com.example.aqualife.payload.response.AccountResponse;
import com.example.aqualife.model.Response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AccountService {
    @GET("/api/accounts/me")
    Call<Response<AccountResponse>> getCurrentAccount();

    @PUT("/api/accounts/{id}")
    Call<Response<AccountResponse>> updateAccount(@Path("id") int accountId, @Body UpdateAccountRequest request);
}