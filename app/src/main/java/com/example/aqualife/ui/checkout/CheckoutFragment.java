package com.example.aqualife.ui.checkout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aqualife.LoginActivity;
import com.example.aqualife.R;
import com.example.aqualife.adapter.CheckoutAdapter;
import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.AddressResponse;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrder;
import com.example.aqualife.model.OrderRequest;
import com.example.aqualife.model.OrderResponse;
import com.example.aqualife.model.PaymentRequest;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.AddressAPI;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.OrderAPI;
import com.example.aqualife.services.PaymentAPI;
import com.example.aqualife.services.ProductAPI;
import com.example.aqualife.ui.cart.CartFragment;
import com.example.aqualife.ui.checkout.activity.PaymentActivity;
import com.example.aqualife.ui.checkout.activity.SelectAddressActivity;
import com.example.aqualife.ui.order.OrderFragment;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CheckoutFragment extends Fragment {
    private TextView txtUserName, txtUserAddress, txtEmpty;
    private TextView txtProductTotal, txtTotalAmount, txtTotalBottom;
    private Button btnPlaceOrder;
    private ImageView tvChangeAddress;
    private AccountInfor account;
    private AddressResponse defaultAddress;
    private ActivityResultLauncher<Intent> selectAddressLauncher;
    private ActivityResultLauncher<Intent> payLauncher;
    private CartResponse cart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_checkout, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("Thanh toán");
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
        txtUserName = root.findViewById(R.id.txtReceiverName);
        txtUserAddress = root.findViewById(R.id.txtReceiverAddress);
        txtEmpty = root.findViewById(R.id.tvEmpty);

        txtProductTotal = root.findViewById(R.id.txtGoodsTotal);
//        txtShippingFee = root.findViewById(R.id.txtShippingFee);
        txtTotalAmount = root.findViewById(R.id.txtTotalAmount);
        txtTotalBottom = root.findViewById(R.id.txtTotalPrice);

        btnPlaceOrder = root.findViewById(R.id.btnPlaceOrder);

        tvChangeAddress = root.findViewById(R.id.tvChangeAddress);

        cart = (CartResponse) getArguments().getSerializable("cart");
        account = (AccountInfor) getArguments().getSerializable("account");
        RecyclerView rvCheckoutItems = root.findViewById(R.id.rvCheckoutItems);
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(getContext()));
        CheckoutAdapter adapter = new CheckoutAdapter(cart.getCartItems());
        rvCheckoutItems.setAdapter(adapter);
        txtProductTotal.setText("Tổng tiền hàng: " + cart.getTotalPrice() + "VND");
        txtTotalAmount.setText("Tổng thanh toán: " + cart.getTotalPrice() + "VND");
        txtTotalBottom.setText("Tổng cộng " + cart.getTotalPrice() + "VND");
        btnPlaceOrder.setOnClickListener(v -> {
            createOrder();
        });
        selectAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            AddressResponse selectedAddress = (AddressResponse) data.getSerializableExtra("selectedAddress");

                            if (selectedAddress != null) {
                                defaultAddress = selectedAddress;
                                txtUserName.setText(account.getFullName() + " | " + selectedAddress.getShippingPhone());
                                txtUserAddress.setText(selectedAddress.getShippingAddress());
                            }
                        }
                    }
                });
        payLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("account", account);
                    NavController navController = NavHostFragment.findNavController(CheckoutFragment.this);
                    navController.navigate(R.id.navigation_order, bundle);
                });

        fetchShippingAddress();
        return root;
    }

    private void fetchShippingAddress(){
        AddressAPI addressAPI = ApiClient.getAuthenticatedClient(requireContext())
                .create(AddressAPI.class);

        // Set the click listener outside the callback to ensure it's always set
        tvChangeAddress.setOnClickListener(v -> {
            try{
                Intent intent = new Intent(requireContext(), SelectAddressActivity.class);
                intent.putExtra("account", account);
                selectAddressLauncher.launch(intent);
            }catch (Exception ex){
                Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        addressAPI.getAddress(account.getAccountId()).enqueue(new Callback<Response<List<AddressResponse>>>() {
            @Override
            public void onResponse(Call<Response<List<AddressResponse>>> call, retrofit2.Response<Response<List<AddressResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AddressResponse> addressList = response.body().getData();

                    if (addressList.isEmpty()) {
                        txtUserName.setText("Chưa có địa chỉ giao hàng");
                        txtUserAddress.setText("");
                        return;
                    }

                    for (AddressResponse addr : addressList) {
                        if (Boolean.TRUE.equals(addr.isDefault())) {
                            defaultAddress = addr;
                            break;
                        }
                    }

                    if (defaultAddress == null && !addressList.isEmpty()) {
                        defaultAddress = addressList.get(0);
                    }

                    if (defaultAddress != null) {
                        txtUserName.setText(account.getFullName() + " | " + defaultAddress.getShippingPhone());
                        txtUserAddress.setText(defaultAddress.getShippingAddress());
                    }
                } else {
                    Toast.makeText(getContext(), "Không lấy được địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<List<AddressResponse>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createOrder() {
        if (defaultAddress == null) {
            Toast.makeText(getContext(), "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderRequest request = new OrderRequest();
        request.setAccountAddId(defaultAddress.getAccountAddId());
        request.setCartId(cart.getCartId());

        OrderAPI orderAPI = ApiClient.getAuthenticatedClient(requireContext())
                .create(OrderAPI.class);

        orderAPI.createOrder(request).enqueue(new Callback<Response<OrderResponse>>() {
            @Override
            public void onResponse(Call<Response<OrderResponse>> call, retrofit2.Response<Response<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), PaymentActivity.class);
                    intent.putExtra("orderId", response.body().getData().getOrderId());
                    intent.putExtra("cart", cart);
                    payLauncher.launch(intent);
                } else {
                    Toast.makeText(getContext(), "Không thể tạo đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<OrderResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
