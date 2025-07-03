package com.example.aqualife.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aqualife.R;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;
import com.example.aqualife.services.CartAPI;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;

    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textName, textType, textPrice;
        private Button btnOpenAddCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.productImage);
            textName = itemView.findViewById(R.id.productName);
//            textType = itemView.findViewById(R.id.textType);
            textPrice = itemView.findViewById(R.id.productPrice);
            btnOpenAddCart = itemView.findViewById(R.id.btnOpenAddToCart);
        }

        public void bind(Product p) {
            textName.setText(p.getProductName());
            textPrice.setText("₫" + p.getPrice());

            Glide.with(itemView.getContext())
                    .load(p.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageProduct);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.btnOpenAddCart.setOnClickListener(v -> {
            Product selected = productList.get(position);
            BottomSheetDialog dialog = new BottomSheetDialog(holder.itemView.getContext());
            View sheetView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.layout_bottom_sheet_product, null);
            dialog.setContentView(sheetView);

            // Bind data
            ImageView img = sheetView.findViewById(R.id.imgProduct);
            TextView name = sheetView.findViewById(R.id.txtName);
            TextView price = sheetView.findViewById(R.id.txtPrice);
            TextView stock = sheetView.findViewById(R.id.txtStock);
            TextView qty = sheetView.findViewById(R.id.txtQuantity);
            Button plus = sheetView.findViewById(R.id.btnPlus);
            Button minus = sheetView.findViewById(R.id.btnMinus);
            Button addToCart = sheetView.findViewById(R.id.btnAddToCart);

            Glide.with(holder.itemView.getContext()).load(selected.getImageUrl()).into(img);
            name.setText(selected.getProductName());
            price.setText("₫" + selected.getPrice());
            stock.setText("Kho: " + selected.getQuantity());

            final int[] quantity = {1};
            plus.setOnClickListener(vv -> {
                if(quantity[0] < selected.getQuantity()){
                    quantity[0]++;
                    qty.setText(String.valueOf(quantity[0]));
                }
            });
            minus.setOnClickListener(vv -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    qty.setText(String.valueOf(quantity[0]));
                }
            });

            addToCart.setOnClickListener(vv -> {
                // TODO: Handle add to cart logic here
                addToCart(holder.itemView.getContext(), selected.getProductId(), qty, dialog);
            });

            dialog.show();
        });

        holder.bind(productList.get(position));
    }
    private void addToCart(Context context, int productId, TextView qty, BottomSheetDialog dialog) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://103.245.236.207:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartAPI api = retrofit.create(CartAPI.class);
        String to = context.getString(R.string.token);
        String token = "Bearer " + to;
        int quantity = Integer.parseInt(qty.getText().toString());
        Call<Response<CartResponse>> call = api.addToCart(token, new CreateOrUpdateCartRequest(productId, quantity));

        call.enqueue(new Callback<Response<CartResponse>>() {
            @Override
            public void onResponse(Call<com.example.aqualife.model.Response<CartResponse>> call, retrofit2.Response<Response<CartResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    CartResponse cartResponse = response.body().getData();

                    Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }else{
                    try {
                        // Đọc lỗi từ errorBody
                        String errorBody = response.errorBody().string();
                        JSONObject json = new JSONObject(errorBody);
                        String message = json.optString("message", "Lỗi không xác định");
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Lỗi không đọc được phản hồi", Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<com.example.aqualife.model.Response<CartResponse>> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }
    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }
}
