package com.example.aqualife.ui.checkout.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aqualife.LoginActivity;
import com.example.aqualife.R;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrder;
import com.example.aqualife.model.PaymentRequest;
import com.example.aqualife.model.PaymentResponse;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.OrderAPI;
import com.example.aqualife.services.PaymentAPI;
import com.example.aqualife.services.ProductAPI;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity {
    private int orderId;
    private CartResponse cart;
    private List<CartItemResponse> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        orderId = getIntent().getIntExtra("orderId", -1);
        cart = (CartResponse) getIntent().getSerializableExtra("cart");

        if (orderId == -1 || cart == null) {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        for (CartItemResponse item: cart.getCartItems()
        ) {
            fetchProductDataById(item);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void startZaloPay(int orderId, Double amount) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ZaloPaySDK.init(2553, Environment.SANDBOX);
        CreateOrder orderApi = new CreateOrder();

        try {
            int amountValue = amount.intValue();
            JSONObject data = orderApi.createOrder(String.valueOf(amountValue), items);
            String code = data.getString("return_code");

            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");

                ZaloPaySDK.getInstance().payOrder(PaymentActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                        PaymentRequest paymentRequest = new PaymentRequest();
                        paymentRequest.setOrderId(orderId);
                        paymentRequest.setAmount(amount);
                        paymentRequest.setTransactionId(transactionId);
                        paymentRequest.setPaymentStatus("SUCCESS");
                        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        paymentRequest.setPaymentDate(iso8601Format.format(new Date()));

                        PaymentAPI paymentAPI = ApiClient.getAuthenticatedClient(PaymentActivity.this)
                                .create(PaymentAPI.class);
                        paymentAPI.createPayment(paymentRequest).enqueue(new Callback<Response<PaymentResponse>>() {
                            @Override
                            public void onResponse(Call<Response<PaymentResponse>> call, retrofit2.Response<Response<PaymentResponse>> response) {
                                if (response.isSuccessful()) {
                                    new AlertDialog.Builder(PaymentActivity.this)
                                            .setTitle("Thanh toán thành công")
                                            .setMessage("Giao dịch #" + transactionId)
                                            .setPositiveButton("OK", (dialog, which) -> {

                                            })
                                            .show();
                                } else {
                                    String mess = response.message();
                                    String mess2 = response.body().getMessage();
                                    Toast.makeText(PaymentActivity.this, "Lưu giao dịch thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Response<PaymentResponse>> call, Throwable t) {
                                Toast.makeText(PaymentActivity.this, "Lỗi lưu giao dịch: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Toast.makeText(PaymentActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        cancelOrder(orderId);
                        showAlert("Bạn đã huỷ thanh toán.");
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        cancelOrder(orderId);
                        showAlert("Lỗi thanh toán: " + zaloPayError.toString());
                    }
                });
            } else {
                String message = data.optString("return_message", "Không thể khởi tạo đơn hàng");
                showAlert("Không thể khởi tạo đơn hàng ZaloPay." + message);
            }

        } catch (Exception e) {
            showAlert("Lỗi khởi tạo thanh toán: " + e.getMessage());
        }
    }

    private void cancelOrder(int orderId) {
        OrderAPI api = ApiClient.getAuthenticatedClient(this).create(OrderAPI.class);
        api.cancelOrder(orderId).enqueue(new Callback<Response<String>>() {
            @Override
            public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                Log.d("Order", "Đã huỷ đơn hàng #" + orderId);
            }

            @Override
            public void onFailure(Call<Response<String>> call, Throwable t) {
                Log.e("Order", "Lỗi huỷ đơn hàng: " + t.getMessage());
            }
        });
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
    private void fetchProductDataById(CartItemResponse item) {
        ProductAPI api = ApiClient.getAuthenticatedClient(this)
                .create(ProductAPI.class);
        api.getProductById(item.getProductId()).enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(Call<Response<Product>> call, retrofit2.Response<Response<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getData();
                    item.setProduct(product);
                    items.add(item);
                    if(items.size() == cart.getCartItems().size()){
                        startZaloPay(orderId, cart.getTotalPrice());
                    }
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(Call<Response<Product>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }
    private void handleErrorResponse(int code) {
        if (code == 401) {
            Toast.makeText(this, "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        } else {
            Toast.makeText(this, "Error: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFailure(Throwable t) {
        if (t instanceof HttpException && ((HttpException) t).code() == 401) {
            Toast.makeText(this, "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        } else {
            Toast.makeText(this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
