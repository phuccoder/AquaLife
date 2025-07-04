// Create this class: app/src/main/java/com/example/aqualife/model/AccountResponse.java
package com.example.aqualife.payload.response;

import com.google.gson.annotations.SerializedName;

public class AccountResponse {
    @SerializedName("accountId")
    private int accountId;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("address")
    private String address;

    @SerializedName("role")
    private String role;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("changedAt")
    private String changedAt;

    public AccountResponse(int accountId, String fullName, String email, String phoneNumber, String address, String role, String avatarUrl, boolean isActive, String createdAt, String changedAt) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.changedAt = changedAt;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }
}