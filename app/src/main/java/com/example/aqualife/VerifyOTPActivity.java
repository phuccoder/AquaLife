package com.example.aqualife;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends AppCompatActivity {
    private static final String TAG = "VerifyOTPActivity";
    private FirebaseAuth auth;
    private String verificationId;
    private String phoneNumber;
    private EditText[] editTextOTP;
    private TextView phoneNumberText;
    private TextView tvResendOtp;
    private Button buttonVerify;
    private CountDownTimer resendTimer;
    private boolean canResendOTP = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Initialize arrays and variables
        auth = FirebaseAuth.getInstance();
        editTextOTP = new EditText[6];

        // Initialize EditText fields
        editTextOTP[0] = findViewById(R.id.otpBox1);
        editTextOTP[1] = findViewById(R.id.otpBox2);
        editTextOTP[2] = findViewById(R.id.otpBox3);
        editTextOTP[3] = findViewById(R.id.otpBox4);
        editTextOTP[4] = findViewById(R.id.otpBox5);
        editTextOTP[5] = findViewById(R.id.otpBox6);

        phoneNumberText = findViewById(R.id.phoneNumber);
        buttonVerify = findViewById(R.id.btnVerifyOtp);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        // Get intent extras
        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        Log.d(TAG, "verificationId received: " + verificationId);
        Log.d(TAG, "phoneNumber received: " + phoneNumber);

        // Set phone number in TextView
        if (phoneNumber != null) {
            phoneNumberText.setText(phoneNumber);
        }

        // Setup OTP input fields
        setupOTPInputs();

        // Start timer for OTP resend
        startResendTimer();

        buttonVerify.setOnClickListener(v -> {
            StringBuilder otpBuilder = new StringBuilder();
            for (EditText editText : editTextOTP) {
                otpBuilder.append(editText.getText().toString());
            }
            String otp = otpBuilder.toString();

            Log.d(TAG, "OTP submitted: " + otp);

            if (otp.length() == 6) {
                if (verificationId != null && !verificationId.isEmpty()) {
                    Log.d(TAG, "Attempting verification with OTP: " + otp + " and verificationId: " + verificationId);
                    verifyOTP(otp);
                } else {
                    Log.e(TAG, "VerificationId is null or empty");
                    Toast.makeText(this, "Lỗi xác thực: Không có ID xác thực", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "Incomplete OTP entered: " + otp + " (length: " + otp.length() + ")");
                Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số OTP", Toast.LENGTH_SHORT).show();
            }
        });

        tvResendOtp.setOnClickListener(v -> {
            if (canResendOTP) {
                resendOTP();
            } else {
                Toast.makeText(this, "Vui lòng đợi trước khi gửi lại mã", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupOTPInputs() {
        for (int i = 0; i < editTextOTP.length; i++) {
            final int currentIndex = i;

            editTextOTP[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < editTextOTP.length - 1) {
                        editTextOTP[currentIndex + 1].requestFocus();
                    }

                    // Check if all boxes are filled and log the complete OTP
                    if (currentIndex == editTextOTP.length - 1 && s.length() == 1) {
                        StringBuilder completeOtp = new StringBuilder();
                        boolean allFilled = true;

                        for (EditText et : editTextOTP) {
                            String digit = et.getText().toString();
                            if (digit.isEmpty()) {
                                allFilled = false;
                                break;
                            }
                            completeOtp.append(digit);
                        }

                        if (allFilled) {
                            Log.d(TAG, "Complete OTP entered: " + completeOtp.toString());
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Handle backspace key
            editTextOTP[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                        editTextOTP[currentIndex].getText().toString().isEmpty() &&
                        currentIndex > 0 &&
                        event.getAction() == KeyEvent.ACTION_DOWN) {
                    editTextOTP[currentIndex - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }

        // Set focus on first box
        editTextOTP[0].requestFocus();
    }

    private void verifyOTP(String otp) {
        buttonVerify.setEnabled(false);
        Toast.makeText(this, "Đang xác thực mã OTP...", Toast.LENGTH_SHORT).show();

        try {
            Log.d(TAG, "Creating credential with OTP: " + otp + " and verificationId: " + verificationId);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            Log.d(TAG, "Credential created, attempting to sign in");

            auth.signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        buttonVerify.setEnabled(true);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "OTP verification successful for: " + otp);
                            Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

                            // OTP verified successfully, go to registration info
                            Intent intent = new Intent(this, RegisterInformationActivity.class);
                            intent.putExtra("phoneNumber", phoneNumber);
                            intent.putExtra("isNewUser", true);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "OTP verification failed for: " + otp, task.getException());
                            String errorMessage = "Mã OTP không đúng";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Mã OTP không đúng hoặc đã hết hạn";
                            }

                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception during OTP verification for: " + otp, e);
            buttonVerify.setEnabled(true);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startResendTimer() {
        tvResendOtp.setEnabled(false);
        canResendOTP = false;

        resendTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendOtp.setText("Gửi lại mã sau " + (millisUntilFinished / 1000) + " giây");
            }

            @Override
            public void onFinish() {
                tvResendOtp.setText("Gửi lại mã OTP");
                tvResendOtp.setEnabled(true);
                canResendOTP = true;
            }
        }.start();
    }


    private void resendOTP() {
        String formattedPhoneNumber = "+84" + phoneNumber.substring(1);
        Log.d(TAG, "Resending OTP to: " + formattedPhoneNumber);
        Toast.makeText(this, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show();

        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "Resend - onVerificationCompleted");
                        // This will be called if the phone is instantly verified
                        Toast.makeText(VerifyOTPActivity.this,
                                "Xác thực tự động thành công!", Toast.LENGTH_SHORT).show();
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "Resend - onVerificationFailed", e);

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(VerifyOTPActivity.this,
                                    "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(VerifyOTPActivity.this,
                                    "Quá nhiều yêu cầu, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerifyOTPActivity.this,
                                    "Lỗi gửi OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "Resend - onCodeSent: " + newVerificationId);
                        verificationId = newVerificationId;
                        Toast.makeText(VerifyOTPActivity.this,
                                "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
                        startResendTimer();
                    }
                };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");

                        Intent intent = new Intent(this, RegisterInformationActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("isNewUser", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        super.onDestroy();
    }
}