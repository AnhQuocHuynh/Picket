package com.example.locket.feed;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.locket.R;
import com.example.locket.auth.UserManager;
import com.example.locket.auth.UserType;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// CameraX Video API
import androidx.camera.video.VideoCapture;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.OutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.video.FileOutputOptions;

import com.example.locket.feed.RecordingProgressView;

public class HomeFragment extends Fragment {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;
    private ExecutorService cameraExecutor;
    private boolean isBackCamera = true;
    private boolean isRecording = false;
    private UserManager userManager;

    private TextView tvDropdown;
    private ImageView ivAvatar;
    private ImageButton btnChat, btnGallery, btnCapture, btnSwitchCamera, btnDraw;
    private RecordingProgressView recordingProgress;

    private GestureDetectorCompat gestureDetector;
    private NavController navController;

    private final ActivityResultLauncher<String> requestAudioPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "Audio permission denied - video recording will not work", Toast.LENGTH_SHORT).show();
                    startCamera(); // Vẫn start camera nhưng không có video recording
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Sau khi có camera permission, xin audio permission nếu là pro user
                    if (userManager.isProUser()) {
                        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                    } else {
                        startCamera();
                    }
                } else {
                    Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });
    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userManager = UserManager.getInstance(requireContext());
        navController = Navigation.findNavController(view);
        previewView = view.findViewById(R.id.previewView);
        tvDropdown = view.findViewById(R.id.tvDropdown);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnChat = view.findViewById(R.id.btnChat);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnCapture = view.findViewById(R.id.btnCapture);
        btnSwitchCamera = view.findViewById(R.id.btnSwitchCamera);
        btnDraw = view.findViewById(R.id.btnDraw);
        recordingProgress = view.findViewById(R.id.recordingProgress);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Gallery picker launcher
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Bundle bundle = new Bundle();
                bundle.putString("image_uri", uri.toString());
                navController.navigate(R.id.action_homeFragment_to_photoPreviewFragment, bundle);
            }
        });

        // Check camera permission before starting camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        gestureDetector = new GestureDetectorCompat(requireContext(), new SwipeGestureListener());
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        handleEvents();
    }

    private void handleEvents() {
        // Setup capture button với chức năng quay phim cho pro users
        if (userManager.isProUser()) {
            setupProCaptureButton();
        } else {
            btnCapture.setOnClickListener(v -> takePhoto());
        }

        btnDraw.setOnClickListener(v -> {
            navController.navigate(R.id.action_homeFragment_to_drawingFragment);
        });

        btnChat.setOnClickListener(v -> {
            navController.navigate(R.id.messageFragment);
        });

        btnGallery.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        ivAvatar.setOnClickListener(v -> {
            navController.navigate(R.id.action_homeFragment_to_userSettingsFragment);
        });

        tvDropdown.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), tvDropdown);
            popup.getMenu().add("Everyone");
            popup.getMenu().add("Only Me");
            popup.getMenu().add("Friends");

            popup.setOnMenuItemClickListener(item -> {
                tvDropdown.setText(item.getTitle());
                return true;
            });
            popup.show();
        });

        btnSwitchCamera.setOnClickListener(v -> {
            isBackCamera = !isBackCamera;
            startCamera();
        });
    }

    private void setupProCaptureButton() {
        btnCapture.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Bắt đầu theo dõi thời gian giữ
                    v.setTag(System.currentTimeMillis());
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // Kiểm tra nếu giữ lâu thì bắt đầu quay phim
                    Long startTime = (Long) v.getTag();
                    if (startTime != null && !isRecording) {
                        long holdTime = System.currentTimeMillis() - startTime;
                        if (holdTime > 500) { // Giữ hơn 500ms = bắt đầu quay phim
                            startRecording();
                            // Hiển thị progress view
                            recordingProgress.setVisibility(View.VISIBLE);
                            recordingProgress.startRecording();
                        }
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Kiểm tra thời gian giữ
                    startTime = (Long) v.getTag();
                    if (startTime != null) {
                        long holdTime = System.currentTimeMillis() - startTime;
                        if (holdTime > 500) { // Giữ hơn 500ms = dừng quay phim
                            stopRecording();
                            // Ẩn progress view
                            recordingProgress.stopRecording();
                            recordingProgress.setVisibility(View.GONE);
                        } else { // Bấm nhanh = chụp ảnh
                            takePhoto();
                        }
                    }
                    v.setTag(null);
                    return true;
            }
            return false;
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                // Thêm VideoCapture cho người dùng pro
                if (userManager.isProUser()) {
                    Recorder recorder = new Recorder.Builder().build();
                    videoCapture = VideoCapture.withOutput(recorder);
                } else {
                    videoCapture = null;
                }

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();

                if (userManager.isProUser() && videoCapture != null) {
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
                } else {
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                }

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (Exception e) {
                Toast.makeText(getContext(), "Không thể khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(requireContext().getCacheDir(),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(System.currentTimeMillis()) + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                requireActivity().runOnUiThread(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("image_uri", savedUri.toString());
                    navController.navigate(R.id.action_homeFragment_to_photoPreviewFragment, bundle);
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void startRecording() {
        if (videoCapture == null || isRecording) return;

        File videoFile = new File(requireContext().getCacheDir(),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(System.currentTimeMillis()) + ".mp4");

        FileOutputOptions outputOptions = new FileOutputOptions.Builder(videoFile).build();
        PendingRecording pendingRecording = videoCapture.getOutput().prepareRecording(requireContext(), outputOptions);
        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(requireContext()), event -> {
            if (event instanceof VideoRecordEvent.Start) {
                isRecording = true;
                Toast.makeText(getContext(), "Bắt đầu quay phim...", Toast.LENGTH_SHORT).show();
            } else if (event instanceof VideoRecordEvent.Finalize) {
                isRecording = false;
                VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) event;
                if (finalizeEvent.hasError()) {
                    Toast.makeText(getContext(), "Lỗi quay phim: " + finalizeEvent.getError(), Toast.LENGTH_SHORT).show();
                } else {
                    Uri savedUri = Uri.fromFile(videoFile);
                    Bundle bundle = new Bundle();
                    bundle.putString("video_uri", savedUri.toString());
                    bundle.putBoolean("is_video", true);
                    navController.navigate(R.id.action_homeFragment_to_photoPreviewFragment, bundle);
                }
                // Ẩn progress view khi kết thúc
                requireActivity().runOnUiThread(() -> {
                    recordingProgress.stopRecording();
                    recordingProgress.setVisibility(View.GONE);
                });
            }
        });

        isRecording = true;
    }

    private void stopRecording() {
        if (activeRecording != null && isRecording) {
            activeRecording.stop();
            isRecording = false;
            Toast.makeText(getContext(), "Dừng quay phim", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    // --- Gesture swipe navigation ---
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY < 0) {
                            onSwipeUp();
                        }
                        result = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    private void onSwipeLeft() {
        navController.navigate(R.id.historyFragment);
    }

    private void onSwipeRight() {
        navController.navigate(R.id.messageFragment);
    }

    private void onSwipeUp() {
        navController.navigate(R.id.feedFragment);
    }
}
