package com.example.aqualife.services;

import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ProductAPI {
    @GET("api/v1/products/{id}")
    Call<Response<Product>> getProductById(
            @Header("Authorization") String token,
            @Path("id") int productId
    );
}
