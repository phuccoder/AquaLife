package com.example.aqualife.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aqualife.R;

public class OrderFragment extends Fragment {

    private TextView textOrder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_view_order, container, false);

        textOrder = root.findViewById(R.id.text_order);
        textOrder.setText("Your Orders");

        return root;
    }
}