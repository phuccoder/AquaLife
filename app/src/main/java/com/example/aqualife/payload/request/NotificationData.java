package com.example.aqualife.payload.request;

public class NotificationData {
    private String title;
    private String body;
    private String type;
    private String targetId;

    public NotificationData(String title, String body, String type, String targetId) {
        this.title = title;
        this.body = body;
        this.type = type;
        this.targetId = targetId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getType() {
        return type;
    }

    public String getTargetId() {
        return targetId;
    }
}