package com.example.aqualife;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.adapter.NotificationAdapter;
import com.example.aqualife.payload.response.NotificationResponse;
import com.example.aqualife.util.NotificationHelper;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private static final String TAG = "NotificationActivity";
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final int ACCOUNT_ID = 1; // Admin account ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView_notifications);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        NotificationHelper.getNotifications(this, ACCOUNT_ID, new NotificationHelper.NotificationCallback() {
            @Override
            public void onSuccess(List<NotificationResponse> notifications) {
                runOnUiThread(() -> {
                    if (notifications != null && !notifications.isEmpty()) {
                        adapter.setNotifications(notifications);
                    } else {
                        Toast.makeText(NotificationActivity.this, "No notifications found", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(NotificationActivity.this, "Failed to load notifications: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register FCM token when activity resumes
        NotificationHelper.registerFCMToken(this);
    }
}