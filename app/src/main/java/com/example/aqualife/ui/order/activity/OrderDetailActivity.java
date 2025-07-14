package com.example.aqualife.ui.order.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.adapter.CartItemAdapter;
import com.example.aqualife.adapter.OrderAdapter;
import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.AddressResponse;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.OrderResponse;
import com.example.aqualife.model.Response;
import com.example.aqualife.model.ShippingResponse;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.AddressAPI;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.ShippingAPI;
import com.example.aqualife.ui.checkout.activity.PaymentActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView txtShippingStatus, txtShippingTime, txtReceiverName, txtShippingAddress, txtOrderId, txtOrderDate;
    private RecyclerView recyclerCartItems;
    private MaterialToolbar tbBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();

        OrderResponse order = (OrderResponse) getIntent().getSerializableExtra("order");

        if (order != null) {
            bindOrderData(order);
            tbBack = findViewById(R.id.toolbar);
            tbBack.setOnClickListener(v -> {
                finish();
            });
        }
    }

    private void initViews() {
        txtShippingStatus = findViewById(R.id.txtShippingStatus);
        txtShippingTime = findViewById(R.id.txtShippingTime);
        txtReceiverName = findViewById(R.id.txtReceiverName);
        txtShippingAddress = findViewById(R.id.txtShippingAddress);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        recyclerCartItems = findViewById(R.id.recyclerCartItems);
    }

    private void bindOrderData(OrderResponse order) {
        try{
            setOrderStatus(order);
            fetchShippingData(order.getShippingId());
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(order.getOrderDate());
            String formattedDate = outputFormat.format(date);
            txtOrderDate.setText("Ngày đặt hàng: " + formattedDate);

            txtOrderId.setText("Mã đơn hàng: #" + order.getOrderId());

            CartItemAdapter adapter = new CartItemAdapter(order.getCartItems(), this);
            recyclerCartItems.setLayoutManager(new LinearLayoutManager(this));
            recyclerCartItems.setAdapter(adapter);
        }catch (Exception ex){
            Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void setOrderStatus(OrderResponse order) {
        String displayText;
        switch (order.getOrderStatus().toUpperCase()) {
            case "DONE":
            case "HOÀN THÀNH":
                displayText = "Giao Hàng Thành Công";
                break;

            case "CANCEL":
            case "ĐÃ HỦY":
                displayText = "Đơn hàng đã hủy";
                break;

            case "PENDING":
            case "CHỜ XÁC NHẬN":
                displayText = "Đơn hàng chờ xác nhận";
                break;

            default:
                displayText = "Đơn hàng đang chờ vận chuyển";
                break;
        }
        txtShippingStatus.setText(displayText);
    }
    private void fetchShippingData(int id) {
        ShippingAPI api = ApiClient.getAuthenticatedClient(this)
                .create(ShippingAPI.class);

        api.getShippingById(id).enqueue(new Callback<Response<ShippingResponse>>() {
            @Override
            public void onResponse(Call<Response<ShippingResponse>> call, retrofit2.Response<Response<ShippingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if(response.body().getData().getDeliveryDate() != null){
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String formattedDate = sdf.format(response.body().getData().getDeliveryDate());
                        txtShippingTime.setText(formattedDate);
                    }else {
                        txtShippingTime.setText("Đơn hàng đang chờ xác nhận");
                    }
                    fetchAddressData(response.body().getData().getAccountAddId());
                }
            }

            @Override
            public void onFailure(Call<Response<ShippingResponse>> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchAddressData(int id) {
        AddressAPI api = ApiClient.getAuthenticatedClient(this)
                .create(AddressAPI.class);

        api.getAddressByID(id).enqueue(new Callback<Response<AddressResponse>>() {
            @Override
            public void onResponse(Call<Response<AddressResponse>> call, retrofit2.Response<Response<AddressResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtReceiverName.setText(response.body().getData().getAccountId() + " - " + response.body().getData().getShippingPhone());
                    txtShippingAddress.setText(response.body().getData().getShippingAddress());
                }
            }

            @Override
            public void onFailure(Call<Response<AddressResponse>> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

