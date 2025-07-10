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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private final List<CartItemResponse> cartItems;

    public CheckoutAdapter(List<CartItemResponse> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_product, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItemResponse item = cartItems.get(position);
        fetchProductDataById(holder, item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductVariant, txtProductPrice, txtProductOriginalPrice, txtProductQuantity;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductQuantity = itemView.findViewById(R.id.txtProductQuantity);
        }
    }
    private void fetchProductDataById(CheckoutViewHolder holder, CartItemResponse item) {
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
                    holder.txtProductQuantity.setText(String.valueOf(item.getQuantity()));

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
