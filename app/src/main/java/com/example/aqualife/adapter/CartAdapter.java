package com.example.aqualife.adapter;

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
import com.example.aqualife.R;
import com.example.aqualife.listener.OnCartChangedListener;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.ProductAPI;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(com.example.aqualife.R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemResponse item = cartItems.getCartItems().get(position);
        fetchProductDataById(holder, item);
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.getCartItems().size() : 0;
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
    private void fetchProductDataById(CartViewHolder holder, CartItemResponse item){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://103.245.236.207:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProductAPI api = retrofit.create(ProductAPI.class);
        String token = "Bearer " + holder.itemView.getContext().getString(R.string.token); // Cập nhật nếu dùng SharedPreferences

        api.getProductById(token, item.getProductId()).enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(Call<Response<Product>> call, retrofit2.Response<Response<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    holder.txtProductName.setText(response.body().getData().getProductName());
                    holder.txtProductPrice.setText(String.valueOf(item.getPrice()));
                    holder.txtQuantity.setText(String.valueOf(item.getQuantity()));
                    int[] quantity = { item.getQuantity() };
                    double unitPrice = response.body().getData().getPrice();
                    Glide.with(holder.itemView.getContext())
                            .load(response.body().getData().getImageUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.imgProduct);
                    holder.btnPlus.setOnClickListener(v -> {
                        quantity[0]++;
                        holder.txtQuantity.setText(String.valueOf(quantity[0]));
                        holder.txtProductPrice.setText("₫" + (int)(quantity[0] * unitPrice));
                        scheduleUpdate(holder, item, quantity[0], retrofit, token);
                    });
                    holder.btnMinus.setOnClickListener(v -> {
                        if (quantity[0] > 1) {
                            quantity[0]--;
                            holder.txtQuantity.setText(String.valueOf(quantity[0]));
                            holder.txtProductPrice.setText("₫" + (int)(quantity[0] * unitPrice));
                            scheduleUpdate(holder, item, quantity[0], retrofit, token);
                        }
                    });
                    holder.btnDelete.setOnClickListener(v -> {
                        updateCart(holder, item, 0, retrofit, token);
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Lỗi lấy sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Product>> call, Throwable t) {
                Toast.makeText(holder.itemView.getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void scheduleUpdate(CartViewHolder holder, CartItemResponse item, int newQuantity, Retrofit retrofit, String yourToken) {
        int itemId = item.getCartItemId();

        if (updateTasks.containsKey(itemId)) {
            handler.removeCallbacks(updateTasks.get(itemId));
        }

        Runnable runnable = () -> {
            updateCart(holder, item, newQuantity, retrofit, yourToken);
            updateTasks.remove(itemId);
        };
        updateTasks.put(itemId, runnable);
        handler.postDelayed(runnable, 800);
    }
    private void updateCart(CartViewHolder holder, CartItemResponse item, int newQuantity, Retrofit retrofit, String yourToken){
        CreateOrUpdateCartRequest request = new CreateOrUpdateCartRequest(item.getProductId(), newQuantity);
        CartAPI api = retrofit.create(CartAPI.class);

        api.updateCart(yourToken, cartItems.getCartId(), request).enqueue(new Callback<Response<CartResponse>>() {
            @Override
            public void onResponse(Call<Response<CartResponse>> call, retrofit2.Response<Response<CartResponse>> response) {
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject json = new JSONObject(errorBody);
                        String message = json.optString("message", "Lỗi không xác định");
                        Toast.makeText(holder.itemView.getContext(), message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(holder.itemView.getContext(), "Lỗi không đọc được phản hồi", Toast.LENGTH_LONG).show();
                    }
                }else{
                    if (cartChangedListener != null) {
                        cartChangedListener.onCartUpdated();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<CartResponse>> call, Throwable t) {
                Toast.makeText(holder.itemView.getContext(), "Lỗi không xóa được sản phẩm", Toast.LENGTH_LONG).show();
            }
        });
    }
}

