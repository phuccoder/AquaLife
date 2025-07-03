package com.example.aqualife.ui.product;

import android.app.AlertDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.adapter.ProductAdapter;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.ProductListData;
import com.example.aqualife.services.ProductAPI;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private TextView tvEmpty;
    private EditText edtKeyword;
    private EditText edtMin;
    private EditText edtMax;
    private ImageView btnSearch;
    private ImageView btnBack;
    private ImageView btnFilter;

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

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter, null);
        edtMin = view.findViewById(R.id.edtMinPrice);
        edtMax = view.findViewById(R.id.edtMaxPrice);

        btnSearch = root.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            String keyword = edtKeyword.getText().toString().trim();
            String min = edtMin.getText().toString().trim();
            String max = edtMax.getText().toString().trim();
            String sortBy = "";

            Integer minPrice = min.isEmpty() ? null : Integer.parseInt(min);
            Integer maxPrice = max.isEmpty() ? null : Integer.parseInt(max);

            fetchProducts(productType, keyword, minPrice, maxPrice, sortBy);
        });
        recyclerView = root.findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        int spanCount = 3; // Số cột
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view); // item position
                int column = position % spanCount; // item column

                outRect.left = 16 - column * 16 / spanCount;
                outRect.right = (column + 1) * 16 / spanCount;

                if (position < spanCount) { // top edge
                    outRect.top = 16;
                }
                outRect.bottom = 16; // item bottom
            }
        });
        adapter = new ProductAdapter();
        recyclerView.setAdapter(adapter);

        fetchProducts(productType, null, null, null, null);

        return root;
    }

    private void fetchProducts(String type, String key, Integer min, Integer max, String sortBy) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://103.245.236.207:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProductAPI api = retrofit.create(ProductAPI.class);
        String to = getString(R.string.token);
        String token = "Bearer " + to;
        Call<com.example.aqualife.model.Response<ProductListData>> call = api.getProducts(token, key, type, min, max, sortBy);

        call.enqueue(new Callback<com.example.aqualife.model.Response<ProductListData>>() {
            @Override
            public void onResponse(Call<com.example.aqualife.model.Response<ProductListData>> call, Response<com.example.aqualife.model.Response<ProductListData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> products = response.body().getData().getProducts();

                    if (products == null || products.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        adapter.setProducts(products);
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setText("Không thể tải dữ liệu.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<com.example.aqualife.model.Response<ProductListData>> call, Throwable t) {
                tvEmpty.setText("Lỗi khi gọi API: " + t.getMessage());
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
    private void showFilterDialog(String productType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter, null);
        EditText edtMin = view.findViewById(R.id.edtMinPrice);
        EditText edtMax = view.findViewById(R.id.edtMaxPrice);
        Button btnApply = view.findViewById(R.id.btnApplyFilter);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnApply.setOnClickListener(v -> {
            String keyword = edtKeyword.getText().toString().trim();
            Integer minPrice = edtMin.getText().toString().isEmpty() ? null : Integer.parseInt(edtMin.getText().toString());
            Integer maxPrice = edtMax.getText().toString().isEmpty() ? null : Integer.parseInt(edtMax.getText().toString());

            fetchProducts(productType, keyword, minPrice, maxPrice, null);
            dialog.dismiss();
        });

        dialog.show();
    }

}
