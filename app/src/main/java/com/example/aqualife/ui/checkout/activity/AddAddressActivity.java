package com.example.aqualife.ui.checkout.activity;

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

import retrofit2.Call;
import retrofit2.Callback;

public class AddAddressActivity extends AppCompatActivity {

    private EditText edtPhone, edtAddress;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        Toolbar toolbar = findViewById(R.id.tbBack);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thêm địa chỉ");
        toolbar.setNavigationOnClickListener(v -> finish());
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            if (phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            AddressRequest request = new AddressRequest();
            request.setShippingPhone(phone);
            request.setShippingAddress(address);
            request.setDefault(false);

            AddressAPI api = ApiClient.getAuthenticatedClient(this)
                    .create(AddressAPI.class);

            api.createAddress(request).enqueue(new Callback<Response<AddressResponse>>() {
                @Override
                public void onResponse(Call<Response<AddressResponse>> call, retrofit2.Response<Response<AddressResponse>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddAddressActivity.this, "Đã thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddAddressActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Response<AddressResponse>> call, Throwable t) {
                    Toast.makeText(AddAddressActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

