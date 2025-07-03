package com.example.aqualife.util;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phone) {
        return !phone.isEmpty() &&
                (phone.matches("^(0|\\+84)[3|5|7|8|9][0-9]{8}$"));
    }
}