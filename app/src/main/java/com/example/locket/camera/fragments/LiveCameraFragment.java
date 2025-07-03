package com.example.locket.camera.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.locket.R;
import com.example.locket.camera.utils.ImageUtils;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.post.PostResponse;
import com.example.locket.common.network.ImageUploadService;
import com.example.locket.common.network.MomentApiService;
import com.example.locket.common.repository.PostRepository;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.utils.SuccessNotificationDialog;
import com.google.android.gms.common.util.IOUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import androidx.camera.video.VideoCapture;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.core.util.Consumer;

public class LiveCameraFragment extends Fragment {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_CODE_PICK_IMAGE = 10001;

    private PreviewView camera_view;

    private RelativeLayout layout_img_view;
    private ImageView img_view;
    private EditText edt_add_message;


    private LinearLayout linear_controller_media;
    private ImageView img_library;
    private RoundedImageView img_capture;
    private ImageView img_camera_switch;

    private LinearLayout linear_controller_send;
    private ImageView img_cancel;
    private LinearLayout layout_send;
    private ImageView img_send;
    private LottieAnimationView lottie_check;
    private ProgressBar progress_bar;
    private ImageView img_save_image;
    private LinearLayout linear_history;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector cameraSelector;
    private boolean isBackCamera = false; // Flag to check current camera
    private LoginResponse loginResponse;
    private MomentApiService momentApiService;
    private byte[] bytes;
    private ImageCapture imageCapture;
    private String edt_message;

    // New API components
    private ImageUploadService imageUploadService;
    private PostRepository postRepository;
    private com.example.locket.common.repository.MomentRepository momentRepository;
    private SuccessNotificationDialog successDialog;

