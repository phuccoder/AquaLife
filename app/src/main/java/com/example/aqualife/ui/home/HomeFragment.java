package com.example.aqualife.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
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

import com.example.aqualife.MainActivity;
import com.example.aqualife.R;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    private View root;
    private MaterialCardView searchButton;
    private MaterialCardView searchBarContainer;
    private ImageView menuButton;
    private ImageView closeSearchButton;
    private EditText searchEditText;
    private ScrollView mainScrollView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_home, container, false);

        initializeViews();
        setupClickListeners();
        setupSearchFunctionality();

        return root;
    }

    private void initializeViews() {
        searchButton = root.findViewById(R.id.searchButton);
        searchBarContainer = root.findViewById(R.id.searchBarContainer);
        menuButton = root.findViewById(R.id.menuButton);
        closeSearchButton = root.findViewById(R.id.closeSearchButton);
        searchEditText = root.findViewById(R.id.searchEditText);
        mainScrollView = root.findViewById(R.id.mainScrollView);
    }

    private void setupClickListeners() {
        // Search button click - show search bar
        searchButton.setOnClickListener(v -> showSearchBar());

        // Close search button - hide search bar
        closeSearchButton.setOnClickListener(v -> hideSearchBar());

        // Menu button click - open navigation drawer
        menuButton.setOnClickListener(v -> openNavigationMenu());
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

        // Alternative: Navigate to a navigation menu activity or fragment
        // Navigation.findNavController(root).navigate(R.id.action_to_navigation_menu);
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            // Show all items
            showAllItems();
            return;
        }

        // Filter items based on search query
        filterItems(query);

        // You can also navigate to a dedicated search results page
        // Bundle bundle = new Bundle();
        // bundle.putString("search_query", query);
        // Navigation.findNavController(root).navigate(R.id.action_to_search_results, bundle);
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