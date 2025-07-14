package com.example.aqualife.ui.checkout.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.adapter.AddressAdapter;
import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.AddressRequest;
import com.example.aqualife.model.AddressResponse;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.AddressAPI;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class SelectAddressActivity extends AppCompatActivity {
    private RecyclerView rvAddresses;
    private Button btnAddNew;
    private AddressAdapter adapter;
    private AccountInfor account;
    private List<AddressResponse> addressList = new ArrayList<>();
    private AddressResponse updatedAddress;
    private ActivityResultLauncher<Intent> addAddressLauncher;
    private ActivityResultLauncher<Intent> editAddressLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);
        Toolbar toolbar = findViewById(R.id.tbBack);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chọn địa chỉ nhận hàng");
        toolbar.setNavigationOnClickListener(v -> {
            if(updatedAddress != null){
                if(updatedAddress.isDefault()){
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedAddress", updatedAddress);
                    setResult(RESULT_OK, resultIntent);
                }
            }
            finish();
        });
        rvAddresses = findViewById(R.id.rvAddresses);
        btnAddNew = findViewById(R.id.btnAddAddress);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        account = (AccountInfor) getIntent().getSerializableExtra("account");
        editAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String selectedAddressJson = data.getStringExtra("updatedAddress");
                            updatedAddress = new Gson().fromJson(selectedAddressJson, AddressResponse.class);
                            replaceAddressById(updatedAddress);
                        }
                    }
                }
        );
        addAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fetchAddresses();
                    }
                }
        );
        btnAddNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            addAddressLauncher.launch(intent);
        });

        try{
            fetchAddresses();
        }catch (Exception ex){
            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAddresses() {
        AddressAPI api = ApiClient.getAuthenticatedClient(this).create(AddressAPI.class);

        api.getAddress(account.getAccountId()).enqueue(new Callback<Response<List<AddressResponse>>>() {
            @Override
            public void onResponse(Call<Response<List<AddressResponse>>> call,
                                   retrofit2.Response<Response<List<AddressResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addressList = response.body().getData();

                    adapter = new AddressAdapter(addressList, account, new AddressAdapter.OnAddressSelectListener() {
                        @Override
                        public void onSelect(AddressResponse address) {
                            updateDefaultAddress(address);
                        }

                        @Override
                        public void onEdit(AddressResponse address) {
                            Intent intent = new Intent(SelectAddressActivity.this, EditAddressActivity.class);
                            intent.putExtra("editAddress", address);
                            editAddressLauncher.launch(intent);
                        }
                    });

                    rvAddresses.setAdapter(adapter);
                } else {
                    Toast.makeText(SelectAddressActivity.this, "Không thể lấy danh sách địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<List<AddressResponse>>> call, Throwable t) {
                Toast.makeText(SelectAddressActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDefaultAddress(AddressResponse selectedAddress) {
        AddressAPI api = ApiClient.getAuthenticatedClient(this)
                .create(AddressAPI.class);

        AddressRequest request = new AddressRequest();
        request.setShippingAddress(selectedAddress.getShippingAddress());
        request.setShippingPhone(selectedAddress.getShippingPhone());
        request.setDefault(true);

        api.updateAddress(selectedAddress.getAccountAddId(), request)
                .enqueue(new Callback<Response<AddressResponse>>() {
                    @Override
                    public void onResponse(Call<Response<AddressResponse>> call,
                                           retrofit2.Response<Response<AddressResponse>> response) {
                        if (response.isSuccessful()) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("selectedAddress", selectedAddress);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(SelectAddressActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<AddressResponse>> call, Throwable t) {
                        String mess = t.getMessage();
                        Toast.makeText(SelectAddressActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void replaceAddressById(AddressResponse updatedAddress) {
        for (int i = 0; i < addressList.size(); i++) {
            if (addressList.get(i).getAccountAddId() == updatedAddress.getAccountAddId()) {
                addressList.set(i, updatedAddress); // Replace it
                adapter.notifyItemChanged(i); // Notify adapter to refresh that item
                return;
            }
        }
    }


}