    private boolean isRecording = false;
    private long captureButtonDownTime = 0;
    private Handler recordHandler = new Handler();
    private Runnable startRecordingRunnable;
    private static final int LONG_PRESS_DURATION = 5000; // 5 giây
    private VideoCapture<Recorder> videoCapture;
    private Recorder recorder;
    private Recording activeRecording;
    private File videoFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        initViews(view);
        onCLick();
        conFigViews();
        checkCameraPermission();
        getDataUser();
    }

    private void getDataUser() {
        loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());

        // Initialize new API services
        imageUploadService = new ImageUploadService(requireContext());
        postRepository = new PostRepository(requireContext());
        momentRepository = new com.example.locket.common.repository.MomentRepository((Application) requireContext().getApplicationContext());
        successDialog = new SuccessNotificationDialog(requireContext());
    }

    private void initViews(View view) {
        camera_view = view.findViewById(R.id.camera_view);
        layout_img_view = view.findViewById(R.id.layout_img_view);
        img_view = view.findViewById(R.id.img_view);
        edt_add_message = view.findViewById(R.id.edt_add_message);

        linear_controller_media = view.findViewById(R.id.linear_controller_media);
        img_library = view.findViewById(R.id.img_library);
        img_capture = view.findViewById(R.id.img_capture);
        img_camera_switch = view.findViewById(R.id.img_camera_switch);

        linear_controller_send = view.findViewById(R.id.linear_controller_send);
        img_cancel = view.findViewById(R.id.img_cancel);
        layout_send = view.findViewById(R.id.layout_send);
        img_send = view.findViewById(R.id.img_send);
        lottie_check = view.findViewById(R.id.lottie_check);
        progress_bar = view.findViewById(R.id.progress_bar);
        img_save_image = view.findViewById(R.id.img_save_image);
        linear_history = view.findViewById(R.id.linear_history);
    }

    private void conFigViews() {
        edt_add_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần thực hiện gì ở đây
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần thực hiện gì ở đây
            }

            @Override
            public void afterTextChanged(Editable s) {
                edt_message = s.toString().trim();
            }
        });
    }

    private void onCLick() {
        img_camera_switch.setOnClickListener(v -> switchCamera());

        img_library.setOnClickListener(view -> openGallery());

        img_cancel.setOnClickListener(view -> {
            bytes = null;
            edt_message = "";
            edt_add_message.setText("");
            layout_img_view.setVisibility(View.GONE);
            camera_view.setVisibility(View.VISIBLE);
            linear_controller_media.setVisibility(View.VISIBLE);
            linear_controller_send.setVisibility(View.GONE);
        });

        img_capture.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    captureButtonDownTime = System.currentTimeMillis();
                    startRecordingRunnable = () -> {
                        if (!isRecording) {
                            startVideoRecording();
                        }
                    };
                    recordHandler.postDelayed(startRecordingRunnable, LONG_PRESS_DURATION);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    recordHandler.removeCallbacks(startRecordingRunnable);
                    if (isRecording) {
                        stopVideoRecording();
                    } else {
                        if (System.currentTimeMillis() - captureButtonDownTime < LONG_PRESS_DURATION) {
                            capturePicture();
                        }
                    }
                    return true;
            }
            return false;
        });

        layout_send.setOnClickListener(view -> sendImage(bytes, edt_message));

    }

    // Open gallery
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraProviderFuture.addListener(this::startCamera, ContextCompat.getMainExecutor(requireContext()));
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(isBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                        .build();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(camera_view.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(800, 800))
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                // Khởi tạo Recorder và VideoCapture mới
                recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Camera initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }


    private void switchCamera() {
        isBackCamera = !isBackCamera; // Toggle the flag
        startCamera(); // Restart camera with the new selection
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraProviderFuture.addListener(this::startCamera, ContextCompat.getMainExecutor(requireContext()));
            } else {
                // Handle the case where the user denied the permission
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Đọc ảnh từ Uri và chuyển thành byte[]
                try {
                    // 🔍 Debug: Log orientation info
                    com.example.locket.common.utils.ImageOrientationUtils.logOrientationInfo(selectedImageUri, requireContext());

                    Uri compressedImageUri = ImageUtils.processImage(requireContext(), selectedImageUri, 50);
                    img_view.setImageURI(compressedImageUri);

                    InputStream inputStream = requireContext().getContentResolver().openInputStream(compressedImageUri);
                    bytes = IOUtils.toByteArray(inputStream);

                    layout_img_view.setVisibility(View.VISIBLE);
                    camera_view.setVisibility(View.GONE);
                    linear_controller_media.setVisibility(View.GONE);
                    linear_controller_send.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void capturePicture() {
        if (imageCapture == null) {
            return;
        }

        // 1. Lấy ảnh preview từ PreviewView (chụp nhanh, dùng để hiển thị ngay lập tức)
        Bitmap previewBitmap = camera_view.getBitmap();

        // 2. Chụp ảnh chất lượng cao bằng ImageCapture (xử lý ở background, upload sau)
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Log.d("Debug", "Ảnh chụp thành công, bắt đầu xử lý...");
                        Bitmap fullBitmap = imageProxyToBitmap(imageProxy);
                        if (fullBitmap == null) {
                            Log.e("Debug", "fullBitmap = null, không thể chuyển đổi!");
                            return;
                        }

                        // 🔧 FIX: Xử lý rotation cho ảnh từ camera
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        boolean isFrontCamera = !isBackCamera;
                        Bitmap rotatedBitmap = ImageUtils.fixCameraImageRotation(fullBitmap, rotationDegrees, isFrontCamera);

                        Log.d("Debug", "Bitmap hợp lệ và đã xoay đúng chiều, bắt đầu chuyển thành byte array...");
                        new Thread(() -> {
                            try {
                                byte[] imageBytes = bitmapToByteArray(rotatedBitmap);
                                Log.d("Debug", "Chuyển đổi thành công, kích thước: " + imageBytes.length + " bytes");

                                // Navigate to PhotoPreviewFragment
                                getActivity().runOnUiThread(() -> {
                                    bytes = imageBytes; // Assign to instance variable on UI thread
                                    navigateToPhotoPreview(previewBitmap, imageBytes);
                                });
                            } catch (Exception e) {
                                Log.e("Debug", "Lỗi khi chuyển đổi Bitmap thành byte array", e);
                            }
                        }).start();
                        imageProxy.close();
                    }
                });
    }

    // Hàm chuyển đổi Bitmap thành byte[]
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(">>>>>>>>>>>>>", "bitmapToByteArray nhận bitmap = null");
            return new byte[0]; // Tránh lỗi null
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }


    // Phương thức chuyển đổi ImageProxy thành Bitmap
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void sendImage(byte[] imageData, String message) {
        if (imageData == null || imageData.length == 0) {
            Toast.makeText(requireContext(), "Không có dữ liệu ảnh để gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading UI
        setLoadingState(true);

        Log.d("LiveCamera", "🚀 Starting new API flow - Upload image first...");

        // Step 1: Upload image to server
        imageUploadService.uploadImage(imageData, new ImageUploadService.UploadCallback() {
            @Override
            public void onUploadComplete(String imageUrl, boolean success) {
                if (success && imageUrl != null) {
                    Log.d("LiveCamera", "✅ Image uploaded successfully: " + imageUrl);
                    // Step 2: Create post with uploaded image URL
                    createPost(imageUrl, message);
                } else {
                    Log.e("LiveCamera", "❌ Image upload failed");
                    getActivity().runOnUiThread(() -> {
                        setLoadingState(false);
                        Toast.makeText(requireContext(), "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onUploadProgress(int progress) {
                Log.d("LiveCamera", "📤 Upload progress: " + progress + "%");
                // You can update a progress bar here if needed
            }

            @Override
            public void onError(String message, int code) {
                Log.e("LiveCamera", "❌ Upload error: " + message + " (Code: " + code + ")");
                getActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(requireContext(), "Lỗi upload: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void createPost(String imageUrl, String caption) {
        Log.d("LiveCamera", "📝 Creating post with imageUrl: " + imageUrl);

        postRepository.createPost(imageUrl, caption, new PostRepository.PostCallback() {
            @Override
            public void onSuccess(PostResponse postResponse) {
                Log.d("LiveCamera", "✅ Post created successfully!");
                
                // 🎯 KEY FIX: Add moment locally with current time for immediate display
                addMomentLocally(imageUrl, caption);
                
                getActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    showSuccessState();
                });
            }

            @Override
            public void onError(String message, int code) {
                Log.e("LiveCamera", "❌ Create post error: " + message + " (Code: " + code + ")");
                getActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(requireContext(), "Tạo bài viết thất bại: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLoading(boolean isLoading) {
                Log.d("LiveCamera", "⏳ Post creation loading: " + isLoading);
                // Loading state is already handled by setLoadingState()
            }
                });
    }

    /*
     * 🎯 Add moment locally with current time for immediate "vừa xong" display
     */
    private void addMomentLocally(String imageUrl, String caption) {
        try {
            // Get current user name from login response
            String currentUserName = loginResponse != null && loginResponse.getUser() != null 
                ? loginResponse.getUser().getUsername() 
                : "You";
            
            // Add moment locally with current time
            momentRepository.addNewMomentLocally(imageUrl, caption, currentUserName);
            Log.d("LiveCamera", "✅ Added moment locally for immediate display");
            
        } catch (Exception e) {
            Log.e("LiveCamera", "❌ Error adding moment locally: " + e.getMessage(), e);
        }
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progress_bar.setVisibility(View.VISIBLE);
            img_cancel.setVisibility(View.GONE);
            img_send.setVisibility(View.GONE);
            img_save_image.setVisibility(View.GONE);
            linear_history.setVisibility(View.GONE);
            edt_add_message.setEnabled(false);
        } else {
            progress_bar.setVisibility(View.GONE);
            img_cancel.setVisibility(View.VISIBLE);
            img_send.setVisibility(View.VISIBLE);
            img_save_image.setVisibility(View.VISIBLE);
            linear_history.setVisibility(View.VISIBLE);
            edt_add_message.setEnabled(true);
        }
    }

    private void showSuccessState() {
        // Hide loading state
        progress_bar.setVisibility(View.GONE);

        // Show custom success dialog
        successDialog.show("Gửi thành công!", "Ảnh của bạn đã được chia sẻ với bạn bè", new SuccessNotificationDialog.OnDismissListener() {
            @Override
            public void onDismiss() {
                // Reset to camera view after dialog dismisses
                resetToCameraView();
            }
        });
    }

    private void resetToCameraView() {
        // Reset data
        bytes = null;
        edt_message = "";
        edt_add_message.setText("");

        // Reset UI to camera view
        edt_add_message.setEnabled(true);
        layout_img_view.setVisibility(View.GONE);
        camera_view.setVisibility(View.VISIBLE);
        linear_controller_media.setVisibility(View.VISIBLE);
        linear_controller_send.setVisibility(View.GONE);
        progress_bar.setVisibility(View.GONE);
        img_cancel.setVisibility(View.VISIBLE);
        img_send.setVisibility(View.VISIBLE);
        lottie_check.setVisibility(View.GONE);
        img_save_image.setVisibility(View.VISIBLE);
        linear_history.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera(); // Hàm để unbind camera khi fragment tạm dừng
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCamera(); // Đảm bảo camera dừng hẳn khi view bị hủy
    }

    private void stopCamera() {
        if (cameraProviderFuture != null) {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll(); // Dừng tất cả các binding liên quan đến camera
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkCameraPermission();
    }

    // Navigate to PhotoPreviewFragment
    private void navigateToPhotoPreview(Bitmap previewBitmap, byte[] imageBytes) {
        if (getParentFragmentManager() != null) {
            PhotoPreviewFragment photoPreviewFragment = PhotoPreviewFragment.newInstance(previewBitmap, imageBytes);
            photoPreviewFragment.setPhotoPreviewListener(new PhotoPreviewFragment.PhotoPreviewListener() {
                @Override
                public void onCancel() {
                    // Go back to camera
                    getParentFragmentManager().popBackStack();
                }

                @Override
                public void onRetake() {
                    // Go back to camera
                    getParentFragmentManager().popBackStack();
                }

                @Override
                public void onSendComplete() {
                    // Go back to camera and reset
                    getParentFragmentManager().popBackStack();
                    resetToCameraView();
                }
            });

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, photoPreviewFragment)
                    .addToBackStack("photo_preview")
                    .commit();
        }
    }

    private void startVideoRecording() {
        isRecording = true;
        img_capture.setBackgroundResource(R.drawable.bg_widget_recording_circle_outline);
        Toast.makeText(getContext(), "Bắt đầu quay video...", Toast.LENGTH_SHORT).show();
        videoFile = new File(getContext().getExternalFilesDir(null), "video_" + System.currentTimeMillis() + ".mp4");
        FileOutputOptions outputOptions = new FileOutputOptions.Builder(videoFile).build();
        PendingRecording pendingRecording = videoCapture.getOutput().prepareRecording(requireContext(), outputOptions);
        // Nếu muốn ghi âm thanh, bỏ comment dòng dưới:
        // pendingRecording = pendingRecording.withAudioEnabled();
        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(requireContext()), new Consumer<VideoRecordEvent>() {
            @Override
            public void accept(VideoRecordEvent event) {
                if (event instanceof VideoRecordEvent.Finalize) {
                    isRecording = false;
                    img_capture.setBackgroundResource(R.drawable.bg_widget_empty_circle_outline);
                    VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) event;
                    if (finalizeEvent.hasError()) {
                        Toast.makeText(getContext(), "Lỗi quay video: " + finalizeEvent.getError(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Đã lưu video: " + videoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        showVideoPreview(videoFile.getAbsolutePath());
                    }
                }
            }
        });
    }
    private void stopVideoRecording() {
        if (isRecording && activeRecording != null) {
            activeRecording.stop();
            isRecording = false;
            img_capture.setBackgroundResource(R.drawable.bg_widget_empty_circle_outline);
        }
    }
    private void showVideoPreview(String videoPath) {
        if (getParentFragmentManager() != null) {
            PhotoPreviewFragment videoPreviewFragment = PhotoPreviewFragment.newInstanceForVideo(videoPath);
            videoPreviewFragment.setPhotoPreviewListener(new PhotoPreviewFragment.PhotoPreviewListener() {
                @Override
                public void onCancel() {
                    getParentFragmentManager().popBackStack();
                }
                @Override
                public void onRetake() {
                    getParentFragmentManager().popBackStack();
                }
                @Override
                public void onSendComplete() {
                    getParentFragmentManager().popBackStack();
                    resetToCameraView();
                }
            });
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, videoPreviewFragment)
                    .addToBackStack("video_preview")
                    .commit();
        }
    }
}
