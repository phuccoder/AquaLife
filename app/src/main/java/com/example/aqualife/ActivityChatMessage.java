package com.example.aqualife;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.network.ApiClient;
import com.example.aqualife.payload.request.ChatMessage;
import com.example.aqualife.payload.request.ChatMessageRequest;
import com.example.aqualife.payload.response.ChatMessageReponse;
import com.example.aqualife.services.ChatMessageService;
import com.example.aqualife.adapter.ChatMessageAdapter;
import com.example.aqualife.model.Response;
import com.example.aqualife.util.FirebaseStorageHelper;
import com.example.aqualife.util.UserSessionManager;
import com.google.firebase.messaging.FirebaseMessaging;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ActivityChatMessage extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageView ivSend, ivBack, ivAttach;
    private TextView tvStoreName, tvStoreStatus;

    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private ChatMessageService chatService;

    private int currentUserId;
    private int receiverId = 1; // Store's ID (assuming store has ID 1)
    private String jwtToken;
    private UserSessionManager sessionManager;
    private BroadcastReceiver chatMessageReceiver;
    private static final int PICK_FILE_REQUEST_CODE = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupUserData();
        setupRecyclerView();
        setupClickListeners();

        loadChatMessages();
        setupFirebaseMessaging();
        chatMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String senderId = intent.getStringExtra("senderId");
                String message = intent.getStringExtra("message");
                String messageId = intent.getStringExtra("messageId");
                handleIncomingMessage(senderId, message, messageId);
            }
        };
        registerReceiver(chatMessageReceiver, new IntentFilter("com.example.aqualife.NEW_CHAT_MESSAGE"));

        ivAttach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/pdf"});
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
        });
    }

    private void initViews() {
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessage = findViewById(R.id.et_message);
        ivSend = findViewById(R.id.iv_send);
        ivBack = findViewById(R.id.iv_back);
        tvStoreName = findViewById(R.id.tv_store_name);
        tvStoreStatus = findViewById(R.id.tv_store_status);
        ivAttach = findViewById(R.id.iv_attach);

        chatService = ApiClient.getAuthenticatedClient(this).create(ChatMessageService.class);
    }

    private void setupUserData() {
        sessionManager = new UserSessionManager(this);
        String userIdString = sessionManager.getUserId();
        jwtToken = sessionManager.getToken();

        try {
            currentUserId = Integer.parseInt(userIdString);
        } catch (NumberFormatException e) {
            currentUserId = 0;
            Log.e(TAG, "Error parsing user ID", e);
        }

        if (currentUserId == 0 || TextUtils.isEmpty(jwtToken)) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User ID " + currentUserId + " or JWT token is empty");
            finish();
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(this, messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        ChatMessageRequest request = new ChatMessageRequest(receiverId, currentUserId, messageText);

        etMessage.setText("");

        ChatMessage tempMessage = new ChatMessage();
        tempMessage.setChatMessageId(-1);
        tempMessage.setMessage(messageText);
        tempMessage.setSendBy(currentUserId);
        tempMessage.setSendAt(getCurrentTimeString());
        tempMessage.setDeliveryStatus("SENDING");

        messageList.add(tempMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();

        chatService.sendMessage(request).enqueue(new Callback<Response<ChatMessageReponse>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ChatMessageReponse>> call,
                                   @NonNull retrofit2.Response<Response<ChatMessageReponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<ChatMessageReponse> apiResponse = response.body();

                    if (apiResponse.getStatus() == 201 && apiResponse.getData() != null) {
                        updateTempMessage(tempMessage, apiResponse.getData());
                        Log.d(TAG, "Message sent successfully");
                    } else {
                        handleMessageSendError(tempMessage, apiResponse.getMessage());
                    }
                } else {
                    handleMessageSendError(tempMessage, "Failed to send message");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ChatMessageReponse>> call, @NonNull Throwable t) {
                handleMessageSendError(tempMessage, "Network error: " + t.getMessage());
                Log.e(TAG, "Send message failed", t);
            }
        });
    }

    private void updateTempMessage(ChatMessage tempMessage, ChatMessageReponse response) {
        int tempIndex = messageList.indexOf(tempMessage);
        if (tempIndex != -1) {
            tempMessage.setChatMessageId(response.getChatMessageId());
            tempMessage.setMessage(response.getMessage());
            tempMessage.setSendAt(response.getSendAt());
            tempMessage.setDeliveryStatus("SENT");
            chatAdapter.notifyItemChanged(tempIndex);
        }
    }

    private void handleMessageSendError(ChatMessage tempMessage, String error) {
        int tempIndex = messageList.indexOf(tempMessage);
        if (tempIndex != -1) {
            tempMessage.setDeliveryStatus("FAILED");
            chatAdapter.notifyItemChanged(tempIndex);
        }
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private void loadChatMessages() {
        Call<Response<List<ChatMessageReponse>>> call = chatService.getMessages(receiverId, currentUserId);

        call.enqueue(new Callback<Response<List<ChatMessageReponse>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<List<ChatMessageReponse>>> call,
                                   @NonNull retrofit2.Response<Response<List<ChatMessageReponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<ChatMessageReponse>> apiResponse = response.body();

                    if (apiResponse.getStatus() == 200 && apiResponse.getData() != null) {
                        for (ChatMessageReponse chatResponse : apiResponse.getData()) {
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setChatMessageId(chatResponse.getChatMessageId());
                            chatMessage.setMessage(chatResponse.getMessage());
                            chatMessage.setSendBy(chatResponse.getSendBy());
                            chatMessage.setSendAt(chatResponse.getSendAt());
                            chatMessage.setDeliveryStatus("RECEIVED");
                            messageList.add(chatMessage);
                        }
                        chatAdapter.notifyDataSetChanged();
                        scrollToBottom();
                        Log.d(TAG, "Messages loaded successfully");
                    }
                } else {
                    Log.e(TAG, "Failed to load messages: MyFirebaseMessagingService" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<List<ChatMessageReponse>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Load messages failed", t);
                Toast.makeText(ActivityChatMessage.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                FirebaseStorageHelper storageHelper = new FirebaseStorageHelper(this);
                storageHelper.uploadFile(fileUri, new FirebaseStorageHelper.OnImageUploadListener() {
                    @Override
                    public void onUploadStart() {
                        Toast.makeText(ActivityChatMessage.this, "Uploading...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUploadProgress(int progress) {
                        // Optional: show progress bar
                    }

                    @Override
                    public void onUploadSuccess(String downloadUrl) {
                        sendMessageWithFile(downloadUrl);
                    }

                    @Override
                    public void onUploadFailure(String error) {
                        Toast.makeText(ActivityChatMessage.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void sendMessageWithFile(String fileUrl) {
        ChatMessageRequest request = new ChatMessageRequest(receiverId, currentUserId, fileUrl);

        ChatMessage tempMessage = new ChatMessage();
        tempMessage.setChatMessageId(-1);
        tempMessage.setMessage(fileUrl);
        tempMessage.setSendBy(currentUserId);
        tempMessage.setSendAt(getCurrentTimeString());
        tempMessage.setDeliveryStatus("SENDING");

        messageList.add(tempMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();

        chatService.sendMessage(request).enqueue(new Callback<Response<ChatMessageReponse>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ChatMessageReponse>> call,
                                   @NonNull retrofit2.Response<Response<ChatMessageReponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    updateTempMessage(tempMessage, response.body().getData());
                } else {
                    handleMessageSendError(tempMessage, "Failed to send file");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ChatMessageReponse>> call, @NonNull Throwable t) {
                handleMessageSendError(tempMessage, "Upload message failed: " + t.getMessage());
            }
        });
    }



    private void setupFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);
                });
    }

    public void handleIncomingMessage(String senderId, String message, String messageId) {
        runOnUiThread(() -> {
            for (ChatMessage existingMessage : messageList) {
                if (existingMessage.getChatMessageId() == Integer.parseInt(messageId)) {
                    return;
                }
            }

            ChatMessage newMessage = new ChatMessage();
            newMessage.setChatMessageId(Integer.parseInt(messageId));
            newMessage.setMessage(message);
            newMessage.setSendBy(Integer.parseInt(senderId));
            newMessage.setSendAt(getCurrentTimeString());
            newMessage.setDeliveryStatus("RECEIVED");

            messageList.add(newMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatMessageReceiver != null) {
            unregisterReceiver(chatMessageReceiver);
        }
    }
}