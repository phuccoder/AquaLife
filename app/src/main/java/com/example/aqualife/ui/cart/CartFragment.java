package com.example.aqualife.ui.cart;

import android.content.Intent;
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

import com.example.aqualife.LoginActivity;
import com.example.aqualife.R;
import com.example.aqualife.adapter.CartAdapter;
import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.CartAPI;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;

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

        recyclerCart = root.findViewById(R.id.recyclerViewCart);
        txtTotal = root.findViewById(R.id.txtTotalCartPrice);
        btnCheckout = root.findViewById(R.id.btnCheckout);

        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter();
        adapter.setOnCartChangedListener(this::fetchCartData);
        recyclerCart.setAdapter(adapter);

        View emptyCartLayout = root.findViewById(R.id.emptyCartLayout);
        Button btnContinueShopping = root.findViewById(R.id.btnContinueShopping);
        if (btnContinueShopping != null) {
            btnContinueShopping.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        fetchAccountData();
        return root;
    }

    private void fetchCartData() {
        CartAPI api = ApiClient.getAuthenticatedClient(requireContext())
                .create(CartAPI.class);

        api.getCartByAccountId(accountId).enqueue(new Callback<Response<CartResponse>>() {
            @Override
            public void onResponse(Call<Response<CartResponse>> call, retrofit2.Response<Response<CartResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartResponse items = response.body().getData();

                    View emptyCartLayout = getView() != null ? getView().findViewById(R.id.emptyCartLayout) : null;
                    View bottomLayout = getView() != null ? getView().findViewById(R.id.bottomLayout) : null;

                    if (items != null && items.getCartItems() != null && !items.getCartItems().isEmpty()) {
                        if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.GONE);
                        if (bottomLayout != null) bottomLayout.setVisibility(View.VISIBLE);
                        if (recyclerCart != null) recyclerCart.setVisibility(View.VISIBLE);

                        txtTotal.setText("₫" + items.getTotalPrice());

                        int itemCount = 0;
                        for (CartItemResponse item : items.getCartItems()) {
                            itemCount += item.getQuantity();
                        }

                        btnCheckout.setVisibility(View.VISIBLE);
                        btnCheckout.setText("Tiến hành đặt hàng (" + itemCount + ")");
                        adapter.setCartItems(items);
                    } else {
                        if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.VISIBLE);
                        if (bottomLayout != null) bottomLayout.setVisibility(View.GONE);
                        if (recyclerCart != null) recyclerCart.setVisibility(View.GONE);
                        adapter.setCartItems(null);
                    }
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(Call<Response<CartResponse>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }

    private void fetchAccountData() {
        CartAPI api = ApiClient.getAuthenticatedClient(requireContext())
                .create(CartAPI.class);

        api.getAccountId().enqueue(new Callback<Response<AccountInfor>>() {
            @Override
            public void onResponse(Call<Response<AccountInfor>> call, retrofit2.Response<Response<AccountInfor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountId = response.body().getData().getAccountId();
                    fetchCartData();
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(Call<Response<AccountInfor>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }

    private void deleteCart(int cartId) {
        CartAPI api = ApiClient.getAuthenticatedClient(requireContext())
                .create(CartAPI.class);

        api.deleteCartById(cartId).enqueue(new Callback<Response<Object>>() {
            @Override
            public void onResponse(Call<Response<Object>> call, retrofit2.Response<Response<Object>> response) {
                if (response.isSuccessful()) {
                    btnCheckout.setText("Mua hàng (0)");
                    txtTotal.setText("Tổng cộng ₫0");
                    btnCheckout.setVisibility(View.GONE);
                    btnDeleteCart.setVisibility(View.GONE);
                    adapter.setCartItems(null);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject json = new JSONObject(errorBody);
                        String message = json.optString("message", "Lỗi không xác định");
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi không đọc được phản hồi", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<Object>> call, Throwable t) {
                handleFailure(t);
            }
        });
    }

    private void handleErrorResponse(int code) {
        if (code == 401) {
            Toast.makeText(getContext(), "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        } else {
            Toast.makeText(getContext(), "Error: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFailure(Throwable t) {
        if (t instanceof HttpException && ((HttpException) t).code() == 401) {
            Toast.makeText(getContext(), "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        } else {
            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}