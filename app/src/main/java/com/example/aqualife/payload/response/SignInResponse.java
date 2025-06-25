package com.example.aqualife.payload.response;

import com.google.gson.annotations.SerializedName;

public class SignInResponse {
    private int status;
    private String message;
    private Data data;

    public static class Data {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public String getToken() {
        return data != null ? data.accessToken : null;
    }

    public String getUserId() {
        return null;
    }

    public String getFullName() {
        return null;
    }

    public String getEmail() {
        return null;
    }

    public String getPhoneNumber() {
        return null;
    }
}