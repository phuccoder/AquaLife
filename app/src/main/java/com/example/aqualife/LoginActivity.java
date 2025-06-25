package com.example.aqualife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.request.SignInRequest;
import com.example.aqualife.payload.response.SignInResponse;
import com.example.aqualife.services.AuthService;
import com.example.aqualife.util.UserSessionManager;
import com.example.aqualife.util.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etPhoneNumber, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvContinueGuest;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupListeners();
        authService = ApiClient.getClient().create(AuthService.class);
    }

    private void initializeViews() {
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvContinueGuest = findViewById(R.id.tvContinueGuest);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, NumberInputActivity.class);
            startActivity(intent);
        });

        tvContinueGuest.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptLogin() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(phoneNumber, password)) {
            return;
        }

        btnLogin.setEnabled(false);

        SignInRequest request = new SignInRequest(phoneNumber, password);
        authService.signIn(request).enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                btnLogin.setEnabled(true);

                // Log the entire response
                Log.d(TAG, "API Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    SignInResponse signInResponse = response.body();

                    // Log successful response details
                    Log.d(TAG, "Response Status: " + signInResponse.getStatus());
                    Log.d(TAG, "Response Message: " + signInResponse.getMessage());

                    if (signInResponse.getData() != null) {
                        Log.d(TAG, "Access Token: " + signInResponse.getData().getAccessToken());
                    }

                    if (signInResponse.getStatus() == 200) {
                        Toast.makeText(LoginActivity.this,
                                signInResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        handleSuccessfulLogin(signInResponse);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập thất bại: " + signInResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";

                        // Log error response details
                        Log.e(TAG, "Login failed with error code: " + response.code());
                        Log.e(TAG, "Error body: " + errorBody);

                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập thất bại: Số điện thoại hoặc mật khẩu không đúng",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body: " + e.getMessage());
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                btnLogin.setEnabled(true);

                // Log the failure with throwable details
                Log.e(TAG, "Network error: " + t.getMessage(), t);

                Toast.makeText(LoginActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String phoneNumber, String password) {
        boolean isValid = true;

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
            etPhoneNumber.setError("Số điện thoại không hợp lệ");
            isValid = false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (password.length() < 5) {
            etPassword.setError("Mật khẩu phải có ít nhất 5 ký tự");
            isValid = false;
        }

        return isValid;
    }

    private void handleSuccessfulLogin(SignInResponse response) {
        // Save user data and token to SharedPreferences
        saveUserSession(response);

        // Navigate to main activity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveUserSession(SignInResponse response) {
        UserSessionManager sessionManager = new UserSessionManager(this);
        sessionManager.saveUserSession(response);
    }
}