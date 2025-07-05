package com.example.aqualife.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.aqualife.R;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.request.UpdateAccountRequest;
import com.example.aqualife.payload.response.AccountResponse;
import com.example.aqualife.services.AccountService;
import com.example.aqualife.util.FirebaseStorageHelper;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int IMAGE_PICKER_REQUEST = 100;

    // UI Components
    private ImageView avatarView;
    private TextView tvUserName, tvEmail, tvPhoneNumber, tvAddress;
    private MaterialButton btnSelectImage;
    private Button btnEditInfo, btnUpload;
    private ImageButton btnEditProfile;
    private ProgressBar progressBar;

    // Data
    private Uri selectedImageUri;
    private String newAvatarUrl;
    private AccountResponse currentAccount;
    private FirebaseStorageHelper firebaseStorageHelper;
    private boolean isEditMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_profile, container, false);

        initializeViews(view);
        setupListeners();

        // Initialize Firebase helper
        firebaseStorageHelper = new FirebaseStorageHelper(requireContext());

        // Load user profile data
        loadUserProfile();

        return view;
    }

    private void initializeViews(View view) {
        avatarView = view.findViewById(R.id.avatarView);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number);
        tvAddress = view.findViewById(R.id.tv_address);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnEditInfo = view.findViewById(R.id.btn_edit_info);
        btnUpload = view.findViewById(R.id.btnUpload);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        progressBar = view.findViewById(R.id.progressBar);

        // Initially hide upload button until edit mode is activated
        btnUpload.setEnabled(false);
        btnUpload.setAlpha(0.5f);
    }

    private void setupListeners() {
        // Avatar selection button
        btnSelectImage.setOnClickListener(v -> openImagePicker());

        // Edit profile buttons (both the icon and the button)
        btnEditProfile.setOnClickListener(v -> toggleEditMode());
        btnEditInfo.setOnClickListener(v -> toggleEditMode());

        // Save changes button
        btnUpload.setOnClickListener(v -> saveProfileChanges());
    }

    private void openImagePicker() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(IMAGE_PICKER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                selectedImageUri = data.getData();

                // Display the selected image
                Glide.with(requireContext())
                        .load(selectedImageUri)
                        .circleCrop()
                        .into(avatarView);

                // Upload the image to Firebase
                uploadImageToFirebase();

                btnUpload.setEnabled(true);
                btnUpload.setAlpha(1.0f);
            }
        }
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri == null) return;

        showProgress(true);

        firebaseStorageHelper.uploadAvatarImage(selectedImageUri, new FirebaseStorageHelper.OnImageUploadListener() {
            @Override
            public void onUploadStart() {
                Log.d(TAG, "Image upload started");
            }

            @Override
            public void onUploadProgress(int progress) {
                Log.d(TAG, "Upload progress: " + progress + "%");
            }

            @Override
            public void onUploadSuccess(String downloadUrl) {
                showProgress(false);
                newAvatarUrl = downloadUrl;
                Log.d(TAG, "Image uploaded: " + downloadUrl);
                Toast.makeText(requireContext(), "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUploadFailure(String error) {
                showProgress(false);
                Log.e(TAG, "Upload failed: " + error);
                Toast.makeText(requireContext(), "Lỗi khi tải ảnh lên: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        showProgress(true);

        AccountService accountService = ApiClient.getAuthenticatedClient(requireContext())
                .create(AccountService.class);

        accountService.getCurrentAccount().enqueue(new Callback<Response<AccountResponse>>() {
            @Override
            public void onResponse(Call<Response<AccountResponse>> call,
                                   retrofit2.Response<Response<AccountResponse>> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentAccount = response.body().getData();
                    updateUI(currentAccount);
                } else {
                    Log.e(TAG, "Failed to load profile: " + response.message());
                    Toast.makeText(requireContext(), "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<AccountResponse>> call, Throwable t) {
                showProgress(false);
                Log.e(TAG, "Error loading profile", t);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(AccountResponse account) {
        if (account == null) return;

        tvUserName.setText(account.getFullName());
        tvEmail.setText(account.getEmail());
        tvPhoneNumber.setText(account.getPhoneNumber());
        tvAddress.setText(account.getAddress());

        // Load avatar image
        if (account.getAvatarUrl() != null && !account.getAvatarUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(account.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(avatarView);
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            // Enter edit mode
            showEditableFields();
            btnEditInfo.setText("Hủy");
            btnUpload.setEnabled(true);
            btnUpload.setAlpha(1.0f);
        } else {
            // Exit edit mode
            hideEditableFields();
            btnEditInfo.setText("Cập nhật");

            // Disable save button if no changes
            if (newAvatarUrl == null) {
                btnUpload.setEnabled(false);
                btnUpload.setAlpha(0.5f);
            }

            // Restore original values
            if (currentAccount != null) {
                updateUI(currentAccount);
            }
        }
    }

    private void showEditableFields() {
        // Hide the original TextViews
        tvUserName.setVisibility(View.GONE);
        tvEmail.setVisibility(View.GONE);
        tvPhoneNumber.setVisibility(View.GONE);
        tvAddress.setVisibility(View.GONE);

        // Show edit fields that are already in the layout
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.layout_edit_name).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_edit_email).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_edit_phone).setVisibility(View.VISIBLE);
            view.findViewById(R.id.layout_edit_address).setVisibility(View.VISIBLE);

            // Set current values
            TextInputEditText etName = view.findViewById(R.id.edit_name);
            TextInputEditText etEmail = view.findViewById(R.id.edit_email);
            TextInputEditText etPhone = view.findViewById(R.id.edit_phone);
            TextInputEditText etAddress = view.findViewById(R.id.edit_address);

            etName.setText(tvUserName.getText());
            etEmail.setText(tvEmail.getText());
            etPhone.setText(tvPhoneNumber.getText());
            etAddress.setText(tvAddress.getText());
        }
    }


    private void addEditTextOverlay(ViewGroup parent, TextView textView, String hint, int editTextId) {
        // Find the parent LinearLayout of the TextView
        ViewGroup container = (ViewGroup) textView.getParent();

        // Create new TextInputLayout with appropriate LayoutParams
        TextInputLayout inputLayout = new TextInputLayout(requireContext());

        // Use the correct LayoutParams type - LinearLayout.LayoutParams
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        inputLayout.setLayoutParams(layoutParams);
        inputLayout.setHint(hint);

        // Create EditText with appropriate LayoutParams
        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setId(editTextId);
        editText.setText(textView.getText());

        // Use TextInputLayout.LayoutParams for the EditText
        TextInputLayout.LayoutParams editTextParams = new TextInputLayout.LayoutParams(
                TextInputLayout.LayoutParams.MATCH_PARENT,
                TextInputLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(editTextParams);

        inputLayout.addView(editText);
        container.addView(inputLayout);
    }

    private void hideEditableFields() {
        tvUserName.setVisibility(View.VISIBLE);
        tvEmail.setVisibility(View.VISIBLE);
        tvPhoneNumber.setVisibility(View.VISIBLE);
        tvAddress.setVisibility(View.VISIBLE);

        // Hide edit fields
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.layout_edit_name).setVisibility(View.GONE);
            view.findViewById(R.id.layout_edit_email).setVisibility(View.GONE);
            view.findViewById(R.id.layout_edit_phone).setVisibility(View.GONE);
            view.findViewById(R.id.layout_edit_address).setVisibility(View.GONE);
        }
    }

    private void removeEditTextOverlay(ViewGroup parent, int editTextId) {
        TextInputEditText editText = parent.findViewById(editTextId);
        if (editText != null) {
            ViewGroup inputLayout = (ViewGroup) editText.getParent();
            ViewGroup container = (ViewGroup) inputLayout.getParent();
            container.removeView(inputLayout);
        }
    }

    private void saveProfileChanges() {
        if (currentAccount == null) {
            Toast.makeText(requireContext(), "Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = getView();
        if (view == null) return;

        // Get edited values first
        TextInputEditText etName = view.findViewById(R.id.edit_name);
        TextInputEditText etEmail = view.findViewById(R.id.edit_email);
        TextInputEditText etPhone = view.findViewById(R.id.edit_phone);
        TextInputEditText etAddress = view.findViewById(R.id.edit_address);

        // Update current account with edited values
        String newName = etName.getText().toString();
        String newEmail = etEmail.getText().toString();
        String newPhone = etPhone.getText().toString();
        String newAddress = etAddress.getText().toString();

        // Save current values to show after edit mode is exited
        tvUserName.setText(newName);
        tvEmail.setText(newEmail);
        tvPhoneNumber.setText(newPhone);
        tvAddress.setText(newAddress);

        // Update account model
        currentAccount.setFullName(newName);
        currentAccount.setEmail(newEmail);
        currentAccount.setPhoneNumber(newPhone);
        currentAccount.setAddress(newAddress);

        // Exit edit mode
        isEditMode = false;
        hideEditableFields();
        btnEditInfo.setText("Cập nhật");

        showProgress(true);

        // Create update request
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setFullName(currentAccount.getFullName());
        request.setEmail(currentAccount.getEmail());
        request.setPhoneNumber(currentAccount.getPhoneNumber());
        request.setAddress(currentAccount.getAddress());
        request.setRole("CUSTOMER");
        request.setActive(true);

        // Set avatar URL (use new one if available)
        if (newAvatarUrl != null && !newAvatarUrl.isEmpty()) {
            request.setAvatarUrl(newAvatarUrl);
        } else {
            request.setAvatarUrl(currentAccount.getAvatarUrl());
        }

        // Call API to update account
        AccountService accountService = ApiClient.getAuthenticatedClient(requireContext())
                .create(AccountService.class);

        accountService.updateAccount(currentAccount.getAccountId(), request)
                .enqueue(new Callback<Response<AccountResponse>>() {
                    @Override
                    public void onResponse(Call<Response<AccountResponse>> call,
                                           retrofit2.Response<Response<AccountResponse>> response) {
                        showProgress(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();

                            // Update the UI with new data
                            if (response.body().getData() != null) {
                                currentAccount = response.body().getData();
                                updateUI(currentAccount);

                                // Reset avatar URL
                                newAvatarUrl = null;

                                // Disable save button if no changes
                                btnUpload.setEnabled(false);
                                btnUpload.setAlpha(0.5f);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi cập nhật: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Update failed: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<AccountResponse>> call, Throwable t) {
                        showProgress(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Network error", t);
                    }
                });
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}