package com.example.aqualife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.request.SignUpRequest;
import com.example.aqualife.services.AuthService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterInformationActivity extends AppCompatActivity {
    private static final String TAG = "RegisterInfoActivity";
    private String phoneNumber;
    private boolean isNewUser;
    private EditText etRegisterName, etRegisterEmail, etRegisterPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_register);

        // Get data from intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        isNewUser = getIntent().getBooleanExtra("isNewUser", false);

        Log.d(TAG, "Phone number received: " + phoneNumber);
        Log.d(TAG, "Is new user: " + isNewUser);

        // Initialize views
        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Set click listeners
        btnRegister.setOnClickListener(v -> registerUser());
        tvBackToLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        String fullName = etRegisterName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(fullName, email, password)) {
            return;
        }

        // Disable button to prevent multiple submissions
        btnRegister.setEnabled(false);

        // Create registration request
        SignUpRequest signUpRequest = new SignUpRequest(fullName, phoneNumber, password, email);

        // Call API
        AuthService authService = ApiClient.getClient().create(AuthService.class);
        Call<Void> call = authService.signup(signUpRequest);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnRegister.setEnabled(true);

                if (response.isSuccessful()) {
                    Log.d(TAG, "Registration successful");
                    Toast.makeText(RegisterInformationActivity.this,
                            "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                    // Navigate to login screen
                    navigateToLogin();
                } else {
                    Log.e(TAG, "Registration failed: " + response.code());
                    String errorMessage = "Đăng ký thất bại. Vui lòng thử lại.";

                    if (response.code() == 400) {
                        errorMessage = "Thông tin không hợp lệ hoặc số điện thoại đã được sử dụng.";
                    } else if (response.code() == 409) {
                        errorMessage = "Số điện thoại hoặc email đã tồn tại.";
                    }

                    Toast.makeText(RegisterInformationActivity.this,
                            errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnRegister.setEnabled(true);
                Log.e(TAG, "Network error during registration", t);
                Toast.makeText(RegisterInformationActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String fullName, String email, String password) {
        if (fullName.isEmpty()) {
            etRegisterName.setError("Vui lòng nhập họ và tên");
            etRegisterName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etRegisterEmail.setError("Vui lòng nhập email");
            etRegisterEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError("Vui lòng nhập email hợp lệ");
            etRegisterEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etRegisterPassword.setError("Vui lòng nhập mật khẩu");
            etRegisterPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etRegisterPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etRegisterPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
        startActivity(intent);
        finish();
    }

}