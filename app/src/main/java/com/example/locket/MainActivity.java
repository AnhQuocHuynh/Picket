package com.example.locket;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import com.example.locket.feed.fragments.HomeFragment;
import com.example.locket.auth.fragments.LoginOrRegisterFragment;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.database.DataSyncWorker;
import com.example.locket.common.repository.FriendshipRepository;
import com.example.locket.common.models.friendship.FriendshipResponse;
import com.example.locket.feed.bottomsheets.BottomSheetFriend;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private FriendshipRepository friendshipRepository;
    
    // Interface for notifying fragments about friend list updates
    public interface FriendsListUpdateListener {
        void onFriendsListUpdated();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize friendship repository
        friendshipRepository = new FriendshipRepository(this);

        // eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeạo PeriodicWorkRequest để chạy công việc mỗi 15 phút
        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(DataSyncWorker.class, 15, TimeUnit.MINUTES)
                .build();

        // Đăng ký công việc với WorkManager
        WorkManager.getInstance(this).enqueue(syncWorkRequest);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, SharedPreferencesUser.isLoggedIn(this) ? new HomeFragment() : new LoginOrRegisterFragment())
                    .commit();
        }

        // Handle deep linking intent
        handleDeepLinkIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLinkIntent(intent);
    }

    /**
     * 🔗 Handle deep linking for friend invite links
     */
    private void handleDeepLinkIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }

        Uri uri = intent.getData();
        if (uri == null) {
            return;
        }

        // Check if this is a friend invite link
        String path = uri.getPath();
        if (path != null && path.startsWith("/add-friend/")) {
            // Extract token from path
            String token = path.substring("/add-friend/".length());
            if (!token.isEmpty()) {
                handleFriendInviteLink(token);
            }
        }
    }

    /**
     * 🎯 Handle friend invite link with token
     */
    private void handleFriendInviteLink(String token) {
        // Check if user is logged in
        if (!SharedPreferencesUser.isLoggedIn(this)) {
            // Save token to handle after login
            SharedPreferencesUser.savePendingFriendToken(this, token);
            Toast.makeText(this, "Vui lòng đăng nhập để kết bạn", Toast.LENGTH_LONG).show();
            return;
        }

        // Process friend invite
        processFriendInvite(token);
    }

    /**
     * 📝 Process friend invite with token
     */
    public void processFriendInvite(String token) {
        friendshipRepository.acceptFriendViaLink(token, new FriendshipRepository.FriendshipCallback() {
            @Override
            public void onSuccess(FriendshipResponse friendshipResponse) {
                runOnUiThread(() -> {
                    if (friendshipResponse != null && friendshipResponse.getMessage() != null) {
                        Toast.makeText(MainActivity.this, friendshipResponse.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Kết bạn thành công!", Toast.LENGTH_LONG).show();
                    }
                    
                    // Notify fragments about friends list update
                    notifyFriendsListUpdated();
                    
                    // Navigate to home to refresh friends list
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                    if (!(currentFragment instanceof HomeFragment)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame_layout, new HomeFragment())
                                .commit();
                    }
                });
            }

            @Override
            public void onError(String message, int code) {
                runOnUiThread(() -> {
                    String errorMsg = message != null ? message : "Có lỗi xảy ra khi kết bạn";
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onLoading(boolean isLoading) {
                // Show loading indicator if needed
                runOnUiThread(() -> {
                    if (isLoading) {
                        Toast.makeText(MainActivity.this, "Đang xử lý...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 🔄 Notify all fragments about friends list update
     */
    public void notifyFriendsListUpdated() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (currentFragment instanceof FriendsListUpdateListener) {
            ((FriendsListUpdateListener) currentFragment).onFriendsListUpdated();
        }
        
        // Also notify any open BottomSheetFriend dialogs
//        getSupportFragmentManager().getFragments().forEach(fragment -> {
//            if (fragment instanceof BottomSheetFriend) {
//                ((BottomSheetFriend) fragment).refreshFriendsList();
//            }
//        });
    }

    /**
     * 🔄 Check and process pending friend token after successful login
     */
    public void checkPendingFriendToken() {
        String pendingToken = SharedPreferencesUser.getPendingFriendToken(this);
        if (pendingToken != null && !pendingToken.isEmpty()) {
            // Clear the pending token first
            SharedPreferencesUser.clearPendingFriendToken(this);
            
            // Process the friend invite
            Toast.makeText(this, "Đang xử lý lời mời kết bạn...", Toast.LENGTH_SHORT).show();
            processFriendInvite(pendingToken);
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
        );

        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void replaceFragmentWithBundle(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
        );

        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


}