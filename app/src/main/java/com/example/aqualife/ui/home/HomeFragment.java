package com.example.aqualife.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.aqualife.LoginActivity;
import com.example.aqualife.MainActivity;
import com.example.aqualife.R;
import com.example.aqualife.adapter.BannerAdapter;
import com.example.aqualife.adapter.ProductAdapter;
import com.example.aqualife.model.CartResponse;
import com.example.aqualife.model.CreateOrUpdateCartRequest;
import com.example.aqualife.model.Product;
import com.example.aqualife.model.ProductListData;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.response.AccountResponse;
import com.example.aqualife.services.AccountService;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.ProductAPI;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;

public class HomeFragment extends Fragment {

    private View root;
    private MaterialCardView searchButton;
    private MaterialCardView searchBarContainer;
    private ImageView menuButton;
    private ImageView closeSearchButton;
    private EditText searchEditText;
    private ScrollView mainScrollView;
    private TextView loginTextView;
    private boolean isLoggedIn = false;

    // Banner slider components
    private ViewPager2 bannerViewPager;
    private TabLayout bannerIndicator;
    private List<Integer> bannerList;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private final long SLIDER_DELAY = 10000;

    // Product sections
    private RecyclerView fishRecyclerView;
    private RecyclerView tankRecyclerView;
    private RecyclerView foodRecyclerView;
    private RecyclerView medicineRecyclerView;

    private ProductAdapter fishAdapter;
    private ProductAdapter tankAdapter;
    private ProductAdapter foodAdapter;
    private ProductAdapter medicineAdapter;

    private Button fishViewMoreBtn;
    private Button tankViewMoreBtn;
    private Button foodViewMoreBtn;
    private Button medicineViewMoreBtn;

