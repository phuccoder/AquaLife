package com.example.aqualife.services;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FiveManageAPI {
    @Multipart
    @POST("image")
    Call<ResponseBody> uploadImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image,
            @Part("metadata") RequestBody metadata
    );
}
