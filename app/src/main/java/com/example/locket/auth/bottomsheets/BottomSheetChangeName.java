package com.example.locket.auth.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.locket.R;
import com.example.locket.common.models.user.UserProfile;
import com.example.locket.common.utils.AuthManager;
import com.example.locket.common.utils.SharedPreferencesUser;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetChangeName extends BottomSheetDialogFragment {
    private final Context context;
    private final Activity activity;
    private BottomSheetDialog bottomSheetDialog;

    private EditText edt_username;
    private LinearLayout linear_continue;
    private TextView txt_continue;
    private String username;

    public BottomSheetChangeName(Context context, Activity activity) {
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
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.item_bottom_sheet_change_name, null);
        bottomSheetDialog.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        initViews(bottomSheetDialog);
        setData();
        conFigViews();
        onClick();

        return bottomSheetDialog;
    }

    private void initViews(BottomSheetDialog bottomSheetDialog) {
        edt_username = bottomSheetDialog.findViewById(R.id.edt_username); // Assuming edt_name is the ID for username field
        linear_continue = bottomSheetDialog.findViewById(R.id.linear_continue);
        txt_continue = bottomSheetDialog.findViewById(R.id.txt_continue);
    }

    private void conFigViews() {
        edt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                username = s.toString().trim();
                if (!username.isEmpty()) {
                    linear_continue.setBackground(ContextCompat.getDrawable(context, R.drawable.background_btn_continue_check));
                    txt_continue.setTextColor(ContextCompat.getColor(context, R.color.white));
                    linear_continue.setEnabled(true);
                } else {
                    linear_continue.setBackground(ContextCompat.getDrawable(context, R.drawable.background_btn_continue_un_check));
                    txt_continue.setTextColor(ContextCompat.getColor(context, R.color.hint));
                    linear_continue.setEnabled(false);
                }
            }
        });
    }

    private void setData() {
        UserProfile userProfile = SharedPreferencesUser.getUserProfile(context);
        if (userProfile != null && userProfile.getUser() != null) {
            edt_username.setText(userProfile.getUser().getUsername());
        }
    }

    private void onClick() {

        linear_continue.setOnClickListener(view -> changeName());
    }

    private void changeName() {
        String newUsername = edt_username.getText().toString().trim();
        if (newUsername.isEmpty()) {
            return; 
        }

        AuthManager.updateProfile(context, newUsername, null, new AuthManager.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d("BottomSheetChangeName", "Username updated successfully.");
                dismiss();
            }

            @Override
            public void onError(String errorMessage, int errorCode) {
                Log.e("BottomSheetChangeName", "Error updating name: " + errorMessage);
                //toast
                Toast.makeText(context, "Error updating name: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


//    @Override
//    public void onDismiss(@NonNull DialogInterface dialog) {
//        super.onDismiss(dialog);
//        BottomSheetInfo bottomSheet1 = new BottomSheetInfo(context, activity);
//        bottomSheet1.show(getParentFragmentManager(), bottomSheet1.getTag());
//    }
}

