package com.example.aqualife;

import static android.Manifest.permission.RECEIVE_SMS;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class NumberInputActivity extends AppCompatActivity {
    private static final String TAG = "NumberInputActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String phoneNumber;
    private EditText editTextPhone;
    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editTextPhone = findViewById(R.id.etRegisterPhone);
        buttonNext = findViewById(R.id.btnRegister);

        // Request SMS permission
        if (ContextCompat.checkSelfPermission(this, RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{RECEIVE_SMS}, 100);
        }

        buttonNext.setOnClickListener(v -> {
            phoneNumber = editTextPhone.getText().toString();
            if (validatePhoneNumber()) {
                buttonNext.setEnabled(false);
                Toast.makeText(this, "Đang xác thực số điện thoại...", Toast.LENGTH_SHORT).show();
                checkExistingUser(phoneNumber);
            }
        });
    }

    private boolean validatePhoneNumber() {
        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkExistingUser(String phoneNumber) {
        db.collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Existing user - go to sign in
                            buttonNext.setEnabled(true);
                            Intent intent = new Intent(NumberInputActivity.this, RegisterInformationActivity.class);
                            intent.putExtra("phoneNumber", phoneNumber);
                            Log.d(TAG, "Existing user with phone number: " + phoneNumber);
                            startActivity(intent);
                        } else {
                            // New user - start phone verification
                            Log.d(TAG, "New user, starting phone verification for: " + phoneNumber);
                            startPhoneVerification();
                        }
                    } else {
                        buttonNext.setEnabled(true);
                        Log.e(TAG, "Error checking user", task.getException());
                        Toast.makeText(NumberInputActivity.this,
                                "Lỗi kết nối: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startPhoneVerification() {
        buttonNext.setEnabled(false);
        Toast.makeText(this, "Đang xác thực số điện thoại...", Toast.LENGTH_SHORT).show();

        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "onVerificationCompleted: " + credential);
                        buttonNext.setEnabled(true);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.w(TAG, "onVerificationFailed", e);
                        buttonNext.setEnabled(true);

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(NumberInputActivity.this,
                                    "Số điện thoại không hợp lệ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(NumberInputActivity.this,
                                    "Quá nhiều yêu cầu từ thiết bị này. Hãy thử lại sau", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(NumberInputActivity.this,
                                    "Lỗi xác thực: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent: " + verificationId);
                        buttonNext.setEnabled(true);

                        // Save verification ID and resending token for later
                        Intent intent = new Intent(NumberInputActivity.this, VerifyOTPActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phoneNumber", phoneNumber);
                        startActivity(intent);
                    }
                };

        // Format phone number with country code (+84)
        String formattedPhoneNumber = "+84" + phoneNumber.substring(1);
        Log.d(TAG, "Starting verification for: " + formattedPhoneNumber);

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

                        // Navigate to registration information
                        Intent intent = new Intent(NumberInputActivity.this, RegisterInformationActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("isNewUser", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(NumberInputActivity.this,
                                    "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}