package com.example.aqualife.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.aqualife.LoginActivity;
import com.example.aqualife.R;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.ProductAPI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductDetailFragment extends Fragment {
    private ImageView imgProduct;
    private TextView txtProductName, txtPrice, txtDescription;
    private TextView txtFishType, txtColor, txtTemperature, txtPh, txtFishFood, txtBehavior, txtQuantity, txtTypeTitle, txtSize;
    private Chip chipStock;
    private ExtendedFloatingActionButton fabAddToCart;
    private LinearLayout llColor, llTemp, llPh, llFood, llBehavior, llType, llSize;
    private MaterialToolbar tbBack;
    private int productId;
    private Product currentProduct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_product_detail, container, false);

        productId = requireArguments().getInt("productId");

        imgProduct = root.findViewById(R.id.imgProduct);
        txtProductName = root.findViewById(R.id.txtProductName);
        txtPrice = root.findViewById(R.id.txtPrice);
        txtDescription = root.findViewById(R.id.txtDescription);
        txtFishType = root.findViewById(R.id.txtType);
        txtColor = root.findViewById(R.id.txtColor);
        txtTemperature = root.findViewById(R.id.txtTemperature);
        txtPh = root.findViewById(R.id.txtPh);
        txtFishFood = root.findViewById(R.id.txtFishFood);
        txtBehavior = root.findViewById(R.id.txtBehavior);
        txtQuantity = root.findViewById(R.id.txtQuantity);
        chipStock = root.findViewById(R.id.chipStock);
        fabAddToCart = root.findViewById(R.id.fabAddToCart);
        txtTypeTitle = root.findViewById(R.id.txtTypeTitle);
        llColor = root.findViewById(R.id.llColor);
        llFood = root.findViewById(R.id.llFood);
        llPh = root.findViewById(R.id.llPh);
        llColor = root.findViewById(R.id.llColor);
        llBehavior = root.findViewById(R.id.llBehavior);
        llType = root.findViewById(R.id.llType);
        llTemp = root.findViewById(R.id.llTemp);
        txtSize = root.findViewById(R.id.txtSize);
        llSize = root.findViewById(R.id.llSize);
        llSize.setVisibility(View.GONE);
        tbBack = root.findViewById(R.id.toolbar);
        tbBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });
        fabAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                showAddToCartBottomSheet(currentProduct);
            } else {
                Toast.makeText(getContext(), "Sản phẩm chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            }
        });

        fetchProductDetail(productId);
        return root;
    }

    private void fetchProductDetail(int id) {
        ProductAPI api = ApiClient.getClient().create(ProductAPI.class);
        api.getProductById(id).enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(Call<Response<Product>> call, retrofit2.Response<Response<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body().getData();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<Response<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        txtProductName.setText(currentProduct.getProductName());
        txtPrice.setText("₫" + currentProduct.getPrice());
        txtDescription.setText(currentProduct.getDescription());
        txtQuantity.setText(String.valueOf(currentProduct.getQuantity()));
        if(currentProduct.getProductType().equalsIgnoreCase("Fish")){
            txtTypeTitle.setText("Loại Cá");
            txtFishType.setText(currentProduct.getFishType());
            txtPh.setText(currentProduct.getPh());
            txtColor.setText(currentProduct.getColor());
            txtFishFood.setText(currentProduct.getFishFood());
            txtBehavior.setText(currentProduct.getBehavior());
            txtTemperature.setText(currentProduct.getTemperature());
        }else if(currentProduct.getProductType().equalsIgnoreCase("Food")){
            txtTypeTitle.setText("Loại Thức Ăn");
            txtFishType.setText(currentProduct.getFoodType());
            txtPh.setText(currentProduct.getPh());
            llBehavior.setVisibility(View.GONE);
            llTemp.setVisibility(View.GONE);
            llFood.setVisibility(View.GONE);
        }else if(currentProduct.getProductType().equalsIgnoreCase("Medicine")){
            txtTypeTitle.setText("Loại Thuốc");
            txtFishType.setText(currentProduct.getMedicineType());
            llBehavior.setVisibility(View.GONE);
            llTemp.setVisibility(View.GONE);
            llPh.setVisibility(View.GONE);
            llColor.setVisibility(View.GONE);
            llFood.setVisibility(View.GONE);
        }else if(currentProduct.getProductType().equalsIgnoreCase("Aquarium")){
            llType.setVisibility(View.GONE);
            llBehavior.setVisibility(View.GONE);
            llTemp.setVisibility(View.GONE);
            llPh.setVisibility(View.GONE);
            llColor.setVisibility(View.GONE);
            llFood.setVisibility(View.GONE);
            llSize.setVisibility(View.VISIBLE);
            txtSize.setText(currentProduct.getSize());
        }

        if (currentProduct.getQuantity() > 0) {
            chipStock.setText("Còn hàng");
            chipStock.setChipBackgroundColorResource(R.color.green);
        } else {
            chipStock.setText("Hết hàng");
            chipStock.setChipBackgroundColorResource(R.color.red);
        }

        Glide.with(this)
                .load(currentProduct.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgProduct);
    }

    private void showAddToCartBottomSheet(Product product) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_product, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        ImageView imgProduct = bottomSheetView.findViewById(R.id.imgProduct);
        TextView txtName = bottomSheetView.findViewById(R.id.txtName);
        TextView txtPrice = bottomSheetView.findViewById(R.id.txtPrice);
        TextView txtStock = bottomSheetView.findViewById(R.id.txtStock);
        TextView txtQuantity = bottomSheetView.findViewById(R.id.txtQuantity);
        TextView txtTotalPrice = bottomSheetView.findViewById(R.id.txtTotalPrice);
        Button btnMinus = bottomSheetView.findViewById(R.id.btnMinus);
        Button btnPlus = bottomSheetView.findViewById(R.id.btnPlus);
        Button btnAddToCart = bottomSheetView.findViewById(R.id.btnAddToCart);

        txtName.setText(product.getProductName());
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        txtPrice.setText("Đơn giá: " + currencyFormat.format(product.getPrice()) + " VNĐ");
        txtStock.setText("Còn: " + product.getQuantity() + " sản phẩm");

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(imgProduct);
        }

        final int[] quantity = {1};
        txtQuantity.setText(String.valueOf(quantity[0]));
        txtTotalPrice.setText("Thành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
                txtTotalPrice.setText("Thành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
            } else {
                Toast.makeText(requireContext(), "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity[0] < product.getQuantity()) {
                quantity[0]++;
                txtQuantity.setText(String.valueOf(quantity[0]));
                txtTotalPrice.setText("Thành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
            } else {
                Toast.makeText(requireContext(), "Đã đạt số lượng tối đa", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            addToCart(product.getProductId(), quantity[0]);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void addToCart(int productId, int quantity) {
        CartAPI api = ApiClient.getAuthenticatedClient(requireContext()).create(CartAPI.class);
        CreateOrUpdateCartRequest request = new CreateOrUpdateCartRequest(productId, quantity);

        api.addToCart(request).enqueue(new Callback<Response<CartResponse>>() {
            @Override
            public void onResponse(Call<Response<CartResponse>> call, retrofit2.Response<Response<CartResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(requireContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                } else {
                    Toast.makeText(requireContext(), "Có lỗi xảy ra: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<CartResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

