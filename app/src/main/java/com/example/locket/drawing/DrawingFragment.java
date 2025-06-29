package com.example.locket.drawing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.locket.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DrawingFragment extends Fragment {

    private DrawingView drawingView;
    private ImageButton btnBack, btnClear, btnPost, btnUndo, btnEraser;
    private SeekBar strokeWidthSeekBar;
    private TextView strokeWidthText;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // Initialize views
        drawingView = view.findViewById(R.id.drawingView);
        btnBack = view.findViewById(R.id.btnBack);
        btnClear = view.findViewById(R.id.btnClear);
        btnPost = view.findViewById(R.id.btnSave);
        btnUndo = view.findViewById(R.id.btnUndo);
        btnEraser = view.findViewById(R.id.btnEraser);
        strokeWidthSeekBar = view.findViewById(R.id.strokeWidthSeekBar);
        strokeWidthText = view.findViewById(R.id.strokeWidthText);

        setupColorPalette(view);
        setupStrokeWidth();
        setupButtons();
    }

    private void setupColorPalette(View view) {
        int[] colorIds = {
                R.id.colorBlack, R.id.colorRed, R.id.colorBlue, R.id.colorGreen,
                R.id.colorYellow, R.id.colorPurple, R.id.colorOrange, R.id.colorPink
        };

        for (int colorId : colorIds) {
            View colorView = view.findViewById(colorId);
            colorView.setOnClickListener(v -> {
                String colorTag = (String) v.getTag();
                int color = Color.parseColor(colorTag);
                drawingView.setColor(color);

                // Chỉ update selection nếu màu này chưa được chọn
                if (!isColorSelected(v)) {
                    updateColorSelection(v);
                }
            });
        }
    }

    private boolean isColorSelected(View colorView) {
        // Kiểm tra xem màu này có đang được chọn không (có border cam)
        return colorView.getBackground().getConstantState() ==
                getResources().getDrawable(R.drawable.circle_orange_border).getConstantState();
    }

    private void updateColorSelection(View selectedView) {
        // Reset all color views về màu gốc
        View parent = (View) selectedView.getParent();
        for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
            View child = ((ViewGroup) parent).getChildAt(i);
            String colorTag = (String) child.getTag();
            if (colorTag != null) {
                switch (colorTag) {
                    case "#000000":
                        child.setBackgroundResource(R.drawable.circle_black);
                        break;
                    case "#FF0000":
                        child.setBackgroundResource(R.drawable.circle_red);
                        break;
                    case "#0000FF":
                        child.setBackgroundResource(R.drawable.circle_blue);
                        break;
                    case "#00FF00":
                        child.setBackgroundResource(R.drawable.circle_green);
                        break;
                    case "#FFFF00":
                        child.setBackgroundResource(R.drawable.circle_yellow);
                        break;
                    case "#800080":
                        child.setBackgroundResource(R.drawable.circle_purple);
                        break;
                    case "#FFA500":
                        child.setBackgroundResource(R.drawable.circle_orange);
                        break;
                    case "#FFC0CB":
                        child.setBackgroundResource(R.drawable.circle_pink);
                        break;
                }
            }
        }

        // Highlight selected color với border cam
        selectedView.setBackgroundResource(R.drawable.circle_orange_border);
    }

    private void setupStrokeWidth() {
        strokeWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float strokeWidth = Math.max(1, progress);
                    drawingView.setStrokeWidth(strokeWidth);
                    strokeWidthText.setText(String.valueOf((int) strokeWidth));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (drawingView.hasContent()) {
                // Show confirmation dialog
                showDiscardConfirmation();
            } else {
                navController.navigateUp();
            }
        });

        btnClear.setOnClickListener(v -> {
            drawingView.clear();
        });

        btnPost.setOnClickListener(v -> {
            if (drawingView.hasContent()) {
                postDrawing();
            } else {
                Toast.makeText(getContext(), "Vẽ gì đó trước khi đăng!", Toast.LENGTH_SHORT).show();
            }
        });

        btnUndo.setOnClickListener(v -> {
            if (drawingView.hasContent()) {
                drawingView.undo();
            } else {
                Toast.makeText(getContext(), "Không có gì để hoàn tác", Toast.LENGTH_SHORT).show();
            }
        });

        btnEraser.setOnClickListener(v -> {
            drawingView.setColor(Color.WHITE);
            updateEraserSelection();
        });
    }

    private void showDiscardConfirmation() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn hủy bức vẽ này?")
                .setPositiveButton("Hủy bỏ", (dialog, which) -> {
                    navController.navigateUp();
                })
                .setNegativeButton("Tiếp tục vẽ", null)
                .show();
    }

    private void updateEraserSelection() {
        // Reset color selection và highlight eraser
        HorizontalScrollView colorPalette = (HorizontalScrollView) getView().findViewById(R.id.colorPalette);
        ViewGroup colorContainer = (ViewGroup) colorPalette.getChildAt(0);
        for (int i = 0; i < colorContainer.getChildCount(); i++) {
            View child = colorContainer.getChildAt(i);
            String colorTag = (String) child.getTag();
            if (colorTag != null) {
                switch (colorTag) {
                    case "#000000":
                        child.setBackgroundResource(R.drawable.circle_black);
                        break;
                    case "#FF0000":
                        child.setBackgroundResource(R.drawable.circle_red);
                        break;
                    case "#0000FF":
                        child.setBackgroundResource(R.drawable.circle_blue);
                        break;
                    case "#00FF00":
                        child.setBackgroundResource(R.drawable.circle_green);
                        break;
                    case "#FFFF00":
                        child.setBackgroundResource(R.drawable.circle_yellow);
                        break;
                    case "#800080":
                        child.setBackgroundResource(R.drawable.circle_purple);
                        break;
                    case "#FFA500":
                        child.setBackgroundResource(R.drawable.circle_orange);
                        break;
                    case "#FFC0CB":
                        child.setBackgroundResource(R.drawable.circle_pink);
                        break;
                }
            }
        }
        btnEraser.setBackgroundResource(R.drawable.circle_orange_border);
    }

    private void postDrawing() {
        try {
            Bitmap bitmap = drawingView.getBitmap();

            // Create file name with timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(System.currentTimeMillis());
            String fileName = "drawing_" + timeStamp + ".jpg";

            // Save to cache directory
            File file = new File(requireContext().getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            // Navigate to photo preview with the drawing
            Uri savedUri = Uri.fromFile(file);
            Bundle bundle = new Bundle();
            bundle.putString("image_uri", savedUri.toString());
            bundle.putBoolean("is_drawing", true);

            // Lưu dữ liệu replay
            bundle.putSerializable("replay_paths", (java.io.Serializable) drawingView.getReplayPaths());

            navController.navigate(R.id.action_drawingFragment_to_photoPreviewFragment, bundle);

            // Tự động quay về HomeFragment sau khi đăng
            Toast.makeText(getContext(), "Đã đăng tranh vẽ!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(getContext(), "Lỗi khi đăng bức vẽ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}