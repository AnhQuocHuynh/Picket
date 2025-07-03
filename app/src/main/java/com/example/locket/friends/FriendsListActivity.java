package com.example.locket.friends;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.example.locket.R;
import com.example.locket.chat.ChatActivity;
import com.example.locket.common.repository.FriendshipRepository;
//import com.example.locket.search.SearchUserActivity;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.example.locket.chat.model.ChatMessage;
import com.example.locket.common.models.friendship.FriendsListResponse.FriendData;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.utils.SharedPreferencesUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity implements FriendsListAdapter.OnFriendClickListener {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friends_list_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (friendsListAdapter != null) {
                    friendsListAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    private static final String TAG = "FriendsListActivity";
    public static final String EXTRA_FRIEND_ID = "FRIEND_ID";
    public static final String EXTRA_FRIEND_NAME = "FRIEND_NAME";

    private RecyclerView friendsRecyclerView;
    private FriendsListAdapter friendsListAdapter;
    private List<FriendData> friendsList;
    private DatabaseReference databaseReference;
    private FriendshipRepository friendshipRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        databaseReference = FirebaseDatabase.getInstance("https://picket-se104-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();


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
        getSupportActionBar().setTitle("Trò chuyện");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
    }

    private void setupRecyclerView() {
        friendsList = new ArrayList<>();
        friendsListAdapter = new FriendsListAdapter(friendsList, this);
        friendsRecyclerView.setAdapter(friendsListAdapter);
    }

    private void loadFriends() {
        friendshipRepository.getFriendsList(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse response) {
                if (response != null && response.getData() != null) {
                    friendsList.clear();
                    friendsList.addAll(response.getData());
                    friendsListAdapter.notifyDataSetChanged();
                    fetchLastMessages();
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

    private void fetchLastMessages() {
        LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(this);
        if (loginResponse == null || loginResponse.getUser() == null || loginResponse.getUser().getId() == null) {
            Log.e(TAG, "Cannot fetch last messages, user is not logged in or user ID is missing.");
            return;
        }
        String currentUserId = loginResponse.getUser().getId();
        for (FriendData friend : friendsList) {
            String friendId = friend.getId();

            String chatRoomId = getChatRoomId(currentUserId, friendId);
            DatabaseReference messagesRef = databaseReference.child("chats").child(chatRoomId).child("messages");

            Query lastMessageQuery = messagesRef.orderByKey().limitToLast(1);

            lastMessageQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            ChatMessage lastMessage = messageSnapshot.getValue(ChatMessage.class);
                            if (lastMessage != null) {
                                friend.setLastMessage(lastMessage.isMedia() ? "[Hình ảnh]" : lastMessage.getText());
                                friend.setLastMessageTimestamp(lastMessage.getTimestamp());
                                boolean isUnread = !lastMessage.getSenderId().equals(currentUserId) && !lastMessage.isSeen();
                                friend.setLastMessageUnread(isUnread);
                            }
                        }
                    } else {
                        friend.setLastMessage("Bắt đầu trò chuyện!");
                        friend.setLastMessageTimestamp(0);
                        friend.setLastMessageUnread(false);
                    }
                    sortAndNotify();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to read last message for friend " + friend.getId(), error.toException());
                }
            });
        }
    }

    private synchronized void sortAndNotify() {
        Collections.sort(friendsList, (o1, o2) -> Long.compare(o2.getLastMessageTimestamp(), o1.getLastMessageTimestamp()));
        if (friendsListAdapter != null) {
            friendsListAdapter.updateFullList(friendsList);
            friendsListAdapter.notifyDataSetChanged();
        }
    }

    private String getChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) > 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    @Override
    public void onFriendClick(FriendData friend) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(EXTRA_FRIEND_ID, friend.getId());
        intent.putExtra(EXTRA_FRIEND_NAME, friend.getUsername());
        startActivity(intent);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.friends_list_menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.action_search) {
//            startActivity(new Intent(FriendsListActivity.this, SearchUserActivity.class));
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
