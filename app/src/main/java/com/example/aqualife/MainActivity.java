package com.example.aqualife;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.aqualife.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private NavController navController;
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupNavigation();
        setupDrawer();
    }

    private void setupToolbar() {
        Toolbar toolbar = new Toolbar(this);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        navController = navHostFragment.getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_order,
                R.id.navigation_profile,
                R.id.navigation_shopping_cart)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        binding.navView.setOnItemSelectedListener(item -> {
//            while (navController.popBackStack()) {
//            }

            navController.navigate(item.getItemId());
            return true;
        });
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        binding.navViewDrawer.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void handleNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        int targetFragmentId = R.id.navigation_home; // default

        Bundle bundle = new Bundle();

        // Map drawer items to fragments
        if (itemId == R.id.nav_home) {
            targetFragmentId = R.id.navigation_home;
        } if (itemId == R.id.nav_fish) {
            targetFragmentId = R.id.navigation_fish;
            bundle.putString("productType", "Fish");
        } else if (itemId == R.id.nav_tank) {
            targetFragmentId = R.id.navigation_fish;
            bundle.putString("productType", "Aquarium");
        } else if (itemId == R.id.nav_food) {
            targetFragmentId = R.id.navigation_fish;
            bundle.putString("productType", "Food");
        } else if (itemId == R.id.nav_medicine) {
            targetFragmentId = R.id.navigation_fish;
            bundle.putString("productType", "Medicine");
        } else if (itemId == R.id.nav_about) {
            targetFragmentId = R.id.navigation_profile;
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
            return;
        }

//        while (navController.popBackStack()) {
//        }

        navController.navigate(targetFragmentId, bundle);
        
        binding.navView.setSelectedItemId(targetFragmentId);
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    clearUserSession();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void clearUserSession() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public void openNavigationDrawer() {
        if (binding.drawerLayout != null) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout != null && binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (!navController.popBackStack()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}