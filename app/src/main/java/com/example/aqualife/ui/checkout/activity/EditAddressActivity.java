package com.example.aqualife.ui.checkout.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.aqualife.R;
import com.example.aqualife.model.AddressRequest;
import com.example.aqualife.model.AddressResponse;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.AddressAPI;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class EditAddressActivity extends AppCompatActivity {

    private EditText edtPhone, edtAddress;
    private Button btnSave;
    private AddressResponse currentAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);
        Toolbar toolbar = findViewById(R.id.tbBack);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chỉnh sửa địa chỉ");
        toolbar.setNavigationOnClickListener(v -> finish());
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSave = findViewById(R.id.btnSave);

        currentAddress = (AddressResponse) getIntent().getSerializableExtra("editAddress");

        if (currentAddress != null) {
            edtPhone.setText(currentAddress.getShippingPhone());
            edtAddress.setText(currentAddress.getShippingAddress());
        }

        btnSave.setOnClickListener(v -> {
            String newPhone = edtPhone.getText().toString().trim();
            String newAddress = edtAddress.getText().toString().trim();

            if (newPhone.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            AddressRequest request = new AddressRequest();
            request.setShippingPhone(newPhone);
            request.setShippingAddress(newAddress);
            request.setDefault(currentAddress.isDefault());

            AddressAPI api = ApiClient.getAuthenticatedClient(this)
                    .create(AddressAPI.class);

            api.updateAddress(currentAddress.getAccountAddId(), request)
                    .enqueue(new Callback<Response<AddressResponse>>() {
                        @Override
                        public void onResponse(Call<Response<AddressResponse>> call, retrofit2.Response<Response<AddressResponse>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(EditAddressActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("updatedAddress", new Gson().toJson(response.body().getData()));
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Toast.makeText(EditAddressActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Response<AddressResponse>> call, Throwable t) {
                            Toast.makeText(EditAddressActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

