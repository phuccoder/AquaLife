package com.example.aqualife.payload.response;

public class ChatMessageReponse {
    private int chatMessageId;
    private int accountId;
    private String message;
    private String sendAt;
    private int sendBy;

    // Getters and setters
    public int getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(int chatMessageId) { this.chatMessageId = chatMessageId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSendAt() { return sendAt; }
    public void setSendAt(String sendAt) { this.sendAt = sendAt; }

    public int getSendBy() { return sendBy; }
    public void setSendBy(int sendBy) { this.sendBy = sendBy; }
}