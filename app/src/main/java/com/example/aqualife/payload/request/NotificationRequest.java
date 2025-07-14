package com.example.aqualife.payload.request;

public class NotificationRequest {
    private String targetAccountId;
    private NotificationData data;

    public NotificationRequest(String targetAccountId, NotificationData data) {
        this.targetAccountId = targetAccountId;
        this.data = data;
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(String targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public NotificationData getData() {
        return data;
    }

    public void setData(NotificationData data) {
        this.data = data;
    }
}
