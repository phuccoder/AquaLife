package com.example.aqualife.services;

import com.example.aqualife.model.Product;
import com.example.aqualife.model.ProductListData;
import com.example.aqualife.model.Response;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductAPI {
    @GET("api/v1/products")
    Call<Response<ProductListData>> getProducts(
            @Query("keyword") String keyword,
            @Query("type") String type,
            @Query("minPrice") Integer minPrice,
            @Query("maxPrice") Integer maxPrice,
            @Query("sortBy") String sortBy
    );

    @GET("api/v1/products/{id}")
    Call<Response<Product>> getProductById(
            @Path("id") int productId
    );
}