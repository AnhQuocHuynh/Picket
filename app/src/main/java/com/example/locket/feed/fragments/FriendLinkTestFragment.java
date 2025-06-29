package com.example.locket.feed.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.locket.MainActivity;
import com.example.locket.R;
import com.example.locket.common.repository.FriendshipRepository;
import com.example.locket.common.models.friendship.FriendshipResponse;
import com.example.locket.common.models.friendship.GenerateLinkResponse;

public class FriendLinkTestFragment extends Fragment {
    private FriendshipRepository friendshipRepository;
    private EditText etTokenInput;
    private TextView tvGeneratedToken;
    private Button btnGenerateLink, btnAcceptLink;
    private ImageView btnBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendshipRepository = new FriendshipRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_link_test, container, false);
        initViews(view);
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etTokenInput = view.findViewById(R.id.et_token_input);
        tvGeneratedToken = view.findViewById(R.id.tv_generated_token);
        btnGenerateLink = view.findViewById(R.id.btn_generate_link);
        btnAcceptLink = view.findViewById(R.id.btn_accept_link);
        btnBack = view.findViewById(R.id.btn_back);

        // Auto select all text when focused for easy copy/paste
        etTokenInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !etTokenInput.getText().toString().isEmpty()) {
                etTokenInput.selectAll();
            }
        });

        // TextView is already selectable via android:textIsSelectable="true" in XML
    }

    private void setupClickListeners() {
        btnGenerateLink.setOnClickListener(v -> generateFriendLink());
        btnAcceptLink.setOnClickListener(v -> acceptFriendLink());
        btnBack.setOnClickListener(v -> navigateBack());
    }

    /**
     * 🔙 Navigate back to previous screen
     */
    private void navigateBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    /**
     * 🔗 Generate friend invite link
     */
    private void generateFriendLink() {
        btnGenerateLink.setEnabled(false);
        btnGenerateLink.setText("Đang tạo...");

        friendshipRepository.generateFriendLink(new FriendshipRepository.LinkCallback() {
            @Override
            public void onSuccess(GenerateLinkResponse linkResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnGenerateLink.setEnabled(true);
                        btnGenerateLink.setText("Tạo Link Mời");

                        if (linkResponse != null && linkResponse.getLink() != null) {
                            String link = linkResponse.getLink();
                            
                            // Extract token from full link
                            String token = extractTokenFromLink(link);

                            // Display token in TextView
                            tvGeneratedToken.setText(token);
                            tvGeneratedToken.setVisibility(View.VISIBLE);

                            // Copy token to clipboard (not full link)
                            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Friend Token", token);
                            clipboard.setPrimaryClip(clip);

                            Toast.makeText(getContext(), "Đã lưu link của bạn!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String message, int code) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnGenerateLink.setEnabled(true);
                        btnGenerateLink.setText("Tạo Link Mời");
                        
                        String errorMsg = message != null ? message : "Có lỗi xảy ra khi tạo link";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    /**
     * 🎯 Accept friend invite via token or link
     */
    private void acceptFriendLink() {
        String input = etTokenInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập link hoặc token", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract token from input (whether it's a full link or just token)
        String token = extractTokenFromInput(input);
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Link hoặc token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAcceptLink.setEnabled(false);
        btnAcceptLink.setText("Đang xử lý...");

        // Use FriendshipRepository directly for better control
        friendshipRepository.acceptFriendViaLink(token, new FriendshipRepository.FriendshipCallback() {
            @Override
            public void onSuccess(FriendshipResponse friendshipResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnAcceptLink.setEnabled(true);
                        btnAcceptLink.setText("Chấp Nhận Lời Mời");
                        
                        String message = "Kết bạn thành công!";
                        if (friendshipResponse != null && friendshipResponse.getMessage() != null) {
                            message = friendshipResponse.getMessage();
                        }
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        
                        // Notify MainActivity about friends list update
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).notifyFriendsListUpdated();
                        }
                        
                        // Clear the token input
                        etTokenInput.setText("");
                    });
                }
            }

            @Override
            public void onError(String message, int code) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        btnAcceptLink.setEnabled(true);
                        btnAcceptLink.setText("Chấp Nhận Lời Mời");
                        
                        String errorMsg = message != null ? message : "Có lỗi xảy ra khi kết bạn";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onLoading(boolean isLoading) {
                // Loading handled by button state
            }
        });
    }

    /**
     * Extract token from friend link URL
     */
    private String extractTokenFromLink(String link) {
        if (link == null) return "";
        
        // Extract token from URL pattern: .../add-friend/TOKEN
        int lastSlashIndex = link.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < link.length() - 1) {
            return link.substring(lastSlashIndex + 1);
        }
        return "";
    }

    /**
     * Extract token from input (can be full link or just token)
     */
    private String extractTokenFromInput(String input) {
        if (input == null || input.isEmpty()) return "";
        
        // If input contains URL pattern, extract token from it
        if (input.contains("/add-friend/")) {
            return extractTokenFromLink(input);
        }
        
        // If input looks like a token (alphanumeric string), return as is
        if (input.matches("^[a-zA-Z0-9]+$")) {
            return input;
        }
        
        // If input is just the token part without URL, return it
        // Remove any extra characters that might be accidentally copied
        String cleanInput = input.replaceAll("[^a-zA-Z0-9]", "");
        return cleanInput.isEmpty() ? "" : cleanInput;
    }
} 