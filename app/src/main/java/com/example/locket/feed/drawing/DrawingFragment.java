package com.example.locket.feed.drawing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.locket.R;
import com.example.locket.camera.fragments.PhotoPreviewFragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DrawingFragment extends Fragment {
    private final int[] COLORS = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.LTGRAY};
    private DrawingView drawingView;
    private LinearLayout layoutColors;
    private SeekBar seekBarStroke;
    private TextView tvStrokeWidth;
    private ImageButton btnUndo, btnRedo, btnClear, btnBack, btnSend, btnEraser;
    private boolean isEraserMode = false;
    private int lastColor = Color.BLACK;
    private List<ImageButton> colorButtons = new ArrayList<>();
    private int selectedColorIndex = 0;

    public static DrawingFragment newInstance(Bitmap bitmap) {
        DrawingFragment fragment = new DrawingFragment();
        if (bitmap != null) {
            Bundle args = new Bundle();
            args.putParcelable("drawing_bitmap", bitmap);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawing, container, false);
        drawingView = view.findViewById(R.id.drawing_view);
        layoutColors = view.findViewById(R.id.layout_colors);
        seekBarStroke = view.findViewById(R.id.seekbar_stroke);
        tvStrokeWidth = view.findViewById(R.id.tv_stroke_width);
        btnUndo = view.findViewById(R.id.btn_undo);
        btnRedo = view.findViewById(R.id.btn_redo);
        btnClear = view.findViewById(R.id.btn_clear);
        btnBack = view.findViewById(R.id.btn_back);
        btnSend = view.findViewById(R.id.btn_send);
        btnEraser = view.findViewById(R.id.btn_eraser);
        setupColorPicker();
        setupStrokePicker();
        btnUndo.setOnClickListener(v -> drawingView.undo());
        btnRedo.setOnClickListener(v -> drawingView.redo());
        btnClear.setOnClickListener(v -> drawingView.clear());
        btnBack.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có muốn thoát khỏi trang vẽ không? Mọi thay đổi chưa lưu sẽ bị mất.")
                    .setPositiveButton("Có", (dialog, which) -> {
                        requireActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        btnSend.setOnClickListener(v -> sendDrawing());
        btnEraser.setOnClickListener(v -> {
            isEraserMode = !isEraserMode;
            if (isEraserMode) {
                lastColor = drawingView.getCurrentColor();
                drawingView.setColor(Color.WHITE);
                btnEraser.setAlpha(0.5f);
            } else {
                drawingView.setColor(lastColor);
                btnEraser.setAlpha(1f);
            }
        });
        if (getArguments() != null && getArguments().containsKey("drawing_bitmap")) {
            Bitmap bitmap = getArguments().getParcelable("drawing_bitmap");
            if (bitmap != null) {
                drawingView.post(() -> drawingView.setBitmap(bitmap));
            }
        }
        return view;
    }

    private void setupColorPicker() {
        layoutColors.removeAllViews();
        colorButtons.clear();
        for (int i = 0; i < COLORS.length; i++) {
            int color = COLORS[i];
            ImageButton btn = new ImageButton(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics()));
            params.setMargins(8, 0, 8, 0);
            btn.setLayoutParams(params);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setShape(GradientDrawable.OVAL);
            if (i == selectedColorIndex) {
                drawable.setStroke(8, getResources().getColor(R.color.orange));
            } else {
                drawable.setStroke(3, Color.WHITE);
            }
            btn.setBackground(drawable);
            final int index = i;
            btn.setOnClickListener(v -> {
                drawingView.setColor(color);
                updateColorSelection(index);
            });
            colorButtons.add(btn);
            layoutColors.addView(btn);
        }
    }

    private void updateColorSelection(int newIndex) {
        for (int i = 0; i < colorButtons.size(); i++) {
            GradientDrawable drawable = (GradientDrawable) colorButtons.get(i).getBackground();
            if (i == newIndex) {
                drawable.setStroke(8, getResources().getColor(R.color.orange));
            } else {
                drawable.setStroke(3, Color.WHITE);
            }
            colorButtons.get(i).setBackground(drawable);
        }
        selectedColorIndex = newIndex;
    }

    private void setupStrokePicker() {
        seekBarStroke.setProgress(5);
        tvStrokeWidth.setText("5");
        drawingView.setStrokeWidth(5f);
        seekBarStroke.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int width = Math.max(progress, 1);
                tvStrokeWidth.setText(String.valueOf(width));
                drawingView.setStrokeWidth(width);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void sendDrawing() {
        Bitmap bitmap = Bitmap.createBitmap(drawingView.getWidth(), drawingView.getHeight(), Bitmap.Config.ARGB_8888);
        drawingView.draw(new android.graphics.Canvas(bitmap));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        PhotoPreviewFragment fragment = PhotoPreviewFragment.newInstance(bitmap, bytes);
        fragment.setPhotoPreviewListener(new PhotoPreviewFragment.PhotoPreviewListener() {
            @Override
            public void onCancel() {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, DrawingFragment.newInstance(bitmap))
                        .commit();
            }
            @Override
            public void onRetake() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
            @Override
            public void onSendComplete() {
                requireActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack("photo_preview");
        transaction.commit();
    }
} 