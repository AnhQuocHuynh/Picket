package com.example.locket.common.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.locket.R;

public class SuccessNotificationDialog {

    private Dialog dialog;
    private Context context;

    public interface OnDismissListener {
        void onDismiss();
    }

    public SuccessNotificationDialog(Context context) {
        this.context = context;
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_success_notification, null);
        dialog.setContentView(dialogView);

        // Set dialog properties
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void show(String title, String message, OnDismissListener dismissListener) {
        if (dialog != null && !dialog.isShowing()) {
            // Update text content
            TextView txtTitle = dialog.findViewById(R.id.txt_success_title);
            TextView txtMessage = dialog.findViewById(R.id.txt_success_message);
            LottieAnimationView lottieIcon = dialog.findViewById(R.id.lottie_success_icon);

            if (txtTitle != null) {
                txtTitle.setText(title != null ? title : "Gửi thành công!");
            }
            if (txtMessage != null) {
                txtMessage.setText(message != null ? message : "Ảnh của bạn đã được gửi đến bạn bè");
            }

            // Start animation
            if (lottieIcon != null) {
                lottieIcon.playAnimation();
            }

            dialog.show();

            // Auto dismiss after 2.5 seconds
            dialog.getWindow().getDecorView().postDelayed(() -> {
                dismiss();
                if (dismissListener != null) {
                    dismissListener.onDismiss();
                }
            }, 2500);
        }
    }

    public void show(OnDismissListener dismissListener) {
        show(null, null, dismissListener);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
} 