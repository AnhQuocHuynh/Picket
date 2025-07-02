package com.example.locket.feed.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.locket.R;
import com.example.locket.common.repository.viewmodels.FriendViewModel;
import com.example.locket.profile.adapters.NewFriendsAdapter;
import com.example.locket.profile.adapters.UserSearchAdapter;

import java.util.ArrayList;

public class BottomSheetFriend extends BottomSheetDialogFragment implements UserSearchAdapter.OnAddFriendClickListener {

    public interface FriendBottomSheetListener {
        void onSendFriendLinkClicked();
    }

    private FriendBottomSheetListener listener;
    private FriendViewModel friendViewModel;
    private NewFriendsAdapter friendsAdapter;
    private UserSearchAdapter userSearchAdapter;

    private EditText edt_search_friend;
    private RecyclerView rv_friends, rv_search_results;
    private TextView txt_number_friends, txt_cancel;
    private LinearLayout linear_view1, linear_view2;
    private RelativeLayout relative_send_friend_link;

    public void setFriendBottomSheetListener(FriendBottomSheetListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendViewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_bottom_sheet_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupAdapters();
        setupClickListeners();
        setupSearch();
        observeViewModel();
    }

    private void initViews(View view) {
        linear_view1 = view.findViewById(R.id.linear_view1);
        linear_view2 = view.findViewById(R.id.linear_view2);
        edt_search_friend = view.findViewById(R.id.edt_search_friend);
        txt_cancel = view.findViewById(R.id.txt_cancel);
        txt_number_friends = view.findViewById(R.id.txt_number_friends);
        rv_friends = view.findViewById(R.id.rv_friends);
        relative_send_friend_link = view.findViewById(R.id.relative_send_friend_link);
        rv_search_results = view.findViewById(R.id.rv_search_results);
    }

    private void setupAdapters() {
        rv_friends.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsAdapter = new NewFriendsAdapter(new ArrayList<>(), getActivity(), getContext());
        rv_friends.setAdapter(friendsAdapter);

        rv_search_results.setLayoutManager(new LinearLayoutManager(getContext()));
        userSearchAdapter = new UserSearchAdapter(new ArrayList<>(), this);
        rv_search_results.setAdapter(userSearchAdapter);
    }

    private void setupClickListeners() {
        linear_view1.setOnClickListener(v -> toggleSearchView(true));
        txt_cancel.setOnClickListener(v -> toggleSearchView(false));
        relative_send_friend_link.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onSendFriendLinkClicked();
            }
        });
    }

    private void toggleSearchView(boolean showSearch) {
        linear_view1.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        linear_view2.setVisibility(showSearch ? View.VISIBLE : View.GONE);
        rv_friends.setVisibility(showSearch ? View.GONE : View.VISIBLE);
        rv_search_results.setVisibility(showSearch ? View.VISIBLE : View.GONE);
        if (showSearch) {
            edt_search_friend.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edt_search_friend, InputMethodManager.SHOW_IMPLICIT);
        } else {
            edt_search_friend.setText("");
            userSearchAdapter.updateUsers(new ArrayList<>()); // Clear search results
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edt_search_friend.getWindowToken(), 0);
        }
    }

    private void setupSearch() {
        edt_search_friend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 2) {
                    friendViewModel.searchUsers(s.toString().trim());
                } else {
                    userSearchAdapter.updateUsers(new ArrayList<>()); // Clear if query is too short
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    private void observeViewModel() {
        friendViewModel.myFriends.observe(getViewLifecycleOwner(), friends -> {
            if (friends != null && friends.getData() != null) {
                friendsAdapter.updateFriendsList(friends.getData());
                updateFriendsCount(friends.getData().size());
            }
        });

        friendViewModel.userSearchResults.observe(getViewLifecycleOwner(), searchResults -> {
            if (searchResults != null && searchResults.getUsers() != null) {
                userSearchAdapter.updateUsers(searchResults.getUsers());
            } else {
                userSearchAdapter.updateUsers(new ArrayList<>()); // Clear list on null response
            }
        });

        friendViewModel.friendshipResponse.observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getMessage() != null) {
                Toast.makeText(getContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
                // After a successful friend request, clear the search and go back
                toggleSearchView(false);
            }
        });

        friendViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateFriendsCount(int count) {
        txt_number_friends.setText(count + " / 20 friends added");
    }

    @Override
    public void onAddFriendClick(String userId) {
        friendViewModel.sendFriendRequest(userId);
    }
}


