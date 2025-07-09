package com.example.aqualife.services;

import com.example.aqualife.model.AddressRequest;
import com.example.aqualife.model.AddressResponse;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Response;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AddressAPI {
    @POST("api/account-adds")
    Call<Response<AddressResponse>> createAddress(
            @Body AddressRequest request
    );
    @GET("api/account-adds/account/{id}")
    Call<Response<List<AddressResponse>>> getAddress(
            @Path("id") int accountId
    );
    @PUT("api/account-adds/{id}")
    Call<Response<AddressResponse>> updateAddress(
            @Path("id") int addressId,
            @Body AddressRequest request
    );
}
