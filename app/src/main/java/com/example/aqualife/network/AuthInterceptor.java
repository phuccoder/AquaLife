package com.example.aqualife.network;

import android.content.Context;

import com.example.aqualife.util.UserSessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private UserSessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = new UserSessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        if (originalRequest.url().toString().contains("api/v1/auth")) {
            return chain.proceed(originalRequest);
        }

        String token = sessionManager.getToken();
        if (token != null) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }
}