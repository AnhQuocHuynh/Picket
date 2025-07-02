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
import androidx.lifecycle.ViewModelProvider;

import com.example.locket.R;
import com.example.locket.common.repository.viewmodels.FriendViewModel;

public class FriendLinkTestFragment extends Fragment {
    private FriendViewModel friendViewModel;
    private EditText etTokenInput;
    private TextView tvGeneratedToken;
    private Button btnGenerateLink, btnAcceptLink;
    private ImageView btnBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_link_test, container, false);
        initViews(view);
        setupClickListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
    }

    private void initViews(View view) {
        etTokenInput = view.findViewById(R.id.et_token_input);
        tvGeneratedToken = view.findViewById(R.id.tv_generated_token);
        btnGenerateLink = view.findViewById(R.id.btn_generate_link);
        btnAcceptLink = view.findViewById(R.id.btn_accept_link);
        btnBack = view.findViewById(R.id.btn_back);

        etTokenInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !etTokenInput.getText().toString().isEmpty()) {
                etTokenInput.selectAll();
            }
        });
    }

    private void setupClickListeners() {
        btnGenerateLink.setOnClickListener(v -> generateFriendLink());
        btnAcceptLink.setOnClickListener(v -> acceptFriendLink());
        btnBack.setOnClickListener(v -> navigateBack());
    }

    private void observeViewModel() {
        friendViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = (isLoading != null && isLoading);
            btnGenerateLink.setEnabled(!loading);
            btnAcceptLink.setEnabled(!loading);
            btnGenerateLink.setText(loading ? "Đang tạo..." : "Tạo Link Mời");
            btnAcceptLink.setText(loading ? "Đang xử lý..." : "Chấp Nhận");
        });

        friendViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        friendViewModel.generateLinkResponse.observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getLink() != null) {
                String token = extractTokenFromLink(response.getLink());
                tvGeneratedToken.setText(token);
                tvGeneratedToken.setVisibility(View.VISIBLE);

                if (getContext() != null) {
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Friend Token", token);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "Đã sao chép token vào bộ nhớ tạm!", Toast.LENGTH_LONG).show();
                }
            }
        });

        friendViewModel.friendshipResponse.observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                String message = response.getMessage() != null ? response.getMessage() : "Thao tác thành công!";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                etTokenInput.setText("");
                // Optionally, navigate back or refresh UI
                // navigateBack();
            }
        });
    }

    private void navigateBack() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private void generateFriendLink() {
        friendViewModel.generateFriendLink();
    }

    private void acceptFriendLink() {
        String input = etTokenInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập link hoặc token", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = extractTokenFromInput(input);
        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Link hoặc token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        friendViewModel.acceptFriendViaLink(token);
    }

    private String extractTokenFromLink(String link) {
        if (link == null) return "";
        // Try extracting from "token=" pattern first
        if (link.contains("token=")) {
            return link.substring(link.indexOf("token=") + 6);
        }
        // Fallback to extracting from URL path like ".../add-friend/TOKEN"
        int lastSlashIndex = link.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < link.length() - 1) {
            return link.substring(lastSlashIndex + 1);
        }
        return "";
    }

    private String extractTokenFromInput(String input) {
        if (input == null || input.isEmpty()) return "";
        
        // If input contains a URL pattern, extract the token from it
        if (input.contains("/add-friend/") || input.contains("token=")) {
            return extractTokenFromLink(input);
        }
        
        // Otherwise, assume the input is the token itself
        return input;
    }
}