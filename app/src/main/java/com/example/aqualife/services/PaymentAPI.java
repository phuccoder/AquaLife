package com.example.aqualife.services;

import com.example.aqualife.model.OrderRequest;
import com.example.aqualife.model.OrderResponse;
import com.example.aqualife.model.PaymentRequest;
import com.example.aqualife.model.PaymentResponse;
import com.example.aqualife.model.Response;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentAPI {
    @POST("api/payments")
    Call<Response<PaymentResponse>> createPayment(
            @Body PaymentRequest request
    );
}
