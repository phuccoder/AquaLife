package com.example.aqualife.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.adapter.OrderAdapter;
import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.OrderResponse;
import com.example.aqualife.model.Response;
import com.example.aqualife.network.ApiClient;
import com.example.aqualife.services.CartAPI;
import com.example.aqualife.services.OrderAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class OrderFragment extends Fragment {
    private RecyclerView rvOrders;
    private TextView txtNoOrders;
    private AccountInfor account;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_view_order, container, false);
        rvOrders = view.findViewById(R.id.rvOrders);
        txtNoOrders  = view.findViewById(R.id.txtNoOrders);
        fetchAccountData();

        return view;
    }

    private void fetchOrders() {
        OrderAPI orderAPI = ApiClient.getAuthenticatedClient(requireContext())
                .create(OrderAPI.class);

        orderAPI.getOrdersByAccountId(account.getAccountId())
                .enqueue(new Callback<Response<List<OrderResponse>>>() {
                    @Override
                    public void onResponse(Call<Response<List<OrderResponse>>> call,
                                           retrofit2.Response<Response<List<OrderResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<OrderResponse> orders = response.body().getData();
                            if (orders.isEmpty()) {
                                txtNoOrders.setVisibility(View.VISIBLE);
                                rvOrders.setVisibility(View.GONE);
                            } else {
                                txtNoOrders.setVisibility(View.GONE);
                                rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
                                rvOrders.setAdapter(new OrderAdapter(orders, getContext()));
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi lấy danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<List<OrderResponse>>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void fetchAccountData() {
        CartAPI api = ApiClient.getAuthenticatedClient(requireContext())
                .create(CartAPI.class);

        api.getAccountId().enqueue(new Callback<Response<AccountInfor>>() {
            @Override
            public void onResponse(Call<Response<AccountInfor>> call, retrofit2.Response<Response<AccountInfor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    account = response.body().getData();
                    fetchOrders();
                }
            }

            @Override
            public void onFailure(Call<Response<AccountInfor>> call, Throwable t) {

            }
        });
    }
}
