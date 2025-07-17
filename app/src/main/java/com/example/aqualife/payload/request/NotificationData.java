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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}