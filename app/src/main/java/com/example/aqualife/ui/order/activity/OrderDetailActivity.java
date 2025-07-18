package com.example.aqualife.ui.order.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import com.example.aqualife.R;
import com.example.aqualife.adapter.CartItemAdapter;
import com.example.aqualife.model.AddressResponse;
import com.example.aqualife.model.OrderResponse;
import com.example.aqualife.model.Response;
import com.example.aqualife.model.ShippingResponse;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.AddressAPI;
import com.example.aqualife.services.OrderAPI;
import com.example.aqualife.services.ShippingAPI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView txtShippingStatus, txtShippingTime, txtReceiverName, txtShippingAddress, txtOrderId, txtOrderDate;
    private ImageView imgStep1, imgStep2, imgStep3, imgStep4;
    private TextView txtStep1, txtStep2, txtStep3, txtStep4;
    private RecyclerView recyclerCartItems;
    private MaterialToolbar tbBack;
    private MaterialButton btnCancelOrder;
    private MaterialButton btnCallSupport;
    private MaterialToolbar toolbar;
    private String orderId;
    private String orderStatus;
    private String supportPhone = "0967630810";
    private static final int REQUEST_CALL_PERMISSION = 1;
    private TextView tvShippingPolicy;
    private TextView tvCancelPolicy;
    private TextView tvCannotCancelPolicy;
    private TextView tvRefundPolicy;
    private TextView tvSupportIntro;
    private TextView tvImportantNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();
        setupToolbar();
        setupListeners();

        OrderResponse order = (OrderResponse) getIntent().getSerializableExtra("order");
        if (order != null) {
            int statusCode = getOrderStatusCode(order.getOrderStatus());
            updateOrderNote(statusCode);
            bindOrderData(order);
        }
        updateCancelButtonVisibility();

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
        // Initialize checkpoint views
        imgStep1 = findViewById(R.id.imgStep1);
        txtStep1 = findViewById(R.id.txtStep1);
        imgStep2 = findViewById(R.id.imgStep2);
        txtStep2 = findViewById(R.id.txtStep2);
        imgStep3 = findViewById(R.id.imgStep3);
        txtStep3 = findViewById(R.id.txtStep3);
        imgStep4 = findViewById(R.id.imgStep4);
        txtStep4 = findViewById(R.id.txtStep4);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnCallSupport = findViewById(R.id.btnCallSupport);
        toolbar = findViewById(R.id.toolbar);
        tvShippingPolicy = findViewById(R.id.tvShippingPolicy);
        tvCancelPolicy = findViewById(R.id.tvCancelPolicy);
        tvCannotCancelPolicy = findViewById(R.id.tvCannotCancelPolicy);
        tvRefundPolicy = findViewById(R.id.tvRefundPolicy);
        tvSupportIntro = findViewById(R.id.tvSupportIntro);
        tvImportantNotes = findViewById(R.id.tvImportantNotes);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnCancelOrder.setOnClickListener(v -> showCancelOrderDialog());
        btnCallSupport.setOnClickListener(v -> callSupport());
    }

    private void bindOrderData(OrderResponse order) {
        try {
            orderStatus = order.getOrderStatus();
            setOrderStatus(order);
            fetchShippingData(order.getShippingId(), order);
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(order.getOrderDate());
            String formattedDate = outputFormat.format(date);
            txtOrderDate.setText("Ngày đặt hàng: " + formattedDate);

            txtOrderId.setText("Mã đơn hàng: #" + order.getOrderId());

            CartItemAdapter adapter = new CartItemAdapter(order.getCartItems(), this);
            recyclerCartItems.setLayoutManager(new LinearLayoutManager(this));
            recyclerCartItems.setAdapter(adapter);
        } catch (Exception ex) {
            Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int getOrderStatusCode(String orderStatus) {
        if (orderStatus == null) return 0;
        switch (orderStatus.toUpperCase()) {
            case "PENDING":
            case "CHỜ XÁC NHẬN":
                return 1;
            case "PROCESS":
            case "ĐÃ XÁC NHẬN":
                return 2;
            case "SHIPPING":
            case "ĐANG GIAO":
                return 3;
            case "DONE":
            case "HOÀN THÀNH":
                return 4;
            default:
                return 0;
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

    private void updateCancelButtonVisibility() {
        if (canCancelOrder()) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private boolean canCancelOrder() {
        return "Pending".equals(orderStatus);
    }

    private void updateOrderNote(int status) {
        TextView txtOrderNote = findViewById(R.id.txtOrderNote);
        String note;
        int color;

        switch (status) {
            case 1:
                note = "AquaLife đang xác nhận đơn hàng của bạn. Vui lòng chờ trong giây lát nhé!";
                color = getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case 2:
                note = "Đơn hàng đã được xác nhận. AquaLife sẽ liên hệ và giao hàng trong vòng 2–7 ngày làm việc tới.";
                color = getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case 3:
                note = "Đơn hàng đang trên đường giao. Hãy để ý điện thoại để nhận hàng nhé!";
                color = getResources().getColor(android.R.color.holo_green_dark);
                break;
            case 4:
                note = "Đơn hàng đã hoàn tất. Cảm ơn bạn đã đồng hành cùng AquaLife!";
                color = getResources().getColor(android.R.color.holo_purple);
                break;
            default:
                note = "AquaLife chưa xác định được trạng thái đơn hàng.";
                color = getResources().getColor(android.R.color.darker_gray);
        }

        txtOrderNote.setText(note);
        txtOrderNote.setTextColor(color);
    }

    private void showCancelOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_cancel, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtOrderIdConfirm = dialogView.findViewById(R.id.txtOrderIdConfirm);
        MaterialButton btnCancelDialog = dialogView.findViewById(R.id.btnCancelDialog);
        MaterialButton btnConfirmCancel = dialogView.findViewById(R.id.btnConfirmCancel);

        txtOrderIdConfirm.setText("Mã đơn hàng: #" + orderId);

        btnConfirmCancel.setOnClickListener(v -> {
            int orderIdInt = Integer.parseInt(orderId);
            OrderAPI orderAPI = ApiClient.getAuthenticatedClient(this)
                    .create(OrderAPI.class);

            orderAPI.preCancelOrder(orderIdInt).enqueue(new Callback<Response<String>>() {
                @Override
                public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(OrderDetailActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        finish(); // Optionally close activity
                    } else {
                        Toast.makeText(OrderDetailActivity.this, "Failed to cancel order", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Response<String>> call, Throwable t) {
                    Toast.makeText(OrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchShippingData(int id, OrderResponse order) {
        ShippingAPI api = ApiClient.getAuthenticatedClient(this)
                .create(ShippingAPI.class);

        api.getShippingById(id).enqueue(new Callback<Response<ShippingResponse>>() {
            @Override
            public void onResponse(Call<Response<ShippingResponse>> call, retrofit2.Response<Response<ShippingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ShippingResponse shipping = response.body().getData();
                    if (shipping.getDeliveryDate() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        String formattedDate = sdf.format(shipping.getDeliveryDate());
                        txtShippingTime.setText(formattedDate);
                    } else {
                        txtShippingTime.setText("Đơn hàng đang chờ xác nhận");
                    }
                    // Update checkpoint with both order and shipping status
                    updateOrderCheckpoint(order.getOrderStatus(), shipping.getShippingStatus());
                    fetchAddressData(shipping.getAccountAddId());
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

    private void updateOrderCheckpoint(String orderStatus, String shippingStatus) {
        // Reset all steps to inactive
        imgStep1.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        txtStep1.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
        imgStep2.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        txtStep2.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
        imgStep3.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        txtStep3.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
        imgStep4.setColorFilter(ContextCompat.getColor(this, R.color.gray));
        txtStep4.setTextColor(ContextCompat.getColor(this, R.color.gray_text));

        // Normalize status strings for comparison
        String normalizedOrderStatus = orderStatus.toUpperCase();
        String normalizedShippingStatus = shippingStatus.toUpperCase();

        // Activate steps based on status
        if (normalizedOrderStatus.equals("CANCEL") || normalizedShippingStatus.equals("CANCEL")) {
            imgStep1.setColorFilter(ContextCompat.getColor(this, R.color.red));
            txtStep1.setText("Đơn hàng đã hủy");
            txtStep1.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {

            if (normalizedOrderStatus.equals("PENDING") && normalizedShippingStatus.equals("PENDING")) {
                imgStep1.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep1.setTextColor(ContextCompat.getColor(this, R.color.green));
            }

            if (normalizedOrderStatus.equals("PROCESS") && normalizedShippingStatus.equals("PENDING")) {
                imgStep1.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep1.setTextColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep2.setTextColor(ContextCompat.getColor(this, R.color.green));
            }

            if (normalizedOrderStatus.equals("PROCESS") && normalizedShippingStatus.equals("SHIPPING")) {
                imgStep1.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep1.setTextColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep2.setTextColor(ContextCompat.getColor(this, R.color.green));
                imgStep3.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep3.setTextColor(ContextCompat.getColor(this, R.color.green));
            }

            if (normalizedOrderStatus.equals("DONE") && normalizedShippingStatus.equals("DONE")) {
                imgStep1.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep1.setTextColor(ContextCompat.getColor(this, R.color.green));
                imgStep2.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep2.setTextColor(ContextCompat.getColor(this, R.color.green));
                imgStep3.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep3.setTextColor(ContextCompat.getColor(this, R.color.green));
                imgStep4.setColorFilter(ContextCompat.getColor(this, R.color.green));
                txtStep4.setTextColor(ContextCompat.getColor(this, R.color.green));
            }
        }
    }

    private void callSupport() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + supportPhone));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        } else {
            startActivity(intent);
        }
    }

    private void showOrderInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_infor, null);
        builder.setView(dialogView);
        TextView tvShippingPolicy = dialogView.findViewById(R.id.tvShippingPolicy);
        TextView tvCancelPolicy = dialogView.findViewById(R.id.tvCancelPolicy);
        TextView tvCannotCancelPolicy = dialogView.findViewById(R.id.tvCannotCancelPolicy);
        TextView tvRefundPolicy = dialogView.findViewById(R.id.tvRefundPolicy);
        TextView tvSupportIntro = dialogView.findViewById(R.id.tvSupportIntro);
        TextView tvImportantNotes = dialogView.findViewById(R.id.tvImportantNotes);

        tvShippingPolicy.setText(Html.fromHtml(getString(R.string.shipping_policy), Html.FROM_HTML_MODE_LEGACY));
        tvCancelPolicy.setText(Html.fromHtml(getString(R.string.cancel_policy), Html.FROM_HTML_MODE_LEGACY));
        tvCannotCancelPolicy.setText(Html.fromHtml(getString(R.string.cannot_cancel_policy), Html.FROM_HTML_MODE_LEGACY));
        tvRefundPolicy.setText(Html.fromHtml(getString(R.string.refund_policy), Html.FROM_HTML_MODE_LEGACY));
        tvSupportIntro.setText(Html.fromHtml(getString(R.string.contact_support_intro), Html.FROM_HTML_MODE_LEGACY));
        tvImportantNotes.setText(Html.fromHtml(getString(R.string.important_notes), Html.FROM_HTML_MODE_LEGACY));

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialButton btnCallSupportDialog = dialogView.findViewById(R.id.btnCallSupportDialog);
        MaterialButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        btnCallSupportDialog.setOnClickListener(v -> {
            callSupport();
            dialog.dismiss();
        });

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_order_info) {
            showOrderInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callSupport();
            } else {
                Toast.makeText(this, "Cần cấp quyền gọi điện để liên hệ hỗ trợ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}