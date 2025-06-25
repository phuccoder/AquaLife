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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.aqualife.LoginActivity;
import com.example.aqualife.MainActivity;
import com.example.aqualife.R;
import com.example.aqualife.adapter.BannerAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private View root;
    private MaterialCardView searchButton;
    private MaterialCardView searchBarContainer;
    private ImageView menuButton;
    private ImageView closeSearchButton;
    private EditText searchEditText;
    private ScrollView mainScrollView;
    private TextView loginTextView;

    // Banner slider components
    private ViewPager2 bannerViewPager;
    private TabLayout bannerIndicator;
    private List<Integer> bannerList;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private final long SLIDER_DELAY = 10000;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_home, container, false);

        initializeViews();
        setupClickListeners();
        setupSearchFunctionality();
        setupBannerSlider();

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
    }

    private void setupBannerSlider() {
        // Initialize banner images
        bannerList = new ArrayList<>();
        bannerList.add(R.drawable.banner_1);
        bannerList.add(R.drawable.banner_2);

        // Set up banner adapter
        BannerAdapter bannerAdapter = new BannerAdapter(requireContext(), bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        // Set up page transformer for animation
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(8));
        transformer.addTransformer((page, position) -> {
            float abs = Math.abs(position);
            page.setScaleY(0.85f + (1 - abs) * 0.15f);
        });

        bannerViewPager.setPageTransformer(transformer);

        // Connect with TabLayout indicator
        new TabLayoutMediator(bannerIndicator, bannerViewPager,
                (tab, position) -> {}).attach();

        // Auto slide functionality
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerViewPager == null) return;

                int currentItem = bannerViewPager.getCurrentItem();
                if (currentItem == bannerList.size() - 1) {
                    // Go back to first slide when reached the end
                    bannerViewPager.setCurrentItem(0);
                } else {
                    // Go to next slide
                    bannerViewPager.setCurrentItem(currentItem + 1);
                }

                sliderHandler.postDelayed(this, SLIDER_DELAY);
            }
        };

        // Start auto sliding
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);

        // Stop auto sliding when user is manually scrolling
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
        // Handle search input
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

        // Handle search submit
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
        // Show search bar with animation
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

        // Add margin to scroll view to accommodate search bar
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
        params.topMargin = dpToPx(70); // Adjust based on search bar height
        mainScrollView.setLayoutParams(params);
    }

    private void hideSearchBar() {
        // Hide search bar with animation
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

        // Remove margin from scroll view
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
        params.topMargin = 0;
        mainScrollView.setLayoutParams(params);
    }

    private void openNavigationMenu() {
        // If using DrawerLayout in MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.openNavigationDrawer();
        }
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            // Show all items
            showAllItems();
            return;
        }

        // Filter items based on search query
        filterItems(query);
    }

    private void showAllItems() {
        // Show all product sections
        setProductSectionVisibility(View.VISIBLE);
    }

    private void filterItems(String query) {
        String lowerQuery = query.toLowerCase();

        // Hide/show sections based on search query
        boolean showFish = "cá".contains(lowerQuery) || "fish".contains(lowerQuery);
        boolean showTank = "bể".contains(lowerQuery) || "hồ".contains(lowerQuery) || "tank".contains(lowerQuery);
        boolean showFood = "thức ăn".contains(lowerQuery) || "food".contains(lowerQuery);
        boolean showMedicine = "thuốc".contains(lowerQuery) || "medicine".contains(lowerQuery);

        // Update visibility of sections
        updateSectionVisibility("fish", showFish);
        updateSectionVisibility("tank", showTank);
        updateSectionVisibility("food", showFood);
        updateSectionVisibility("medicine", showMedicine);
    }

    private void setProductSectionVisibility(int visibility) {
        // Set visibility for all sections
        updateSectionVisibility("fish", visibility == View.VISIBLE);
        updateSectionVisibility("tank", visibility == View.VISIBLE);
        updateSectionVisibility("food", visibility == View.VISIBLE);
        updateSectionVisibility("medicine", visibility == View.VISIBLE);
    }

    private void updateSectionVisibility(String section, boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;

        // Find and update section views based on section type
        switch (section) {
            case "fish":
                // Update fish section visibility
                break;
            case "tank":
                // Update tank section visibility
                break;
            case "food":
                // Update food section visibility
                break;
            case "medicine":
                // Update medicine section visibility
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