package com.example.aqualife.payload.response;

import java.time.LocalDateTime;

public class FcmTokenResponse {
    private Integer fcmTokenId;
    private Integer accountId;
    private String token;
    private String deviceType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FcmTokenResponse() {}

    public Integer getFcmTokenId() {
        return fcmTokenId;
    }

    public void setFcmTokenId(Integer fcmTokenId) {
        this.fcmTokenId = fcmTokenId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}