package com.example.locket.feed.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.makeramen.roundedimageview.RoundedImageView;
import com.example.locket.R;
import com.example.locket.auth.bottomsheets.BottomSheetChangeEmail;
import com.example.locket.auth.bottomsheets.BottomSheetChangeName;
import com.example.locket.auth.bottomsheets.BottomSheetLogout;
import com.example.locket.auth.bottomsheets.BottomSheetRegisterUserName;
import com.example.locket.common.models.user.AccountInfo;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;

public class BottomSheetInfo extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;

    private TextView txt_edit_info;
    private LinearLayout linear_logout, linear_new, linear_change_email;

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
        linear_new = bottomSheetDialog.findViewById(R.id.linear_new);
        RoundedImageView img_capture = bottomSheetDialog.findViewById(R.id.img_capture);
        RoundedImageView img_avatar_2 = bottomSheetDialog.findViewById(R.id.img_avatar_2);
        TextView txt_full_name = bottomSheetDialog.findViewById(R.id.txt_full_name);
        linear_change_email = bottomSheetDialog.findViewById(R.id.linear_change_email);
        linear_logout = bottomSheetDialog.findViewById(R.id.linear_logout);

        // Load user profile data
        loadUserProfileData(txt_full_name, img_avatar_2, img_capture);
    }

    private void onClick() {

        linear_logout.setOnClickListener(view -> {
            openBottomSheetLogout();
        });
        linear_new.setOnClickListener(view -> openBottomSheetRegisterUserName());

        txt_edit_info.setOnClickListener(view -> openBottomSheetChangeName());
        linear_change_email.setOnClickListener(view -> openBottomSheetChangeEmail());
    }

    private void openBottomSheetRegisterUserName() {
        dismiss();
        BottomSheetRegisterUserName bottomSheetRegisterUserName = new BottomSheetRegisterUserName(context, activity);
        bottomSheetRegisterUserName.show(getActivity().getSupportFragmentManager(), bottomSheetRegisterUserName.getTag());
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

    /**
     * 🔄 Load user profile data from API or fallback to cached data
     */
    private void loadUserProfileData(TextView txtFullName, RoundedImageView imgAvatar2, RoundedImageView imgCapture) {
        // First try to load from cached UserProfile
        UserProfile cachedProfile = SharedPreferencesUser.getUserProfile(requireContext());
        if (cachedProfile != null && cachedProfile.getUser() != null) {
            setUserProfileData(cachedProfile, txtFullName, imgAvatar2, imgCapture);
        }

        // Then load fresh data from API
        AuthManager.getUserProfile(requireContext(), new AuthManager.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (userProfile != null && userProfile.getUser() != null) {
                    // Save updated profile
                    SharedPreferencesUser.saveUserProfile(requireContext(), userProfile);
                    // Update UI
                    setUserProfileData(userProfile, txtFullName, imgAvatar2, imgCapture);
                }
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                // Fallback to LoginResponse if UserProfile fails
                LoginResponse loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());
                if (loginResponse != null) {
                    setLoginResponseData(loginResponse, txtFullName, imgAvatar2, imgCapture);
                } else {
                    // Last fallback to AccountInfo (for backward compatibility)
                    AccountInfo accountInfo = SharedPreferencesUser.getAccountInfo(requireContext());
                    if (accountInfo != null && accountInfo.getUsers() != null && !accountInfo.getUsers().isEmpty()) {
                        txtFullName.setText(accountInfo.getUsers().get(0).getDisplayName());
                        Glide.with(BottomSheetInfo.this).load(accountInfo.getUsers().get(0).getPhotoUrl()).into(imgAvatar2);
                        Glide.with(BottomSheetInfo.this).load(accountInfo.getUsers().get(0).getPhotoUrl()).into(imgCapture);
                    }
                }
            }
        });
    }

    /**
     * 🎨 Set UI data from UserProfile
     */
    private void setUserProfileData(UserProfile userProfile, TextView txtFullName, RoundedImageView imgAvatar2, RoundedImageView imgCapture) {
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
                    .into(imgAvatar2);
            
            Glide.with(this)
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.ic_widget_empty_icon)
                    .error(R.drawable.ic_widget_empty_icon)
                    .into(imgCapture);
        } else {
            imgAvatar2.setImageResource(R.drawable.ic_widget_empty_icon);
            imgCapture.setImageResource(R.drawable.ic_widget_empty_icon);
        }
    }

    /**
     * 🔄 Fallback: Set UI data from LoginResponse
     */
    private void setLoginResponseData(LoginResponse loginResponse, TextView txtFullName, RoundedImageView imgAvatar2, RoundedImageView imgCapture) {
        // Set display name
        String displayName = loginResponse.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = loginResponse.getEmail();
        }
        txtFullName.setText(displayName);

        // Load profile pictures
        String profilePictureUrl = loginResponse.getProfilePicture();
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            Glide.with(this).load(profilePictureUrl).into(imgAvatar2);
            Glide.with(this).load(profilePictureUrl).into(imgCapture);
        } else {
            imgAvatar2.setImageResource(R.drawable.ic_widget_empty_icon);
            imgCapture.setImageResource(R.drawable.ic_widget_empty_icon);
        }
    }
}

