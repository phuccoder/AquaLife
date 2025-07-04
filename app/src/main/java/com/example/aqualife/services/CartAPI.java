package com.example.aqualife.services;

import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CartAPI {
    @POST("api/v1/carts/add-to-cart")
    Call<Response<CartResponse>> addToCart(
            @Header("Authorization") String token,
            @Body CreateOrUpdateCartRequest request
    );
    @GET("/api/v1/carts/me")
    Call<Response<CartResponse>> getCartByAccountId(
            @Header("Authorization") String token,
            @Query("accountId") int accountId
    );
    @GET("api/accounts/me")
    Call<Response<AccountInfor>> getAccountId(
            @Header("Authorization") String token
    );
    @PUT("api/v1/carts/{id}")
    Call<Response<CartResponse>> updateCart(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body CreateOrUpdateCartRequest request
    );
    @DELETE("/api/v1/carts/{id}")
    Call<Response<Object>> deleteCartById(
            @Header("Authorization") String token,
            @Path("id") int id
    );
}