    private static final int PRODUCTS_PER_SECTION = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_home, container, false);

        initializeViews();
        setupClickListeners();
        setupSearchFunctionality();
        setupBannerSlider();
        setupProductSections();
        loadProducts();
        checkLoginStatus();

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
        checkLoginStatus();
    }

    private void initializeViews() {
        searchButton = root.findViewById(R.id.searchButton);
        searchBarContainer = root.findViewById(R.id.searchBarContainer);
        menuButton = root.findViewById(R.id.menuButton);
        closeSearchButton = root.findViewById(R.id.closeSearchButton);
        searchEditText = root.findViewById(R.id.searchEditText);
        mainScrollView = root.findViewById(R.id.mainScrollView);
        loginTextView = root.findViewById(R.id.loginText);

        // Initialize banner views
        bannerViewPager = root.findViewById(R.id.bannerViewPager);
        bannerIndicator = root.findViewById(R.id.bannerIndicator);

        // Initialize product section views
        fishRecyclerView = root.findViewById(R.id.recyclerViewFish);
        tankRecyclerView = root.findViewById(R.id.recyclerViewTank);
        foodRecyclerView = root.findViewById(R.id.recyclerViewFood);
        medicineRecyclerView = root.findViewById(R.id.recyclerViewMedicine);

        fishViewMoreBtn = root.findViewById(R.id.btnViewMoreFish);
        tankViewMoreBtn = root.findViewById(R.id.btnViewMoreTank);
        foodViewMoreBtn = root.findViewById(R.id.btnViewMoreFood);
        medicineViewMoreBtn = root.findViewById(R.id.btnViewMoreMedicine);
    }

    private void checkLoginStatus() {
        AccountService accountService = ApiClient.getAuthenticatedClient(requireContext())
                .create(AccountService.class);

        accountService.getCurrentAccount().enqueue(new Callback<Response<AccountResponse>>() {
            @Override
            public void onResponse(Call<Response<AccountResponse>> call,
                                   retrofit2.Response<Response<AccountResponse>> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null) {
                    isLoggedIn = true;
                    AccountResponse account = response.body().getData();
                    updateUIWithAccountInfo(account);
                } else {
                    isLoggedIn = false;
                    updateUIForGuest();
                }
            }

            @Override
            public void onFailure(Call<Response<AccountResponse>> call, Throwable t) {
                isLoggedIn = false;
                updateUIForGuest();
                Log.d("HomeFragment", "Failed to load user profile: " + t.getMessage());
            }
        });
    }

    private void updateUIWithAccountInfo(AccountResponse account) {
        if (account != null) {
            TextView nameTextView = root.findViewById(R.id.loginText);
            nameTextView.setText("Xin chào quý khách");

            TextView fullNameTextView = root.findViewById(R.id.fullName);
            if (fullNameTextView != null && account.getFullName() != null && !account.getFullName().isEmpty()) {
                fullNameTextView.setText(account.getFullName());
            }

            ShapeableImageView avatarView = root.findViewById(R.id.avatarImage);
            if (avatarView != null && account.getAvatarUrl() != null && !account.getAvatarUrl().isEmpty()) {
                Glide.with(requireContext())
                        .load(account.getAvatarUrl())
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .into(avatarView);
            }
        }
    }

    private void updateUIForGuest() {
        TextView nameTextView = root.findViewById(R.id.loginText);
        if (nameTextView != null) {
            nameTextView.setText("Đăng nhập");
        }

        TextView fullNameTextView = root.findViewById(R.id.fullName);
        if (fullNameTextView != null) {
            fullNameTextView.setText("Khách tham quan");
        }

        ShapeableImageView avatarView = root.findViewById(R.id.avatarImage);
        if (avatarView != null) {
            Glide.with(requireContext())
                    .load(R.drawable.logo)
                    .into(avatarView);
        }
    }

    private void setupProductSections() {
        // Setup horizontal RecyclerViews for each product section
        setupRecyclerView(fishRecyclerView, fishAdapter = new ProductAdapter());
        setupRecyclerView(tankRecyclerView, tankAdapter = new ProductAdapter());
        setupRecyclerView(foodRecyclerView, foodAdapter = new ProductAdapter());
        setupRecyclerView(medicineRecyclerView, medicineAdapter = new ProductAdapter());

        // Setup item click listeners
        fishAdapter.setOnItemClickListener(this::showAddToCartBottomSheet);
        tankAdapter.setOnItemClickListener(this::showAddToCartBottomSheet);
        foodAdapter.setOnItemClickListener(this::showAddToCartBottomSheet);
        medicineAdapter.setOnItemClickListener(this::showAddToCartBottomSheet);

        // Setup view more buttons
        fishViewMoreBtn.setOnClickListener(v -> navigateToProductList("Fish"));
        tankViewMoreBtn.setOnClickListener(v -> navigateToProductList("Aquarium"));
        foodViewMoreBtn.setOnClickListener(v -> navigateToProductList("Food"));
        medicineViewMoreBtn.setOnClickListener(v -> navigateToProductList("Medicine"));
    }

    private void setupRecyclerView(RecyclerView recyclerView, ProductAdapter adapter) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = spacingInPixels;
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void navigateToProductList(String productType) {
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putString("productType", productType);
        navController.navigate(R.id.navigation_fish, bundle);
    }

    private void loadProducts() {
        loadProductsByType("Fish", fishAdapter);
        loadProductsByType("Aquarium", tankAdapter);
        loadProductsByType("Food", foodAdapter);
        loadProductsByType("Medicine", medicineAdapter);
    }

    private void loadProductsByType(String type, ProductAdapter adapter) {
        ProductAPI api = ApiClient.getClient().create(ProductAPI.class);

        api.getProducts(null, type, null, null, null)
                .enqueue(new Callback<Response<ProductListData>>() {
                    @Override
                    public void onResponse(Call<Response<ProductListData>> call,
                                           retrofit2.Response<Response<ProductListData>> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                response.body().getData() != null) {

                            List<Product> products = response.body().getData().getProducts();
                            if (products != null && !products.isEmpty()) {
                                // Take first 10 products or less if there aren't that many
                                int endIndex = Math.min(products.size(), PRODUCTS_PER_SECTION);
                                adapter.setProducts(products.subList(0, endIndex));
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải sản phẩm: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<ProductListData>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
        TextView txtTotalPrice = bottomSheetView.findViewById(R.id.txtTotalPrice);

        txtName.setText(product.getProductName());
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        txtPrice.setText(currencyFormat.format(product.getPrice()) + " VNĐ");
        txtStock.setText("Còn: " + product.getQuantity() + " sản phẩm");

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(imgProduct);
        }

        final int[] quantity = {1};
        txtQuantity.setText(String.valueOf(quantity[0]));

        if (txtTotalPrice != null) {
            txtTotalPrice.setText("Thành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
        } else {
            txtPrice.setText("Đơn giá: " + currencyFormat.format(product.getPrice()) + " VNĐ");
            txtPrice.append("\nThành tiền: " + currencyFormat.format(product.getPrice() * quantity[0]) + " VNĐ");
        }

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                txtQuantity.setText(String.valueOf(quantity[0]));
                updateTotalPrice(txtTotalPrice, txtPrice, product.getPrice(), quantity[0], currencyFormat);
            } else {
                Toast.makeText(requireContext(), "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (quantity[0] < product.getQuantity()) {
                quantity[0]++;
                txtQuantity.setText(String.valueOf(quantity[0]));
                updateTotalPrice(txtTotalPrice, txtPrice, product.getPrice(), quantity[0], currencyFormat);
            } else {
                Toast.makeText(requireContext(), "Đã đạt số lượng tối đa", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (isLoggedIn) {
                addToCart(product.getProductId(), quantity[0]);
            } else {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                startActivity(intent);
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updateTotalPrice(TextView txtTotalPrice, TextView txtPrice, double price,
                                  int quantity, NumberFormat currencyFormat) {
        if (txtTotalPrice != null) {
            txtTotalPrice.setText("Thành tiền: " + currencyFormat.format(price * quantity) + " VNĐ");
        } else {
            txtPrice.setText("Đơn giá: " + currencyFormat.format(price) + " VNĐ");
            txtPrice.append("\nThành tiền: " + currencyFormat.format(price * quantity) + " VNĐ");
        }
    }

    private void addToCart(int productId, int quantity) {
        CartAPI api = ApiClient.getAuthenticatedClient(requireContext())
                .create(CartAPI.class);

        CreateOrUpdateCartRequest request = new CreateOrUpdateCartRequest(productId, quantity);

        api.addToCart(request).enqueue(new Callback<Response<CartResponse>>() {
            @Override
            public void onResponse(Call<Response<CartResponse>> call,
                                   retrofit2.Response<Response<CartResponse>> response) {
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
            public void onFailure(Call<Response<CartResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBannerSlider() {
        bannerList = new ArrayList<>();
        bannerList.add(R.drawable.banner_1);
        bannerList.add(R.drawable.banner_2);

        BannerAdapter bannerAdapter = new BannerAdapter(requireContext(), bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(8));
        transformer.addTransformer((page, position) -> {
            float abs = Math.abs(position);
            page.setScaleY(0.85f + (1 - abs) * 0.15f);
        });

        bannerViewPager.setPageTransformer(transformer);

        new TabLayoutMediator(bannerIndicator, bannerViewPager,
                (tab, position) -> {}).attach();

        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerViewPager == null) return;

                int currentItem = bannerViewPager.getCurrentItem();
                if (currentItem == bannerList.size() - 1) {
                    bannerViewPager.setCurrentItem(0);
                } else {
                    bannerViewPager.setCurrentItem(currentItem + 1);
                }

                sliderHandler.postDelayed(this, SLIDER_DELAY);
            }
        };

        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
            }
        });
    }

    private void setupClickListeners() {
        searchButton.setOnClickListener(v -> showSearchBar());

        closeSearchButton.setOnClickListener(v -> hideSearchBar());

        menuButton.setOnClickListener(v -> openNavigationMenu());

        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString());
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void showSearchBar() {
        searchBarContainer.setVisibility(View.VISIBLE);
        searchBarContainer.setAlpha(0f);
        searchBarContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchEditText.requestFocus();
                        showKeyboard();
                    }
                });

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
        params.topMargin = dpToPx(70);
        mainScrollView.setLayoutParams(params);
    }

    private void hideSearchBar() {
        searchBarContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchBarContainer.setVisibility(View.GONE);
                        searchEditText.setText("");
                        hideKeyboard();
                    }
                });

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
        params.topMargin = 0;
        mainScrollView.setLayoutParams(params);
    }

    private void openNavigationMenu() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.openNavigationDrawer();
        }
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            showAllItems();
            return;
        }

        filterItems(query);
    }

    private void showAllItems() {
        setProductSectionVisibility(View.VISIBLE);
    }

    private void filterItems(String query) {
        String lowerQuery = query.toLowerCase();

        boolean showFish = "cá".contains(lowerQuery) || "fish".contains(lowerQuery);
        boolean showTank = "bể".contains(lowerQuery) || "hồ".contains(lowerQuery) || "tank".contains(lowerQuery);
        boolean showFood = "thức ăn".contains(lowerQuery) || "food".contains(lowerQuery);
        boolean showMedicine = "thuốc".contains(lowerQuery) || "medicine".contains(lowerQuery);

        updateSectionVisibility("fish", showFish);
        updateSectionVisibility("tank", showTank);
        updateSectionVisibility("food", showFood);
        updateSectionVisibility("medicine", showMedicine);
    }

    private void setProductSectionVisibility(int visibility) {
        updateSectionVisibility("fish", visibility == View.VISIBLE);
        updateSectionVisibility("tank", visibility == View.VISIBLE);
        updateSectionVisibility("food", visibility == View.VISIBLE);
        updateSectionVisibility("medicine", visibility == View.VISIBLE);
    }

    private void updateSectionVisibility(String section, boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;

        switch (section) {
            case "fish":
                break;
            case "tank":
                break;
            case "food":
                break;
            case "medicine":
                break;
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}