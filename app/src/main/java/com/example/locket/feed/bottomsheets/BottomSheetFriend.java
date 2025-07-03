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

import com.example.locket.R;
import com.example.locket.common.repository.viewmodels.FriendViewModel;
import com.example.locket.profile.adapters.NewFriendsAdapter;
import com.example.locket.profile.adapters.ReceivedFriendRequestAdapter;
import com.example.locket.profile.adapters.SentFriendRequestAdapter;
import com.example.locket.profile.adapters.UserSearchAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class BottomSheetFriend extends BottomSheetDialogFragment implements UserSearchAdapter.OnAddFriendClickListener, ReceivedFriendRequestAdapter.OnRequestActionListener, SentFriendRequestAdapter.OnRequestActionListener {

    public interface FriendBottomSheetListener {
        void onSendFriendLinkClicked();
    }

    private FriendBottomSheetListener listener;
    private FriendViewModel friendViewModel;
    private NewFriendsAdapter friendsAdapter;
    private UserSearchAdapter userSearchAdapter;
    private ReceivedFriendRequestAdapter receivedRequestAdapter;
    private SentFriendRequestAdapter sentRequestAdapter;

    private EditText edt_search_friend;
    private RecyclerView rv_friends, rv_search_results, rv_friend_requests_received, rv_friend_requests_sent;
    private TextView txt_number_friends, txt_cancel, txt_friend_requests_title, txt_sent_requests_title;
    private LinearLayout linear_view1, linear_view2, txt_friendlist_title, txt_share_title, send_link_layout;
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
        rv_friend_requests_received = view.findViewById(R.id.rv_friend_requests_received);
        rv_friend_requests_sent = view.findViewById(R.id.rv_friend_requests_sent);
        txt_friend_requests_title = view.findViewById(R.id.txt_friend_requests_title);
        txt_sent_requests_title = view.findViewById(R.id.txt_sent_requests_title);
        txt_friendlist_title = view.findViewById(R.id.txt_friendlist_title);
        txt_share_title = view.findViewById(R.id.txt_share_title);
        send_link_layout = view.findViewById(R.id.send_link_layout);

    }

    private void setupAdapters() {
        rv_friends.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsAdapter = new NewFriendsAdapter(new ArrayList<>(), getActivity(), getContext());
        rv_friends.setAdapter(friendsAdapter);

        rv_search_results.setLayoutManager(new LinearLayoutManager(getContext()));
        userSearchAdapter = new UserSearchAdapter(new ArrayList<>(), this);
        rv_search_results.setAdapter(userSearchAdapter);

        rv_friend_requests_received.setLayoutManager(new LinearLayoutManager(getContext()));
        receivedRequestAdapter = new ReceivedFriendRequestAdapter(new ArrayList<>(), this);
        rv_friend_requests_received.setAdapter(receivedRequestAdapter);

        rv_friend_requests_sent.setLayoutManager(new LinearLayoutManager(getContext()));
        sentRequestAdapter = new SentFriendRequestAdapter(new ArrayList<>(), this);
        rv_friend_requests_sent.setAdapter(sentRequestAdapter);
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
        // Visibility of RecyclerViews is now handled by the TextWatcher
        if (showSearch) {
            edt_search_friend.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edt_search_friend, InputMethodManager.SHOW_IMPLICIT);
        } else {
            edt_search_friend.setText(""); // This will trigger TextWatcher to show rv_friends
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
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    rv_friends.setVisibility(View.VISIBLE);
                    rv_search_results.setVisibility(View.GONE);
                    txt_friendlist_title.setVisibility(View.VISIBLE);
                    txt_share_title.setVisibility(View.VISIBLE);
                    send_link_layout.setVisibility(View.VISIBLE);
                    userSearchAdapter.updateUsers(new ArrayList<>());
                } else {
                    rv_friends.setVisibility(View.GONE);
                    txt_friendlist_title.setVisibility(View.GONE);
                    txt_share_title.setVisibility(View.GONE);
                    send_link_layout.setVisibility(View.GONE);
                    rv_search_results.setVisibility(View.VISIBLE);
                    if (query.length() > 2) {
                        friendViewModel.searchUsers(query);
                    } else {
                        userSearchAdapter.updateUsers(new ArrayList<>()); // Clear if query is too short
                    }
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
        // Fetch initial data
        friendViewModel.fetchReceivedFriendRequests();
        friendViewModel.fetchSentFriendRequests();

        // Observer for received friend requests
        friendViewModel.receivedFriendRequests.observe(getViewLifecycleOwner(), response -> {
            boolean hasData = response != null && response.getData() != null && !response.getData().isEmpty();
            txt_friend_requests_title.setVisibility(hasData ? View.VISIBLE : View.GONE);
            rv_friend_requests_received.setVisibility(hasData ? View.VISIBLE : View.GONE);
            if (hasData) {
                receivedRequestAdapter.updateRequests(response.getData());
            }
        });

        // Observer for sent friend requests
        friendViewModel.sentFriendRequests.observe(getViewLifecycleOwner(), response -> {
            boolean hasData = response != null && response.getData() != null && !response.getData().isEmpty();
            txt_sent_requests_title.setVisibility(hasData ? View.VISIBLE : View.GONE);
            rv_friend_requests_sent.setVisibility(hasData ? View.VISIBLE : View.GONE);
            if (hasData) {
                sentRequestAdapter.updateRequests(response.getData());
            }
        });

        // Observer for current friends list
        friendViewModel.myFriends.observe(getViewLifecycleOwner(), friends -> {
            if (friends != null && friends.getData() != null) {
                friendsAdapter.updateFriendsList(friends.getData());
                updateFriendsCount(friends.getData().size());
            }
        });

        // Observer for search results
        friendViewModel.userSearchResults.observe(getViewLifecycleOwner(), searchResults -> {
            if (searchResults != null && searchResults.getUsers() != null) {
                userSearchAdapter.updateUsers(searchResults.getUsers());
            } else {
                userSearchAdapter.updateUsers(new ArrayList<>()); // Clear list on null response
            }
        });

        // Observer for actions like decline, cancel
        friendViewModel.actionResponse.observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getMessage() != null) {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                // Refresh all lists
                friendViewModel.fetchMyFriends();
                friendViewModel.fetchReceivedFriendRequests();
                friendViewModel.fetchSentFriendRequests();
            }
        });

        // This observer handles both sending a new request and accepting one
        friendViewModel.friendshipResponse.observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getMessage() != null) {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
                // Refresh all lists
                friendViewModel.fetchMyFriends();
                friendViewModel.fetchReceivedFriendRequests();
                friendViewModel.fetchSentFriendRequests();
                // If search was active (i.e., we just sent a request), close it.
                if (linear_view2.getVisibility() == View.VISIBLE) {
                    toggleSearchView(false);
                }
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

    @Override
    public void onAccept(String friendshipId) {
        friendViewModel.acceptFriendRequest(friendshipId);
    }

    @Override
    public void onDecline(String friendshipId) {
        friendViewModel.declineFriendRequest(friendshipId);
    }

    @Override
    public void onCancel(String friendshipId) {
        friendViewModel.cancelFriendRequest(friendshipId);
    }
}


