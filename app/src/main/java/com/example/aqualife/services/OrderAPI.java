package com.example.aqualife.services;

import com.example.aqualife.model.AddressRequest;
import com.example.aqualife.model.AddressResponse;
import com.example.aqualife.model.OrderRequest;
import com.example.aqualife.model.OrderResponse;
import com.example.aqualife.model.Response;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderAPI {
    @POST("api/orders")
    Call<Response<OrderResponse>> createOrder(
            @Query("paymentMethod") String paymentMethod,
            @Body OrderRequest request
    );
    @POST("api/orders/{id}/cancel")
    Call<Response<String>> cancelOrder(
            @Path("id") int orderId
    );
    @GET("api/orders/account/{id}")
    Call<Response<List<OrderResponse>>> getOrdersByAccountId(
            @Path("id") int accountId
    );
    @GET("api/orders/{id}")
    Call<Response<List<OrderResponse>>> getOrdersById(
            @Path("id") int id
    );

    @POST("api/orders/{id}/pre-cancel")
    Call<Response<String>> preCancelOrder(@Path("id") int orderId);
}

