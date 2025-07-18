package com.example.aqualife.payload.request;

public class NotificationRequest {
    private Integer accountId;
    private String message;

    // Add constructor
    public NotificationRequest(Integer accountId, String message) {
        this.accountId = accountId;
        this.message = message;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}