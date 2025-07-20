package com.example.aqualife.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.payload.request.ChatMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<ChatMessage> messageList;
    private int currentUserId;

    public ChatMessageAdapter(Context context, List<ChatMessage> messageList, int currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        return message.isSentByCurrentUser(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for sent messages
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime, tvMessageStatusText;
        ImageView ivMessageStatus;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tv_message_text);
            tvMessageTime = itemView.findViewById(R.id.tv_message_time);
            tvMessageStatusText = itemView.findViewById(R.id.tv_message_status_text);
            ivMessageStatus = itemView.findViewById(R.id.iv_message_status);
        }

        void bind(ChatMessage message) {
            tvMessageTime.setText(formatTime(message.getSendAt()));

            // Handle message content (attachment or text)
            if (message.getMessage().startsWith("http")) {
                tvMessageText.setText("📎 Attachment");
                tvMessageText.setTextColor(Color.BLUE);
                tvMessageText.setOnClickListener(v -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMessage()));
                    context.startActivity(browserIntent);
                });
            } else {
                tvMessageText.setText(message.getMessage());
                tvMessageText.setTextColor(Color.BLACK);
                tvMessageText.setOnClickListener(null);
            }

            // Handle message status
            if (message.isFailedMessage()) {
                tvMessageStatusText.setVisibility(View.VISIBLE);
                tvMessageStatusText.setText("Failed to send • Tap to retry");
                ivMessageStatus.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> {
                    // Retry logic here
                });
            } else if (message.isSendingMessage()) {
                tvMessageStatusText.setVisibility(View.VISIBLE);
                tvMessageStatusText.setText("Sending...");
                ivMessageStatus.setVisibility(View.GONE);
            } else {
                tvMessageStatusText.setVisibility(View.GONE);
                ivMessageStatus.setVisibility(View.VISIBLE);

                // Set appropriate status icon
                switch (message.getDeliveryStatus()) {
                    case "SENT":
                    case "DELIVERED":
                    case "READ":
                        ivMessageStatus.setImageResource(R.drawable.ic_check_double);
                        break;
                    default:
                        ivMessageStatus.setImageResource(R.drawable.ic_check_double);
                        break;
                }
            }
        }
    }

    // ViewHolder for received messages
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime, tvSenderName;
        ImageView ivStoreAvatar;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tv_message_text);
            tvMessageTime = itemView.findViewById(R.id.tv_message_time);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            ivStoreAvatar = itemView.findViewById(R.id.iv_store_avatar);
        }

        void bind(ChatMessage message) {
            tvMessageText.setText(message.getMessage());
            tvMessageTime.setText(formatTime(message.getSendAt()));

            // For group chats, you might want to show sender name
            // For now, hide it since it's a direct chat with store
            tvSenderName.setVisibility(View.GONE);

            // Set store avatar (you can load from URL if needed)
            ivStoreAvatar.setImageResource(R.drawable.logo);
        }
    }

    private String formatTime(String timeString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = inputFormat.parse(timeString);
            return date != null ? outputFormat.format(date) : timeString;
        } catch (ParseException e1) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Date date = inputFormat.parse(timeString);
                return date != null ? outputFormat.format(date) : timeString;
            } catch (ParseException e2) {
                return timeString;
            }
        }
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    public void updateMessage(int position, ChatMessage message) {
        if (position >= 0 && position < messageList.size()) {
            messageList.set(position, message);
            notifyItemChanged(position);
        }
    }
}