package com.example.aqualife.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.aqualife.R;
import com.example.aqualife.payload.response.AccountResponse;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.AccountService;

import retrofit2.Call;
import retrofit2.Callback;

public class ProfileFragment extends Fragment {

    private View rootView;
    private ImageView avatarView;
    private TextView tvUserName;
    private TextView tvEmail;
    private TextView tvPhoneNumber;
    private TextView tvAddress;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_user_profile, container, false);

        initializeViews();
        loadUserProfile();

        return rootView;
    }

    private void initializeViews() {
        avatarView = rootView.findViewById(R.id.avatarView);
        tvUserName = rootView.findViewById(R.id.tv_user_name);
        tvEmail = rootView.findViewById(R.id.tv_email);
        tvPhoneNumber = rootView.findViewById(R.id.tv_phone_number);
        tvAddress = rootView.findViewById(R.id.tv_address);
        progressBar = rootView.findViewById(R.id.progressBar);
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        AccountService accountService = ApiClient.getAuthenticatedClient(requireContext())
                .create(AccountService.class);

        accountService.getCurrentAccount().enqueue(new Callback<Response<AccountResponse>>() {
            @Override
            public void onResponse(Call<Response<AccountResponse>> call,
                                   retrofit2.Response<Response<AccountResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null) {

                    AccountResponse account = response.body().getData();
                    updateUIWithAccountInfo(account);
                } else {
                    Toast.makeText(requireContext(), "Không thể tải thông tin tài khoản",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<AccountResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithAccountInfo(AccountResponse account) {
        if (account != null) {
            // Update user name
            if (account.getFullName() != null && !account.getFullName().isEmpty()) {
                tvUserName.setText(account.getFullName());
            }

            // Update email
            if (account.getEmail() != null && !account.getEmail().isEmpty()) {
                tvEmail.setText(account.getEmail());
            }

            // Update phone number
            if (account.getPhoneNumber() != null && !account.getPhoneNumber().isEmpty()) {
                tvPhoneNumber.setText(account.getPhoneNumber());
            }

            // Update address
            if (account.getAddress() != null && !account.getAddress().isEmpty()) {
                tvAddress.setText(account.getAddress());
            } else {
                tvAddress.setText("Chưa cập nhật địa chỉ");
            }

            // Update avatar
            if (account.getAvatarUrl() != null && !account.getAvatarUrl().isEmpty()) {
                Glide.with(requireContext())
                        .load(account.getAvatarUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(avatarView);
            }
        }
    }
}