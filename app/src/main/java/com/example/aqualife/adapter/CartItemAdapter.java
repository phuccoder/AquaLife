package com.example.aqualife.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aqualife.LoginActivity;
import com.example.aqualife.R;
import com.example.aqualife.model.CartItemResponse;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.ProductAPI;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    private List<CartItemResponse> cartItems;
    private Context context;

    public CartItemAdapter(List<CartItemResponse> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_item, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItemResponse item = cartItems.get(position);
        fetchProductDataById(holder, item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtProductName, txtQuantity, txtPrice;
        ImageView imgProduct;
        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
    private void fetchProductDataById(CartItemViewHolder holder, CartItemResponse item) {
        Context context = holder.itemView.getContext();

        ProductAPI api = ApiClient.getAuthenticatedClient(context)
                .create(ProductAPI.class);

        api.getProductById(item.getProductId()).enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(Call<Response<Product>> call, retrofit2.Response<Response<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getData();

                    NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    currencyFormat.setMaximumFractionDigits(0);

                    holder.txtProductName.setText(product.getProductName());
                    holder.txtPrice.setText(currencyFormat.format(product.getPrice()) + " VNĐ");
                    holder.txtQuantity.setText(String.valueOf(item.getQuantity()));

                    Glide.with(context)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.imgProduct);
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

