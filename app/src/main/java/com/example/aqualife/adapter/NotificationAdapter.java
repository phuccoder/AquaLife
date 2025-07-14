package com.example.aqualife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.payload.response.NotificationResponse;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationResponse> notifications = new ArrayList<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public void setNotifications(List<NotificationResponse> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponse notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private TextView textDate;
        private View indicatorRead;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textDate = itemView.findViewById(R.id.text_date);
            indicatorRead = itemView.findViewById(R.id.indicator_read);
        }

        public void bind(NotificationResponse notification) {
            textMessage.setText(notification.getMessage());

            if (notification.getCreateAt() != null) {
                textDate.setText(notification.getCreateAt().format(formatter));
            }

            // Show/hide read indicator
            if (notification.getIsRead() != null && notification.getIsRead()) {
                indicatorRead.setVisibility(View.GONE);
            } else {
                indicatorRead.setVisibility(View.VISIBLE);
            }
        }
    }
}