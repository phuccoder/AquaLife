package com.example.aqualife.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aqualife.R;

public class CartFragment extends Fragment {

    private TextView textCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_all_shopping_cart, container, false);

        textCart = root.findViewById(R.id.text_cart);
        textCart.setText("Your Shopping Cart");

        return root;
    }
}