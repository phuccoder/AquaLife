package com.example.aqualife.model;

public class OrderRequest {
    private int cartId;
    private int accountAddId;
    private String description;

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
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

}
