package com.example.aqualife.model;

import java.io.Serializable;

public class AddressResponse implements Serializable {
    private int accountAddId;
    private int accountId;
    private String shippingAddress;
    private boolean isDefault;
    private String shippingPhone;
    private boolean isActive;
    private String createdAt;
    private String changeAt;

    public int getAccountAddId() {
        return accountAddId;
    }

    public void setAccountAddId(int accountAddId) {
        this.accountAddId = accountAddId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getChangeAt() {
        return changeAt;
    }

    public void setChangeAt(String changeAt) {
        this.changeAt = changeAt;
    }
}
