package com.example.locket.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.chat.model.ChatMessage;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton attachButton;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private DatabaseReference databaseReference;
    private String currentUserId;
    private String receiverId;
    private String receiverName;
    private String chatRoomId;
    private StorageReference storageReference;
    private ActivityResultLauncher<String> mediaPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Lấy dữ liệu từ Intent
        receiverId = getIntent().getStringExtra(com.example.locket.friends.FriendsListActivity.EXTRA_FRIEND_ID);
        receiverName = getIntent().getStringExtra(com.example.locket.friends.FriendsListActivity.EXTRA_FRIEND_NAME);

        // Lấy ID người dùng hiện tại từ SharedPreferences
        currentUserId = SharedPreferencesUser.getLoginResponse(this).getLocalId();

        if (receiverId == null || currentUserId == null) {
            Log.e(TAG, "User IDs are missing. Cannot start chat.");
            finish(); // Đóng activity nếu không có đủ thông tin
            return;
        }

        chatRoomId = getChatRoomId(currentUserId, receiverId);

        // Khởi tạo Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference().child("chat_media");

        // Khởi tạo ActivityResultLauncher để chọn media
        mediaPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadMedia(uri);
                    }
                });

        initViews();
        setupRecyclerView();
        setupFirebase();
        setupSendButton();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        attachButton = findViewById(R.id.attachButton);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(receiverName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Giúp tin nhắn mới luôn ở dưới
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupFirebase() {
        // Sử dụng đúng URL của database
        databaseReference = FirebaseDatabase.getInstance("https://picket-se104-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("chat_rooms").child(chatRoomId);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                if (chatMessage != null) {
                    chatMessages.add(chatMessage);
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> sendMessage());

        attachButton.setOnClickListener(v -> openGallery());
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Người nhận đã được lấy từ Intent

        ChatMessage message = new ChatMessage(messageText, currentUserId, receiverId, System.currentTimeMillis());

        databaseReference.push().setValue(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Message sent successfully!");
                    messageEditText.setText(""); // Xóa nội dung EditText sau khi gửi
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send message.", e));
    }

    private String getChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) > 0) {
            return userId2 + "_" + userId1;
        } else {
            return userId1 + "_" + userId2;
        }
    }

    private void openGallery() {
        // Mở thư viện để chọn cả ảnh và video
        mediaPickerLauncher.launch("*/*");
    }

    private void uploadMedia(Uri fileUri) {
        // Tạo tên file ngẫu nhiên
        final StorageReference fileRef = storageReference.child(chatRoomId + "/" + UUID.randomUUID().toString());
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    // Xác định đây là ảnh hay video dựa trên kiểu MIME (cần cải tiến thêm)
                    String mimeType = getContentResolver().getType(fileUri);
                    ChatMessage.MessageType type = (mimeType != null && mimeType.startsWith("video")) ? ChatMessage.MessageType.VIDEO : ChatMessage.MessageType.IMAGE;
                    
                    sendMediaMessage(downloadUrl, type);
                    Toast.makeText(ChatActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upload failed: " + e.getMessage());
                    Toast.makeText(ChatActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendMediaMessage(String mediaUrl, ChatMessage.MessageType type) {
        ChatMessage message = new ChatMessage(currentUserId, receiverId, System.currentTimeMillis(), type, mediaUrl);
        databaseReference.push().setValue(message)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Media message sent successfully."))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send media message.", e));
    }
}
