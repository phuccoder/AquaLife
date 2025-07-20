package com.example.aqualife.payload.request;

public class ChatMessageRequest {
    private Integer receiverId;
    private Integer senderId;
    private String message;

    public ChatMessageRequest(Integer receiverId, Integer senderId, String message) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.message = message;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
