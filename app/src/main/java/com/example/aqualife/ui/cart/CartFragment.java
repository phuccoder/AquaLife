package com.example.aqualife.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.adapter.CartAdapter;
import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Response;
import com.example.aqualife.services.CartAPI;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartFragment extends Fragment {

    private RecyclerView recyclerCart;
    private CartAdapter adapter;
    private int accountId;
    private TextView txtTotal;
    private Button btnCheckout;
    private ImageButton btnDeleteCart;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_all_shopping_cart, container, false);

        recyclerCart = root.findViewById(R.id.recyclerCart);
        txtTotal = root.findViewById(R.id.txtTotal);
        btnCheckout = root.findViewById(R.id.btnCheckout);
        btnCheckout.setVisibility(View.GONE);
        btnDeleteCart = root.findViewById(R.id.btnDeleteCart);
        btnDeleteCart.setVisibility(View.GONE);
        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter();
        adapter.setOnCartChangedListener(this::fetchCartData);
        recyclerCart.setAdapter(adapter);
        fetchAccountData();
        return root;
    }

    private void fetchCartData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://103.245.236.207:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartAPI api = retrofit.create(CartAPI.class);
        String token = "Bearer " + getString(R.string.token); // Cập nhật nếu dùng SharedPreferences

        api.getCartByAccountId(token, accountId).enqueue(new Callback<Response<CartResponse>>() {
            @Override
            public void onResponse(Call<Response<CartResponse>> call, retrofit2.Response<Response<CartResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartResponse items = response.body().getData();
                    txtTotal.setText("Tổng cộng ₫" + items.getTotalPrice());
                    int itemCount = 0;
                    for (CartItemResponse item : items.getCartItems()) {
                        itemCount += item.getQuantity();
                    }
                    btnCheckout.setVisibility(View.VISIBLE);
                    btnCheckout.setText("Mua hàng (" + itemCount + ")");
                    btnDeleteCart.setVisibility(View.VISIBLE);
                    btnDeleteCart.setOnClickListener(v -> {
                        deleteCart(items.getCartId());
                    });
                    adapter.setCartItems(items);
                } else {
                    Toast.makeText(getContext(), "Lỗi lấy giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<CartResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchAccountData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://103.245.236.207:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartAPI api = retrofit.create(CartAPI.class);
        String token = "Bearer " + getString(R.string.token); // Cập nhật nếu dùng SharedPreferences

        api.getAccountId(token).enqueue(new Callback<Response<AccountInfor>>() {
            @Override
            public void onResponse(Call<Response<AccountInfor>> call, retrofit2.Response<Response<AccountInfor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountId = response.body().getData().getAccountId();
                    fetchCartData();
                } else {
                    Toast.makeText(getContext(), "Lỗi lấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<AccountInfor>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteCart(int cartId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://103.245.236.207:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        CartAPI api = retrofit.create(CartAPI.class);
        String token = "Bearer " + getString(R.string.token);
        api.deleteCartById(token, cartId).enqueue(new Callback<Response<Object>>() {
            @Override
            public void onResponse(Call<Response<Object>> call, retrofit2.Response<Response<Object>> response) {
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject json = new JSONObject(errorBody);
                        String message = json.optString("message", "Lỗi không xác định");
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi không đọc được phản hồi", Toast.LENGTH_LONG).show();
                    }
                }else{
                    btnCheckout.setText("Mua hàng (" + 0 + ")");
                    txtTotal.setText("Tổng cộng ₫" + 0);
                    btnCheckout.setVisibility(View.GONE);
                    btnDeleteCart.setVisibility(View.GONE);
                    adapter.setCartItems(null);
                }
            }

            @Override
            public void onFailure(Call<Response<Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi không xóa được giỏ hàng", Toast.LENGTH_LONG).show();
            }
        });
    }
}