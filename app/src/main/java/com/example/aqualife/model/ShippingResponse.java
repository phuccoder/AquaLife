package com.example.aqualife.model;

public class ShippingResponse {
    private int shippingId;
    private int accountAddId;
    private String description;
    private String shippingStatus;
    private String deliveryDate;
    private String createdAt;
    private String changeAt;

    public int getShippingId() {
        return shippingId;
    }

    public void setShippingId(int shippingId) {
        this.shippingId = shippingId;
    }

    public int getAccountAddId() {
        return accountAddId;
    }

    public void setAccountAddId(int accountAddId) {
        this.accountAddId = accountAddId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getChangedAt() {
        return changeAt;
    }

    public void setChangedAt(String changedAt) {
        this.changeAt = changedAt;
    }
}
