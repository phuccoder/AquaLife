package com.example.aqualife.payload.request;

public class FCMTokenRequest {
    private String jwtToken;
    private String token;
    private String deviceType;

    public FCMTokenRequest(String jwtToken, String token, String deviceType) {
        this.jwtToken = jwtToken;
        this.token = token;
        this.deviceType = deviceType;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}