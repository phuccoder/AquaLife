package com.example.aqualife.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.model.OrderResponse;
import com.example.aqualife.ui.checkout.activity.PaymentActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<OrderResponse> orderList;
    private Context context;

    public OrderAdapter(List<OrderResponse> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponse order = orderList.get(position);
        holder.txtOrderId.setText("Mã đơn: #" + order.getOrderId());
        holder.txtTotalPrice.setText(order.getTotalPrice() + " VND");
        setOrderStatus(holder, order);
        holder.txtDate.setText(formatOrderDate(order.getOrderDate()));
        CartItemAdapter cartItemAdapter = new CartItemAdapter(order.getCartItems(), context);
        holder.rvCartItems.setAdapter(cartItemAdapter);
        holder.rvCartItems.setLayoutManager(new LinearLayoutManager(context));
    }
    private void setOrderStatus(OrderViewHolder holder, OrderResponse order) {
        ColorStateList chipColor;
        int iconRes;
        String displayText;

        switch (order.getOrderStatus().toUpperCase()) {
            case "SUCCESS":
            case "HOÀN THÀNH":
                displayText = "Hoàn thành";
                chipColor = ColorStateList.valueOf(Color.parseColor("#4CAF50"));
                iconRes = R.drawable.ic_check_circle;
                holder.mbBuyAgain.setVisibility(View.GONE);
                break;

            case "CANCEL":
            case "ĐÃ HỦY":
                displayText = "Đã hủy";
                chipColor = ColorStateList.valueOf(Color.parseColor("#F44336"));
                iconRes = R.drawable.ic_cancel;
                holder.mbBuyAgain.setVisibility(View.GONE);
                break;

            case "PENDING":
            case "CHỜ XÁC NHẬN":
                displayText = "Chờ xác nhận";
                chipColor = ColorStateList.valueOf(Color.parseColor("#9C27B0"));
                iconRes = R.drawable.ic_pending;
                holder.mbBuyAgain.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(), PaymentActivity.class);
                    intent.putExtra("orderId", order.getOrderId());
                    intent.putExtra("amount", order.getTotalPrice());
                    v.getContext().startActivity(intent);
                });
                break;

            default:
                displayText = order.getOrderStatus();
                chipColor = ColorStateList.valueOf(Color.parseColor("#757575"));
                iconRes = R.drawable.ic_pending;
                break;
        }
        holder.txtOrderStatus.setText(displayText);
        holder.txtOrderStatus.setChipBackgroundColor(chipColor);
        holder.txtOrderStatus.setChipIcon(ContextCompat.getDrawable(context, iconRes));
        holder.txtOrderStatus.setChipIconTint(ColorStateList.valueOf(Color.WHITE));
        holder.txtOrderStatus.setTextColor(Color.WHITE);
    }
    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtTotalPrice, txtDate;
        RecyclerView rvCartItems;
        Chip txtOrderStatus;
        MaterialButton mbBuyAgain;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            rvCartItems = itemView.findViewById(R.id.rvCartItems);
            txtDate = itemView.findViewById(R.id.txtDate);
            mbBuyAgain = itemView.findViewById(R.id.btnReorder);
        }
    }
    private String formatOrderDate(String orderDate) {
        try {
            // Parse ISO date format (assuming format like "2024-01-15T10:30:00")
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(orderDate);
            return "Đặt ngày " + outputFormat.format(date);
        } catch (ParseException e) {
            // If parsing fails, try to extract just the date part
            if (orderDate.length() >= 10) {
                String datePart = orderDate.substring(0, 10);
                try {
                    SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date = simpleFormat.parse(datePart);
                    return "Đặt ngày " + outputFormat.format(date);
                } catch (ParseException ex) {
                    return "Đặt hôm nay";
                }
            }
            return "Đặt hôm nay";
        }
    }
}


