package com.example.aqualife.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aqualife.payload.response.SignInResponse;

public class UserSessionManager {
    private static final String PREF_NAME = "AquaLifeUserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void saveUserSession(SignInResponse response) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        if (response.getData() != null) {
            editor.putString(KEY_TOKEN, response.getData().getAccessToken());
            editor.putString(KEY_REFRESH_TOKEN, response.getData().getRefreshToken());
        }

        editor.apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getRefreshToken() {
        return preferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserPhone() {
        return preferences.getString(KEY_USER_PHONE, null);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}