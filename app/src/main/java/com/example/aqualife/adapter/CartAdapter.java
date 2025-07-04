package com.example.aqualife.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aqualife.LoginActivity;
import com.example.aqualife.R;
import com.example.aqualife.listener.OnCartChangedListener;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.ProductAPI;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private CartResponse cartItems;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<Integer, Runnable> updateTasks = new HashMap<>();
    private OnCartChangedListener cartChangedListener;

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartChangedListener = listener;
    }

    public void setCartItems(CartResponse items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        if (cartItems != null && cartItems.getCartItems() != null && position < cartItems.getCartItems().size()) {
            CartItemResponse item = cartItems.getCartItems().get(position);
            fetchProductDataById(holder, item);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems != null && cartItems.getCartItems() != null ? cartItems.getCartItems().size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtProductName, txtProductPrice, txtQuantity;
        ImageView imgProduct;
        Button btnPlus, btnMinus;
        ImageButton btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void fetchProductDataById(CartViewHolder holder, CartItemResponse item) {
        Context context = holder.itemView.getContext();

        ProductAPI api = ApiClient.getAuthenticatedClient(context)
                .create(ProductAPI.class);

        api.getProductById(item.getProductId()).enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(Call<Response<Product>> call, retrofit2.Response<Response<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getData();

                    holder.txtProductName.setText(product.getProductName());
                    holder.txtProductPrice.setText("₫" + item.getPrice());
                    holder.txtQuantity.setText(String.valueOf(item.getQuantity()));

                    int[] quantity = { item.getQuantity() };
                    double unitPrice = product.getPrice();

                    Glide.with(context)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.imgProduct);

                    holder.btnPlus.setOnClickListener(v -> {
                        quantity[0]++;
                        holder.txtQuantity.setText(String.valueOf(quantity[0]));
                        holder.txtProductPrice.setText("₫" + (int)(quantity[0] * unitPrice));
                        scheduleUpdate(holder, item, quantity[0]);
                    });

                    holder.btnMinus.setOnClickListener(v -> {
                        if (quantity[0] > 1) {
                            quantity[0]--;
                            holder.txtQuantity.setText(String.valueOf(quantity[0]));
                            holder.txtProductPrice.setText("₫" + (int)(quantity[0] * unitPrice));
                            scheduleUpdate(holder, item, quantity[0]);
                        }
                    });

                    holder.btnDelete.setOnClickListener(v -> {
                        updateCart(holder, item, 0);
                    });
                } else {
                    handleErrorResponse(context, response.code());
                }
            }

            @Override
            public void onFailure(Call<Response<Product>> call, Throwable t) {
                handleFailure(context, t);
            }
        });
    }

    private void scheduleUpdate(CartViewHolder holder, CartItemResponse item, int newQuantity) {
        int itemId = item.getCartItemId();

        if (updateTasks.containsKey(itemId)) {
            handler.removeCallbacks(updateTasks.get(itemId));
        }

        Runnable runnable = () -> {
            updateCart(holder, item, newQuantity);
            updateTasks.remove(itemId);
        };

        updateTasks.put(itemId, runnable);
        handler.postDelayed(runnable, 800);
    }

    private void updateCart(CartViewHolder holder, CartItemResponse item, int newQuantity) {
        Context context = holder.itemView.getContext();
        CreateOrUpdateCartRequest request = new CreateOrUpdateCartRequest(item.getProductId(), newQuantity);

        CartAPI api = ApiClient.getAuthenticatedClient(context)
                .create(CartAPI.class);

        api.updateCart(cartItems.getCartId(), request).enqueue(new Callback<Response<CartResponse>>() {
            @Override
            public void onResponse(Call<Response<CartResponse>> call, retrofit2.Response<Response<CartResponse>> response) {
                if (response.isSuccessful()) {
                    if (cartChangedListener != null) {
                        cartChangedListener.onCartUpdated();
                    }
                } else {
                    handleErrorResponse(context, response.code());
                }
            }

            @Override
            public void onFailure(Call<Response<CartResponse>> call, Throwable t) {
                handleFailure(context, t);
            }
        });
    }

    private void handleErrorResponse(Context context, int code) {
        if (code == 401) {
            // Unauthorized - redirect to login
            Toast.makeText(context, "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(loginIntent);
        } else {
            Toast.makeText(context, "Error: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFailure(Context context, Throwable t) {
        if (t instanceof HttpException && ((HttpException) t).code() == 401) {
            // Unauthorized - redirect to login
            Toast.makeText(context, "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(loginIntent);
        } else {
            Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}