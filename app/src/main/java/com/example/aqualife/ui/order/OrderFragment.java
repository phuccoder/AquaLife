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
//import com.example.aqualife.adapter.CartAdapter;
//import com.example.aqualife.model.CartItemResponse;
//import com.example.aqualife.model.CartResponse;
//import com.example.aqualife.model.Response;
//import com.example.aqualife.services.CartAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderFragment extends Fragment {
    private RecyclerView recyclerCart;
//    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_view_order, container, false);

//        recyclerCart = root.findViewById(R.id.recyclerOrder);
//        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new CartAdapter();
//        recyclerCart.setAdapter(adapter);
//
//        fetchCartData();

        return root;
    }

//    private void fetchCartData() {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://103.245.236.207:8080/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        CartAPI api = retrofit.create(CartAPI.class);
//        String token = "Bearer " + getString(R.string.token); // Cập nhật nếu dùng SharedPreferences
//
//        api.getCartByAccountId(token, 2).enqueue(new Callback<Response<CartResponse>>() {
//            @Override
//            public void onResponse(Call<Response<CartResponse>> call, retrofit2.Response<Response<CartResponse>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    CartResponse items = response.body().getData();
//                    adapter.setCartItems(items);
//                } else {
//                    Toast.makeText(getContext(), "Lỗi lấy giỏ hàng", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Response<CartResponse>> call, Throwable t) {
//                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}