package com.example.locket.feed.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.locket.R;
import com.example.locket.profile.adapters.NewFriendsAdapter;
import com.example.locket.common.repository.FriendshipRepository;
import com.example.locket.common.models.friendship.FriendsListResponse;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetFriend extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;

    private FriendshipRepository friendshipRepository;
    private List<FriendsListResponse.FriendData> friendList;
    private NewFriendsAdapter friendsAdapter;

    private LinearLayout linear_view1, linear_view2;
    private TextView txt_cancel;
    private TextView txt_number_friends;
    private EditText edt_search_friend;
    private RecyclerView rv_friends;

    public BottomSheetFriend(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme; // Áp dụng theme tùy chỉnh
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.item_bottom_sheet_friend, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Initialize repository and data
        friendshipRepository = new FriendshipRepository(context);
        friendList = new ArrayList<>();

        initViews(bottomSheetDialog);
        setAdapters();
        onClick();
        loadFriendsList();

        return bottomSheetDialog;
    }

    private void initViews(BottomSheetDialog bottomSheetDialog) {
        linear_view1 = bottomSheetDialog.findViewById(R.id.linear_view1);
        linear_view2 = bottomSheetDialog.findViewById(R.id.linear_view2);
        edt_search_friend = bottomSheetDialog.findViewById(R.id.edt_search_friend);
        txt_cancel = bottomSheetDialog.findViewById(R.id.txt_cancel);
        txt_number_friends = bottomSheetDialog.findViewById(R.id.txt_number_friends);
        rv_friends = bottomSheetDialog.findViewById(R.id.rv_friends);
    }

    private void setAdapters() {
        rv_friends.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        friendsAdapter = new NewFriendsAdapter(friendList, requireActivity(), requireContext());
        rv_friends.setAdapter(friendsAdapter);
    }

    private void onClick() {
        linear_view1.setOnClickListener(view -> {
            linear_view1.setVisibility(View.GONE);
            linear_view2.setVisibility(View.VISIBLE);
            edt_search_friend.requestFocus();

            requireActivity().getWindow().getDecorView().post(() -> {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edt_search_friend, InputMethodManager.SHOW_IMPLICIT);
            });
        });
        
        txt_cancel.setOnClickListener(view -> {
            linear_view1.setVisibility(View.VISIBLE);
            linear_view2.setVisibility(View.GONE);
        });
    }

    /**
     * 👥 Load friends list from API
     */
    private void loadFriendsList() {
        friendshipRepository.getFriendsList(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse friendsListResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (friendsListResponse != null && friendsListResponse.getData() != null) {
                            friendList.clear();
                            friendList.addAll(friendsListResponse.getData());
                            friendsAdapter.updateFriendsList(friendList);
                            updateFriendsCount();
                            Log.d("BottomSheetFriend", "✅ Friends list loaded: " + friendList.size() + " friends");
                        } else {
                            Log.w("BottomSheetFriend", "⚠️ Empty friends list response");
                            updateFriendsCount();
                        }
                    });
                }
            }

            @Override
            public void onError(String message, int code) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e("BottomSheetFriend", "❌ Failed to load friends: " + message);
                        updateFriendsCount();
                        // Optionally show error message to user
                    });
                }
            }

            @Override
            public void onLoading(boolean isLoading) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isLoading) {
                            Log.d("BottomSheetFriend", "🔄 Loading friends list...");
                        }
                    });
                }
            }
        });
    }

    /**
     * 🔄 Refresh friends list (call this after successful friend operations)
     */
    public void refreshFriendsList() {
        Log.d("BottomSheetFriend", "🔄 Refreshing friends list...");
        loadFriendsList();
    }

    @SuppressLint("SetTextI18n")
    private void updateFriendsCount() {
        int friendsCount = friendList.size();
        txt_number_friends.setText(friendsCount + " / 20 người bạn đã được bổ sung");
    }
}

