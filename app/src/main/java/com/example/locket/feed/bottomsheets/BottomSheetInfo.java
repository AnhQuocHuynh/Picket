package com.example.locket.feed.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.locket.MainActivity;
import com.example.locket.R;
import com.example.locket.auth.bottomsheets.BottomSheetChangeEmail;
import com.example.locket.auth.bottomsheets.BottomSheetChangeName;
import com.example.locket.auth.bottomsheets.BottomSheetLogout;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.utils.CloudinaryManager;
import com.example.locket.common.models.user.AccountInfo;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.feed.fragments.FriendLinkTestFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BottomSheetInfo extends BottomSheetDialogFragment implements MainActivity.FriendsListUpdateListener, BottomSheetFriend.FriendBottomSheetListener {
    private final Context context;
    private final Activity activity;

    private TextView txt_edit_info;
    private LinearLayout linear_logout, linear_new, linear_change_email, linear_friend, linear_friend_link ,linear_change_password;
    private TextView txt_full_name;
    private RoundedImageView img_capture;
    private static final int PICK_IMAGE_REQUEST = 1;

    public BottomSheetInfo(Context context, Activity activity) {
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
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.item_bottom_sheet_info, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        initViews(bottomSheetDialog);
        onClick();

        return bottomSheetDialog;
    }

    private void initViews(BottomSheetDialog bottomSheetDialog) {
        txt_edit_info = bottomSheetDialog.findViewById(R.id.txt_edit_info);

        img_capture = bottomSheetDialog.findViewById(R.id.img_capture);
        txt_full_name = bottomSheetDialog.findViewById(R.id.txt_full_name);
//        linear_change_email = bottomSheetDialog.findViewById(R.id.linear_change_email);
        linear_friend = bottomSheetDialog.findViewById(R.id.linear_friends);
        linear_change_password = bottomSheetDialog.findViewById(R.id.linear_change_password);
        linear_friend_link = bottomSheetDialog.findViewById(R.id.linear_friend_link);
        linear_logout = bottomSheetDialog.findViewById(R.id.linear_logout);

        // Load user profile data
        loadUserProfileData(txt_full_name, img_capture);
    }

    private void onClick() {

        linear_logout.setOnClickListener(view -> {
            openBottomSheetLogout();
        });
//        linear_new.setOnClickListener(view -> openBottomSheetRegisterUserName());
        txt_edit_info.setOnClickListener(view -> openBottomSheetChangeName());
//        linear_change_email.setOnClickListener(view -> openBottomSheetChangeEmail());
        linear_friend.setOnClickListener(view->openBottomSheetFriendList());
        linear_friend_link.setOnClickListener(view -> openFriendLinkTest());
        linear_change_password.setOnClickListener(view->openBottomSheetChangePassword());
        img_capture.setOnClickListener(v -> openImagePicker());
    }

    private void openBottomSheetFriendList() {
        dismiss();
        BottomSheetFriend bottomSheetFriend = new BottomSheetFriend();
        bottomSheetFriend.setFriendBottomSheetListener(this);
        bottomSheetFriend.show(getParentFragmentManager(), bottomSheetFriend.getTag());
    }

    private void openBottomSheetChangePassword() {
    }

    private void openBottomSheetChangeEmail() {
        dismiss();
        BottomSheetChangeEmail bottomSheetChangeEmail = new BottomSheetChangeEmail(context, activity);
        bottomSheetChangeEmail.show(getActivity().getSupportFragmentManager(), bottomSheetChangeEmail.getTag());
    }


    private void openBottomSheetLogout() {
        dismiss();
        BottomSheetLogout bottomSheetLogout = new BottomSheetLogout(context, activity);
        bottomSheetLogout.show(getActivity().getSupportFragmentManager(), bottomSheetLogout.getTag());
    }

    private void openBottomSheetChangeName() {
        dismiss();
        BottomSheetChangeName bottomSheetChangeName = new BottomSheetChangeName(context, activity);
        bottomSheetChangeName.show(getActivity().getSupportFragmentManager(), bottomSheetChangeName.getTag());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadAvatarAndUpdateProfile(imageUri);
        }
    }

    private void openFriendLinkTest() {
        dismiss();
        // Navigate to FriendLinkTestFragment
        if (activity instanceof com.example.locket.MainActivity) {
            com.example.locket.MainActivity mainActivity = (com.example.locket.MainActivity) activity;
            androidx.fragment.app.FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            com.example.locket.feed.fragments.FriendLinkTestFragment friendLinkTestFragment =
                    new com.example.locket.feed.fragments.FriendLinkTestFragment();

            transaction.replace(android.R.id.content, friendLinkTestFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    /**
     * Load user profile data from API or fallback to cached data
     */
    private void loadUserProfileData(TextView txtFullName, RoundedImageView imgCapture) {
        // First try to load from cached UserProfile
        UserProfile cachedProfile = SharedPreferencesUser.getUserProfile(requireContext());
        if (cachedProfile != null && cachedProfile.getUser() != null) {
            setUserProfileData(cachedProfile, txtFullName, imgCapture);
        }

        // Then load fresh data from API
        AuthManager.getUserProfile(requireContext(), new AuthManager.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (userProfile != null && userProfile.getUser() != null) {
                    // Save updated profile
                    SharedPreferencesUser.saveUserProfile(requireContext(), userProfile);
                    // Update UI
                    setUserProfileData(userProfile, txtFullName, imgCapture);
                }
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                // Fallback to LoginResponse if UserProfile fails
                LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());
                if (loginResponse != null) {
                    setLoginResponseData(loginResponse, txtFullName, imgCapture);
                } else {
                    // Last fallback to AccountInfo (for backward compatibility)
                    AccountInfo accountInfo = SharedPreferencesUser.getAccountInfo(requireContext());
                    if (accountInfo != null && accountInfo.getUsers() != null && !accountInfo.getUsers().isEmpty()) {
                        txtFullName.setText(accountInfo.getUsers().get(0).getDisplayName());
                        Glide.with(BottomSheetInfo.this).load(accountInfo.getUsers().get(0).getPhotoUrl()).into(imgCapture);
                    }
                }
            }
        });
    }

    private void uploadAvatarAndUpdateProfile(Uri imageUri) {
        try {
            byte[] imageBytes = getBytesFromUri(imageUri);
            String fileName = "avatar_" + System.currentTimeMillis();

            CloudinaryManager.uploadImage(imageBytes, fileName, new CloudinaryManager.CloudinaryUploadCallback() {
                @Override
                public void onUploadStart(String requestId) {
                    Log.d("BottomSheetInfo", "Cloudinary upload started.");
                    // Optionally, show a loading indicator
                }

                @Override
                public void onUploadProgress(String requestId, int progress) {
                    // Optionally, update a progress bar
                }

                @Override
                public void onUploadSuccess(String requestId, String publicUrl) {
                    Log.d("BottomSheetInfo", "Cloudinary upload success. URL: " + publicUrl);

                    // After uploading, update the user's profile with the new avatar URL
                    UserProfile cachedProfile = SharedPreferencesUser.getUserProfile(requireContext());
                    String currentUsername = "";
                    if (cachedProfile != null && cachedProfile.getUser() != null) {
                        currentUsername = cachedProfile.getUser().getUsername();
                    }

                    AuthManager.updateProfile(requireContext(), currentUsername, publicUrl, new AuthManager.ProfileCallback() {
                        @Override
                        public void onSuccess(UserProfile userProfile) {
                            Log.d("BottomSheetInfo", "Profile updated successfully.");

                            loadUserProfileData(txt_full_name, img_capture); // Refresh UI
                        }

                        @Override
                        public void onError(String errorMessage, int errorCode) {
                            Log.e("BottomSheetInfo", "Error updating profile: " + errorMessage);
                            // Handle error, e.g., show a toast
                        }
                    });
                }

                @Override
                public void onUploadError(String requestId, String error) {
                    Log.e("BottomSheetInfo", "Cloudinary upload error: " + error);
                    // Handle error
                }

                @Override
                public void onUploadReschedule(String requestId, String error) {
                    // Handle reschedule
                }
            });

        } catch (IOException e) {
            Log.e("BottomSheetInfo", "Error converting URI to bytes", e);
            // Handle error
        }
    }

    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream iStream = requireContext().getContentResolver().openInputStream(uri);
        if (iStream == null) {
            throw new IOException("Unable to open input stream for URI: " + uri);
        }
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = iStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        iStream.close();
        return byteBuffer.toByteArray();
    }

    /**
     * Set UI data from UserProfile
     */
    private void setUserProfileData(UserProfile userProfile, TextView txtFullName, RoundedImageView imgCapture) {
        UserProfile.UserData userData = userProfile.getUser();

        // Set display name
        String displayName = userData.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = userData.getUsername();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = userData.getEmail();
        }
        txtFullName.setText(displayName);

        // Load profile pictures
        String profilePictureUrl = userData.getProfilePicture();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {

            Glide.with(this)
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.ic_widget_empty_icon)
                    .error(R.drawable.ic_widget_empty_icon)
                    .into(imgCapture);
        } else {
            imgCapture.setImageResource(R.drawable.ic_launcher_round);
        }
    }

    /**
     * 🔄 Fallback: Set UI data from LoginResponse
     */
    private void setLoginResponseData(LoginResponse loginResponse, TextView txtFullName, RoundedImageView imgCapture) {
        // Set display name
        String displayName = loginResponse.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = loginResponse.getEmail();
        }
        txtFullName.setText(displayName);

        // Load profile pictures
        String profilePictureUrl = loginResponse.getProfilePicture();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Glide.with(this).load(profilePictureUrl).into(imgCapture);
        } else {
            imgCapture.setImageResource(R.drawable.ic_launcher_round);
        }
    }

    @Override
    public void onFriendsListUpdated() {

    }

    @Override
    public void onSendFriendLinkClicked() {
        // Check if the fragment is currently added to an activity and has a context
        if (!isAdded() || getContext() == null) {
            Log.w("BottomSheetInfo", "onSendFriendLinkClicked called when fragment is not attached or context is null.");
            return; // Exit if not in a valid state
        }

        // It's also good practice to dismiss the current dialog if it's still showing
        // before navigating to another fragment, though this depends on your desired UX.
        // If BottomSheetInfo is the one that should be dismissed here, you might already be doing it.
        // If this callback comes from BottomSheetFriend, ensure BottomSheetFriend is dismissed if needed.

        Log.d("BottomSheetInfo", "🔗 Navigating to Friend Link Fragment from onSendFriendLinkClicked");

        // Consider using requireParentFragmentManager() if you are sure it should be attached.
        // This will throw an exception if not attached, helping identify issues earlier in development.
        // However, for callbacks, a graceful check is often preferred.
        FragmentManager fragmentManager = getParentFragmentManager(); // Or requireParentFragmentManager()

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Ensure R.id.frame_layout is the correct container ID in your Activity's layout
        // where you want to place FriendLinkTestFragment.
        // If this method is called from BottomSheetInfo, and BottomSheetInfo is a dialog,
        // replacing R.id.frame_layout in the Activity might be the intended behavior.
        transaction.replace(R.id.frame_layout, new FriendLinkTestFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        // If BottomSheetInfo itself should be dismissed after this action
        if (getDialog() != null && getDialog().isShowing()) {
            dismissAllowingStateLoss(); // Or just dismiss() if state loss is not a concern here
        }
    }
}

