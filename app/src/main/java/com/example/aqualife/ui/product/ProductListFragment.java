package com.example.aqualife.ui.product;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aqualife.LoginActivity;
import com.example.aqualife.R;
import com.example.aqualife.adapter.ProductAdapter;
import com.example.aqualife.adapter.ProductGridAdapter;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.ProductListData;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.ProductAPI;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class ProductListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProductGridAdapter adapter;
    private TextView tvEmpty;
    private EditText edtKeyword;
    private ImageView btnSearch;
    private ImageView btnBack;
    private ImageView btnFilter;
    private Integer minPrice = null;
    private Integer maxPrice = null;
    private String currentSortBy = null;
    private ProgressBar progressBar;
    private String selectedProductType = null;
    private int pageNumber = 0;
    private final int pageSize = 10;
    private int productSize = 0;
    private boolean isLastPage = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_view_all_fish, container, false);
        String productType = requireArguments().getString("productType");
        tvEmpty = root.findViewById(R.id.tvEmpty);
        edtKeyword = root.findViewById(R.id.edtSearch);

        btnBack = root.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        btnFilter = root.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> showFilterDialog(productType));

        btnSearch = root.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            String keyword = edtKeyword.getText().toString().trim();
            fetchProducts(selectedProductType != null ? selectedProductType : productType,
                    keyword, minPrice, maxPrice, currentSortBy);
        });

        recyclerView = root.findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int column = position % 2;
                outRect.left = column == 0 ? 0 : spacingInPixels / 2;
                outRect.right = column == 0 ? spacingInPixels / 2 : 0;
                outRect.bottom = spacingInPixels;
                if (position < 2) {
                    outRect.top = spacingInPixels;
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (productSize != 0 && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == productSize - 1 && !isLastPage) {
                    pageNumber += 1;
                    String keyword = edtKeyword.getText().toString().trim();
                    fetchProducts(selectedProductType != null ? selectedProductType : productType,
                            keyword, minPrice, maxPrice, currentSortBy);
                }
            }
        });
        adapter = new ProductGridAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(product -> showAddToCartBottomSheet(product));
        adapter.setProductClickListener(product -> {
            Bundle bundle = new Bundle();
            bundle.putInt("productId", product.getProductId());
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.navigation_productList_to_productDetail, bundle);
        });
        progressBar = new ProgressBar(requireContext());
        progressBar.setId(View.generateViewId());
        progressBar.setVisibility(View.GONE);

        // Add progress bar to layout
        ConstraintLayout constraintLayout = (ConstraintLayout) root;
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        constraintLayout.addView(progressBar, layoutParams);

        root.post(() -> fetchProducts(productType, null, null, null, null));

        return root;
    }


    private void fetchProducts(String type, String keyword, Integer minPrice, Integer maxPrice, String sortBy) {
        showLoading(true);
        if (isLastPage) return;
        ProductAPI api = ApiClient.getAuthenticatedClient(requireContext())
                .create(ProductAPI.class);

        Call<com.example.aqualife.model.Response<ProductListData>> call =
                api.getProducts(keyword, type, minPrice, maxPrice, sortBy, pageNumber, pageSize);

        call.enqueue(new Callback<com.example.aqualife.model.Response<ProductListData>>() {
            @Override
            public void onResponse(Call<com.example.aqualife.model.Response<ProductListData>> call,
                                   Response<com.example.aqualife.model.Response<ProductListData>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null) {

                    List<Product> products = response.body().getData().getProducts();
                    if (products != null && !products.isEmpty()) {
                        adapter.setProducts(products);
                        recyclerView.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                        productSize += products.size();
                        isLastPage = response.body().getData().getCurrentPage() == (response.body().getData().getTotalPages() - 1);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.aqualife.model.Response<ProductListData>> call, Throwable t) {
                showLoading(false);
                handleFailure(t);
            }
        });
    }

    private void showFilterDialog(String initialProductType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);

        // Find views in filter dialog
        EditText edtMin = view.findViewById(R.id.edtMinPrice);
        EditText edtMax = view.findViewById(R.id.edtMaxPrice);
        RadioGroup rgSortBy = view.findViewById(R.id.rgSortBy);
        Button btnApply = view.findViewById(R.id.btnApplyFilter);
        Button btnReset = view.findViewById(R.id.btnReset);
        Spinner spinnerProductType = view.findViewById(R.id.spinnerProductType);

        // Setup product type spinner with Vietnamese names
        String[] productTypes = new String[]{"Tất cả", "Cá", "Thức ăn", "Thuốc", "Bể cá"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, productTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductType.setAdapter(adapter);

        // Select current product type if it exists
        String selectedType = this.selectedProductType != null ? this.selectedProductType : initialProductType;
        if (selectedType != null) {
            int position = 0;
            if ("Fish".equalsIgnoreCase(selectedType)) {
                position = 1;
            } else if ("Food".equalsIgnoreCase(selectedType)) {
                position = 2;
            } else if ("Medicine".equalsIgnoreCase(selectedType)) {
                position = 3;
            } else if ("Aquarium".equalsIgnoreCase(selectedType)) {
                position = 4;
            }
            spinnerProductType.setSelection(position);
        }

        // Pre-populate fields if values exist
        if (this.minPrice != null) {
            edtMin.setText(String.valueOf(this.minPrice));
        }
        if (this.maxPrice != null) {
            edtMax.setText(String.valueOf(this.maxPrice));
        }

        // Set correct radio button based on current sort
        if (currentSortBy != null) {
            switch (currentSortBy) {
                case "priceAsc":
                    rgSortBy.check(R.id.rbPriceAsc);
                    break;
                case "priceDesc":
                    rgSortBy.check(R.id.rbPriceDesc);
                    break;
                case "name":
                    rgSortBy.check(R.id.rbName);
                    break;
                default:
                    rgSortBy.check(R.id.rbDefault);
                    break;
            }
        }

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnApply.setOnClickListener(v -> {

            String keyword = edtKeyword.getText().toString().trim();

            Integer minPriceValue = null;
            Integer maxPriceValue = null;

            if (!edtMin.getText().toString().isEmpty()) {
                try {
                    minPriceValue = Integer.parseInt(edtMin.getText().toString().trim());
                    this.minPrice = minPriceValue;
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Giá tối thiểu không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                this.minPrice = null;
            }

            if (!edtMax.getText().toString().isEmpty()) {
                try {
                    maxPriceValue = Integer.parseInt(edtMax.getText().toString().trim());
                    this.maxPrice = maxPriceValue;
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Giá tối đa không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                this.maxPrice = null;
            }

            String sortBy = null;
            int selectedId = rgSortBy.getCheckedRadioButtonId();
            if (selectedId == R.id.rbPriceAsc) {
                sortBy = "priceAsc";
            } else if (selectedId == R.id.rbPriceDesc) {
                sortBy = "priceDesc";
            } else if (selectedId == R.id.rbName) {
                sortBy = "name";
            }
            this.currentSortBy = sortBy;

            String apiProductType = null;
            String selectedItem = spinnerProductType.getSelectedItem().toString();

            if ("Cá".equals(selectedItem)) {
                apiProductType = "Fish";
            } else if ("Thức ăn".equals(selectedItem)) {
                apiProductType = "Food";
            } else if ("Thuốc".equals(selectedItem)) {
                apiProductType = "Medicine";
            } else if ("Bể cá".equals(selectedItem)) {
                apiProductType = "Aquarium";
            }

            this.selectedProductType = apiProductType;

            showLoading(true);

            fetchProducts(this.selectedProductType, keyword, this.minPrice, this.maxPrice, this.currentSortBy);
            dialog.dismiss();
        });

        // Reset button click
        btnReset.setOnClickListener(v -> {
            edtMin.setText("");
            edtMax.setText("");
            rgSortBy.check(R.id.rbDefault);
            spinnerProductType.setSelection(0);
            this.minPrice = null;
            this.maxPrice = null;
            this.currentSortBy = null;
            this.selectedProductType = null;
        });

        dialog.show();
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
        Button btnMinus = bottomSheetView.findViewById(R.id.btnMinus);
        Button btnPlus = bottomSheetView.findViewById(R.id.btnPlus);
        Button btnAddToCart = bottomSheetView.findViewById(R.id.btnAddToCart);

        // Set product data
        txtName.setText(product.getProductName());
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        txtPrice.setText(currencyFormat.format(product.getPrice()) + " VNĐ");
        txtStock.setText("Còn: " + product.getQuantity() + " sản phẩm");

        // Load product image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(imgProduct);
        }

        final int[] quantity = {1};
        txtQuantity.setText(String.valueOf(quantity[0]));

        // Create a TextView for total price
        TextView txtTotalPrice = bottomSheetView.findViewById(R.id.txtTotalPrice);
        if (txtTotalPrice == null) {
            txtPrice.setText("Đơn giá: " + currencyFormat.format(product.getPrice()) + " VNĐ");
            txtPrice.append("\nThành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
        }

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
                if (txtTotalPrice != null) {
                    txtTotalPrice.setText("Thành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
                } else {
                    txtPrice.setText("Đơn giá: " + currencyFormat.format(product.getPrice()) + " VNĐ");
                    txtPrice.append("\nThành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
                }
            } else {
                Toast.makeText(requireContext(), "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity[0] < product.getQuantity()) {
                quantity[0]++;
                txtQuantity.setText(String.valueOf(quantity[0]));
                if (txtTotalPrice != null) {
                    txtTotalPrice.setText("Thành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
                } else {
                    txtPrice.setText("Đơn giá: " + currencyFormat.format(product.getPrice()) + " VNĐ");
                    txtPrice.append("\nThành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
                }
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
        CartAPI api = ApiClient.getAuthenticatedClient(requireContext())
                .create(CartAPI.class);

        CreateOrUpdateCartRequest request = new CreateOrUpdateCartRequest(productId, quantity);

        api.addToCart(request).enqueue(new Callback<com.example.aqualife.model.Response<CartResponse>>() {
            @Override
            public void onResponse(Call<com.example.aqualife.model.Response<CartResponse>> call,
                                   Response<com.example.aqualife.model.Response<CartResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(requireContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireContext(), "Có lỗi xảy ra: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.aqualife.model.Response<CartResponse>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void handleErrorResponse(int code) {
        if (code == 401) {
            Toast.makeText(getContext(), "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        } else {
            tvEmpty.setText("Error code: " + code);
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void handleFailure(Throwable t) {
        if (t instanceof HttpException && ((HttpException) t).code() == 401) {
            Toast.makeText(getContext(), "Session expired. Please login again", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
        } else {
            tvEmpty.setText("Error: " + t.getMessage());
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}