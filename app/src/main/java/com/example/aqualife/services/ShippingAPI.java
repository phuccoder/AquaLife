package com.example.aqualife.services;

import com.example.aqualife.model.Response;
import com.example.aqualife.model.ShippingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ShippingAPI {
    @GET("api/shippings/{shippingId}")
    Call<Response<ShippingResponse>> getShippingById(
            @Path("shippingId") int id
    );
}
