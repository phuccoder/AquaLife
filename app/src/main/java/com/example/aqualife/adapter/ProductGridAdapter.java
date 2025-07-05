package com.example.aqualife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aqualife.R;
import com.example.aqualife.listener.OnProductClickListener;
import com.example.aqualife.model.Product;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnItemClickListener listener;
    private OnProductClickListener productClickListener;
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setProductClickListener(OnProductClickListener productClickListener) {
        this.productClickListener = productClickListener;
    }

    public void setProducts(List<Product> products) {
        if(this.products.isEmpty()){
            this.products = products;
        }else{
            this.products.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_product_grid_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
        holder.itemView.setOnClickListener(v -> {
            if(productClickListener != null){
                productClickListener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final MaterialButton btnOpenAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            btnOpenAddToCart = itemView.findViewById(R.id.btnOpenAddToCart);
        }

        public void bind(Product product) {
            productName.setText(product.getProductName());

            NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            productPrice.setText(currencyFormat.format(product.getPrice()) + " VNĐ");

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(productImage);
            }

            btnOpenAddToCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(product);
                }
            });
        }
    }
}