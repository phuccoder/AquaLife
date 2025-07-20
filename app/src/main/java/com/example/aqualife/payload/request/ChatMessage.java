package com.example.aqualife.payload.request;

public class ChatMessage {
    private int chatMessageId;
    private String message;
    private int sendBy;
    private String sendAt;
    private String deliveryStatus;

    public ChatMessage() {}

    public int getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(int chatMessageId) { this.chatMessageId = chatMessageId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getSendBy() { return sendBy; }
    public void setSendBy(int sendBy) { this.sendBy = sendBy; }

    public String getSendAt() { return sendAt; }
    public void setSendAt(String sendAt) { this.sendAt = sendAt; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    // Add this method
    public boolean isSentByCurrentUser(int userId) {
        return this.sendBy == userId;
    }

    public boolean isFailedMessage() {
        return "FAILED".equalsIgnoreCase(deliveryStatus);
    }

    public boolean isSendingMessage() {
        return "SENDING".equalsIgnoreCase(deliveryStatus);
    }
}