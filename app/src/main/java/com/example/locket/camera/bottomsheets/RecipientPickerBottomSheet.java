package com.example.locket.camera.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.camera.adapters.RecipientSelectionAdapter;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class RecipientPickerBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_ALL_FRIENDS = "all_friends";
    private static final String ARG_SELECTED_FRIENDS = "selected_friends";

    // UI Components
    private TextView txt_title;
    private EditText edt_search;
    private LinearLayout linear_search_view;
    private RecyclerView rv_friends;
    private MaterialButton btn_send;
    private TextView txt_selected_count;

    // Data
    private ArrayList<FriendsListResponse.FriendData> allFriends = new ArrayList<>();
    private ArrayList<FriendsListResponse.FriendData> selectedFriends = new ArrayList<>();
    private ArrayList<FriendsListResponse.FriendData> filteredFriends = new ArrayList<>();

    // Components
    private RecipientSelectionAdapter adapter;
    private OnRecipientsSelectedListener listener;

    public interface OnRecipientsSelectedListener {
        void onRecipientsSelected(List<FriendsListResponse.FriendData> recipients);
    }

    public static RecipientPickerBottomSheet newInstance(
            ArrayList<FriendsListResponse.FriendData> allFriends,
            ArrayList<FriendsListResponse.FriendData> selectedFriends) {
        RecipientPickerBottomSheet fragment = new RecipientPickerBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALL_FRIENDS, allFriends);
        args.putSerializable(ARG_SELECTED_FRIENDS, selectedFriends);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnRecipientsSelectedListener(OnRecipientsSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            allFriends = (ArrayList<FriendsListResponse.FriendData>) getArguments().getSerializable(ARG_ALL_FRIENDS);
            selectedFriends = (ArrayList<FriendsListResponse.FriendData>) getArguments().getSerializable(ARG_SELECTED_FRIENDS);
            if (allFriends == null) allFriends = new ArrayList<>();
            if (selectedFriends == null) selectedFriends = new ArrayList<>();
        }

        // Filter out current user (allFriends should already be filtered from PhotoPreviewFragment)
        filteredFriends.addAll(allFriends);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheetInternal).setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_recipient_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        setupSearchWatcher();
        updateSelectedCount();
    }

    private void initViews(View view) {
        txt_title = view.findViewById(R.id.txt_title);
        edt_search = view.findViewById(R.id.edt_search);
        linear_search_view = view.findViewById(R.id.linear_search_view);
        rv_friends = view.findViewById(R.id.rv_friends);
        btn_send = view.findViewById(R.id.btn_send);
        txt_selected_count = view.findViewById(R.id.txt_selected_count);
    }

    private void setupRecyclerView() {
        adapter = new RecipientSelectionAdapter(requireContext(), filteredFriends, selectedFriends);
        adapter.setOnSelectionChangedListener(new RecipientSelectionAdapter.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(FriendsListResponse.FriendData friend, boolean isSelected) {
                if (isSelected) {
                    if (!selectedFriends.contains(friend)) {
                        selectedFriends.add(friend);
                    }
                } else {
                    selectedFriends.remove(friend);
                }
                updateSelectedCount();
            }
        });

        rv_friends.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv_friends.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btn_send.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipientsSelected(new ArrayList<>(selectedFriends));
            }
            dismiss();
        });

        // Handle search box expansion
        linear_search_view.setOnClickListener(v -> {
            edt_search.requestFocus();
            // You could add keyboard opening logic here
        });
    }

    private void setupSearchWatcher() {
        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterFriends(s.toString().trim());
            }
        });
    }

    private void filterFriends(String query) {
        filteredFriends.clear();

        if (query.isEmpty()) {
            filteredFriends.addAll(allFriends);
        } else {
            String lowerQuery = query.toLowerCase();
            for (FriendsListResponse.FriendData friend : allFriends) {
                String displayName = friend.getDisplayName();
                String username = friend.getUsername();

                if ((displayName != null && displayName.toLowerCase().contains(lowerQuery)) ||
                        (username != null && username.toLowerCase().contains(lowerQuery))) {
                    filteredFriends.add(friend);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void updateSelectedCount() {
        int count = selectedFriends.size();
        if (count == 0) {
            txt_selected_count.setText("Chưa chọn ai");
            btn_send.setText("Send to All");
        } else {
            txt_selected_count.setText("Đã chọn " + count + " người");
            btn_send.setText("Send to " + count);
        }
    }
} 