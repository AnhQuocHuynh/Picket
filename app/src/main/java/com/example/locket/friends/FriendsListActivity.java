package com.example.locket.friends;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.example.locket.R;
import com.example.locket.chat.ChatActivity;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.example.locket.common.repository.FriendshipRepository;
import com.example.locket.common.models.friendship.FriendsListResponse.FriendData;
import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity implements FriendsListAdapter.OnFriendClickListener {

    private static final String TAG = "FriendsListActivity";
    public static final String EXTRA_FRIEND_ID = "FRIEND_ID";
    public static final String EXTRA_FRIEND_NAME = "FRIEND_NAME";

    private RecyclerView friendsRecyclerView;
    private FriendsListAdapter adapter;
    private List<FriendData> friendList;
    private FriendshipRepository friendshipRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        friendshipRepository = new FriendshipRepository(this);

        initViews();
        setupRecyclerView();
        loadFriends();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
    }

    private void setupRecyclerView() {
        friendList = new ArrayList<>();
        adapter = new FriendsListAdapter(friendList, this);
        friendsRecyclerView.setAdapter(adapter);
    }

    private void loadFriends() {
        friendshipRepository.getFriendsList(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse response) {
                if (response != null && response.getData() != null) {
                    friendList.clear();
                    friendList.addAll(response.getData());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Successfully loaded " + response.getData().size() + " friends.");
                } else {
                    Log.e(TAG, "Friend list response is null or empty.");
                    Toast.makeText(FriendsListActivity.this, "Could not load friends", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message, int code) {
                Log.e(TAG, "Error loading friends: " + message + " (code: " + code + ")");
                Toast.makeText(FriendsListActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoading(boolean isLoading) {
                // You can show a progress bar here if needed
                Log.d(TAG, "Loading friends: " + isLoading);
            }
        });
    }

    @Override
    public void onFriendClick(FriendData friend) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(EXTRA_FRIEND_ID, friend.getId());
        intent.putExtra(EXTRA_FRIEND_NAME, friend.getUsername());
        startActivity(intent);
    }
}
