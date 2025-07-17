package com.example.aqualife;

import static android.content.ContentValues.TAG;
import static com.google.firebase.FirebaseApp.initializeApp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.aqualife.util.PermissionManager;
import com.example.aqualife.util.UserSessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.aqualife.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;

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
        checkPermissions();
    }

    private void initializeApp() {
        Log.d(TAG, "App initialization started.");
        Toast.makeText(this, "Welcome to AquaLife!", Toast.LENGTH_SHORT).show();

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
        int targetFragmentId = R.id.navigation_home;

        Bundle bundle = new Bundle();
        boolean shouldNavigate = true;

        if (itemId == R.id.nav_home) {
            targetFragmentId = R.id.navigation_home;
        } else if (itemId == R.id.nav_fish) {
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
            targetFragmentId = R.id.navigation_about_us;
        } else if (itemId == R.id.nav_all_products) {
            targetFragmentId = R.id.navigation_fish;
            bundle.putString("productType", null);
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
            shouldNavigate = false;
        }

        if (shouldNavigate) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_home, false)
                    .build();

            navController.navigate(targetFragmentId, bundle, navOptions);

            if (targetFragmentId == R.id.navigation_home ||
                    targetFragmentId == R.id.navigation_order ||
                    targetFragmentId == R.id.navigation_profile ||
                    targetFragmentId == R.id.navigation_shopping_cart) {
                binding.navView.setSelectedItemId(targetFragmentId);
            }
        }
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    new UserSessionManager(this).logoutUser();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void checkPermissions() {
        PermissionManager.checkAndRequestAllPermissions(this, new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Log.d(TAG, "All permissions granted!");
                initializeApp();
            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions) {
                Log.w(TAG, "Some permissions denied: " + Arrays.toString(deniedPermissions));
                // Hiển thị dialog giải thích
                showPermissionExplanationDialog(deniedPermissions);
            }
        });
    }

    private void showPermissionExplanationDialog(String[] deniedPermissions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cần cấp quyền")
                .setMessage("Ứng dụng cần các quyền này để hoạt động tốt:\n" +
                        "- Thông báo: Để gửi thông báo quan trọng\n" +
                        "- Rung: Để nhắc nhở khi có thông báo\n" +
                        "- Không làm phiền: Để hiển thị thông báo ngay cả khi điện thoại ở chế độ im lặng")
                .setPositiveButton("Cấp quyền", (dialog, which) -> {
                    PermissionManager.openAppSettings(this);
                })
                .setNegativeButton("Bỏ qua", (dialog, which) -> {
                    // Vẫn cho phép sử dụng app nhưng hạn chế tính năng
                    initializeApp();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionManager.handlePermissionResult(this, requestCode, permissions, grantResults,
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        Log.d(TAG, "Permission granted after request");
                        initializeApp();
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                        Log.w(TAG, "Permission still denied: " + Arrays.toString(deniedPermissions));
                        showPermissionExplanationDialog(deniedPermissions);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        PermissionManager.handleActivityResult(this, requestCode,
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        Log.d(TAG, "Do Not Disturb permission granted");
                        initializeApp();
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                        Log.w(TAG, "Do Not Disturb permission denied");
                        // Vẫn cho phép sử dụng app
                        initializeApp();
                    }
                });
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