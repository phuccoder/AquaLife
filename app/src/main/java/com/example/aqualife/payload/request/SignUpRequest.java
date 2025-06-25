package com.example.aqualife.payload.request;

public class SignUpRequest {
    private String fullName;
    private String phoneNumber;
    private String password;
    private String email;

    public SignUpRequest(String fullName, String phoneNumber, String password, String email) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
