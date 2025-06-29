package com.example.locket.feed;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.VideoView;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.Address;
import android.location.LocationManager;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.locket.R;
import com.example.locket.drawing.DrawingView;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import android.widget.FrameLayout;
import android.provider.Settings;
import android.content.Intent;

public class PhotoPreviewFragment extends Fragment {

    private ImageView imageView;
    private VideoView videoView;
    private DrawingView drawingViewReplay;
    private ImageButton btnPost, btnCancel, btnDownload, btnPlayReplay, btnPlayReplayOverlay;
    private NavController navController;
    private String message = "";
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isVideo = false;
    private boolean isDrawing = false;
    private String mediaUri = null;
    private boolean isReplaying = false;

    // Chức năng chọn người xem
    private ImageView receiverEveryone, receiverBin1, receiverBin2;
    private String selectedReceiver = "Everyone"; // Mặc định là Everyone
    private boolean everyoneSelected = true;
    private boolean bin1Selected = false;
    private boolean bin2Selected = false;

    // Danh sách người được chọn
    private java.util.Set<String> selectedReceivers = new java.util.HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        imageView = view.findViewById(R.id.imageViewPreview);
        videoView = view.findViewById(R.id.videoViewPreview);
        drawingViewReplay = view.findViewById(R.id.drawingViewReplay);
        btnPost = view.findViewById(R.id.btnPost);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDownload = view.findViewById(R.id.btnDownload);
        btnPlayReplay = view.findViewById(R.id.btnPlayReplay);
        btnPlayReplayOverlay = view.findViewById(R.id.btnPlayReplayOverlay);
        EditText etCaption = view.findViewById(R.id.etCaption);

        // Khởi tạo các view chọn người xem
        receiverEveryone = view.findViewById(R.id.receiverEveryone);
        receiverBin1 = view.findViewById(R.id.receiverBin1);
        receiverBin2 = view.findViewById(R.id.receiverBin2);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Lấy thông tin từ arguments
        Bundle args = getArguments();
        if (args != null) {
            mediaUri = args.getString("image_uri");
            if (mediaUri == null) {
                mediaUri = args.getString("video_uri");
            }
            isVideo = args.getBoolean("is_video", false);
            isDrawing = args.getBoolean("is_drawing", false);
        }

        if (mediaUri != null) {
            Uri uri = Uri.parse(mediaUri);
            if (isVideo) {
                // Hiển thị video
                imageView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                drawingViewReplay.setVisibility(View.GONE);
                videoView.setVideoURI(uri);

                // Set video để khít khung
                videoView.setOnPreparedListener(mp -> {
                    // Tự động phát video khi đã sẵn sàng
                    videoView.start();

                    // Điều chỉnh video để khít khung
                    float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                    float screenRatio = videoView.getWidth() / (float) videoView.getHeight();

                    if (videoRatio > screenRatio) {
                        // Video rộng hơn, scale theo width
                        videoView.setLayoutParams(new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                    } else {
                        // Video cao hơn, scale theo height
                        videoView.setLayoutParams(new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        ));
                    }
                });
                videoView.setOnCompletionListener(mp -> {
                    // Lặp lại video
                    videoView.start();
                });
            } else {
                // Hiển thị ảnh
                imageView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                drawingViewReplay.setVisibility(View.GONE);
                imageView.setImageURI(uri);

                // Hiển thị nút play nếu là tranh vẽ
                if (isDrawing) {
                    btnPlayReplayOverlay.setVisibility(View.VISIBLE);
                    setupDrawingReplay();
                } else {
                    btnPlayReplayOverlay.setVisibility(View.GONE);
                }
            }
        }

        // Nếu có message trước đó thì set lại
        etCaption.setText(message);

        // Khi nhấn Enter trên bàn phím
        etCaption.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etCaption.clearFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(etCaption.getWindowToken(), 0);
                message = etCaption.getText().toString().trim();
                return true;
            }
            return false;
        });

        // Khi mất focus thì cũng lưu lại message và ẩn bàn phím
        etCaption.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                message = etCaption.getText().toString().trim();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(etCaption.getWindowToken(), 0);
            }
        });

        // Xử lý bấm vào icon location
        etCaption.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                // Lấy drawable bên phải (icon location)
                android.graphics.drawable.Drawable[] drawables = etCaption.getCompoundDrawables();
                android.graphics.drawable.Drawable drawableEnd = drawables[2]; // 2 = right

                if (drawableEnd != null) {
                    // Tính toán vị trí icon
                    int iconStart = etCaption.getWidth() - etCaption.getPaddingRight() - drawableEnd.getIntrinsicWidth();
                    int iconEnd = etCaption.getWidth() - etCaption.getPaddingRight();

                    // Kiểm tra xem có bấm vào icon không
                    if (event.getX() >= iconStart && event.getX() <= iconEnd) {
                        // Bấm vào icon location
                        requestLocationPermission();
                        return true;
                    }
                }
            }
            return false;
        });

        btnPost.setOnClickListener(v -> {
            message = etCaption.getText().toString().trim();

            // Kiểm tra xem có phải là tranh vẽ không
            boolean isDrawing = getArguments() != null && getArguments().getBoolean("is_drawing", false);

            // Hiển thị thông tin về người được chọn
            String receiverInfo = "Người xem: " + getSelectedReceiversText();

            if (isVideo) {
                Toast.makeText(getContext(), "Đăng video! " + receiverInfo, Toast.LENGTH_SHORT).show();
            } else if (isDrawing) {
                Toast.makeText(getContext(), "Đăng tranh vẽ! " + receiverInfo, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Đăng ảnh! " + receiverInfo, Toast.LENGTH_SHORT).show();
            }

            // TODO: Gửi media và message nếu cần

            // Nếu là tranh vẽ, quay về HomeFragment
            if (isDrawing) {
                navController.navigate(R.id.homeFragment);
            } else {
                navController.popBackStack();
            }
        });

        btnCancel.setOnClickListener(v -> navController.popBackStack());

        btnDownload.setOnClickListener(v -> {
            if (getContext() == null) return;
            if (mediaUri == null) {
                Toast.makeText(getContext(), "Không tìm thấy media", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Uri uri = Uri.parse(mediaUri);
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) throw new IOException("Không thể mở media");

                ContentValues values = new ContentValues();
                if (isVideo) {
                    values.put(MediaStore.Video.Media.DISPLAY_NAME, "Locket_Video_" + System.currentTimeMillis() + ".mp4");
                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                    values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Locket");
                } else {
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, "Locket_Image_" + System.currentTimeMillis() + ".jpg");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Locket");
                }

                Uri outUri;
                if (isVideo) {
                    outUri = getContext().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    outUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }

                if (outUri == null) throw new IOException("Không thể lưu media");

                OutputStream outputStream = getContext().getContentResolver().openOutputStream(outUri);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();

                String mediaType = isVideo ? "video" : "ảnh";
                Toast.makeText(getContext(), "Đã lưu " + mediaType + " vào thư viện!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                String mediaType = isVideo ? "video" : "ảnh";
                Toast.makeText(getContext(), "Lưu " + mediaType + " thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlayReplayOverlay.setOnClickListener(v -> {
            if (isDrawing) {
                if (isReplaying) {
                    // Đang replay thì dừng và hiển thị ảnh tĩnh
                    drawingViewReplay.stopReplay();
                    btnPlayReplayOverlay.setImageResource(R.drawable.ic_play);
                    isReplaying = false;

                    // Chuyển về hiển thị ảnh tĩnh
                    imageView.setVisibility(View.VISIBLE);
                    drawingViewReplay.setVisibility(View.GONE);
                } else {
                    // Bắt đầu replay và hiển thị DrawingView
                    imageView.setVisibility(View.GONE);
                    drawingViewReplay.setVisibility(View.VISIBLE);

                    drawingViewReplay.startReplay();
                    btnPlayReplayOverlay.setImageResource(R.drawable.ic_pause);
                    isReplaying = true;
                }
            }
        });

        // Thiết lập sự kiện click cho chọn người xem
        setupReceiverSelection();

        // Khởi tạo trạng thái ban đầu
        selectedReceivers.add("Everyone");
        updateReceiverUI();
    }

    private void setupReceiverSelection() {
        // Sự kiện click cho Everyone
        receiverEveryone.setOnClickListener(v -> {
            toggleReceiver("Everyone");
        });

        // Sự kiện click cho Bin1
        receiverBin1.setOnClickListener(v -> {
            toggleReceiver("Bin1");
        });

        // Sự kiện click cho Bin2
        receiverBin2.setOnClickListener(v -> {
            toggleReceiver("Bin2");
        });
    }

    private void toggleReceiver(String receiver) {
        if (selectedReceivers.contains(receiver)) {
            // Nếu đã được chọn thì bỏ chọn
            selectedReceivers.remove(receiver);

            // Nếu bỏ chọn Everyone và không còn ai được chọn, thì chọn lại Everyone
            if (receiver.equals("Everyone") && selectedReceivers.isEmpty()) {
                selectedReceivers.add("Everyone");
            }
        } else {
            // Nếu chưa được chọn thì thêm vào
            selectedReceivers.add(receiver);

            // Nếu chọn Everyone, bỏ tất cả người khác
            if (receiver.equals("Everyone")) {
                selectedReceivers.clear();
                selectedReceivers.add("Everyone");
            } else {
                // Nếu chọn người khác, bỏ Everyone
                selectedReceivers.remove("Everyone");

                // Nếu đã chọn hết Bin1 và Bin2, tự động chuyển về Everyone
                if (selectedReceivers.contains("Bin1") && selectedReceivers.contains("Bin2")) {
                    selectedReceivers.clear();
                    selectedReceivers.add("Everyone");
                }
            }
        }

        // Cập nhật trạng thái boolean
        updateBooleanStates();

        // Cập nhật giao diện
        updateReceiverUI();
    }

    private void updateBooleanStates() {
        everyoneSelected = selectedReceivers.contains("Everyone");
        bin1Selected = selectedReceivers.contains("Bin1");
        bin2Selected = selectedReceivers.contains("Bin2");
    }

    private void updateReceiverUI() {
        // Cập nhật Everyone
        if (everyoneSelected) {
            receiverEveryone.setBackgroundResource(R.drawable.circle_orange_border);
        } else {
            receiverEveryone.setBackgroundResource(R.drawable.circle_gray_border);
        }

        // Cập nhật Bin1
        if (bin1Selected) {
            receiverBin1.setBackgroundResource(R.drawable.circle_orange_border);
        } else {
            receiverBin1.setBackgroundResource(R.drawable.circle_gray_border);
        }

        // Cập nhật Bin2
        if (bin2Selected) {
            receiverBin2.setBackgroundResource(R.drawable.circle_orange_border);
        } else {
            receiverBin2.setBackgroundResource(R.drawable.circle_gray_border);
        }
    }

    private void setupDrawingReplay() {
        // Thiết lập listener cho DrawingView
        drawingViewReplay.setOnReplayListener(new DrawingView.OnReplayListener() {
            @Override
            public void onReplayFinished() {
                // Khi replay kết thúc, reset về trạng thái ban đầu
                isReplaying = false;
                btnPlayReplayOverlay.setImageResource(R.drawable.ic_play);

                // Chuyển về hiển thị ảnh tĩnh
                imageView.setVisibility(View.VISIBLE);
                drawingViewReplay.setVisibility(View.GONE);
            }
        });

        // Load dữ liệu replay từ DrawingFragment
        Bundle args = getArguments();
        if (args != null && args.getBoolean("is_drawing", false)) {
            try {
                @SuppressWarnings("unchecked")
                List<DrawingView.DrawingPath> replayPaths =
                        (List<DrawingView.DrawingPath>) args.getSerializable("replay_paths");
                if (replayPaths != null && !replayPaths.isEmpty()) {
                    drawingViewReplay.loadReplayData(replayPaths);
                }
            } catch (Exception e) {
                // Nếu không load được dữ liệu replay, vẫn hiển thị ảnh tĩnh
                e.printStackTrace();
            }
        }
    }

    private String getSelectedReceiversText() {
        StringBuilder receiverInfo = new StringBuilder();
        for (String receiver : selectedReceivers) {
            receiverInfo.append(receiver).append(", ");
        }
        if (receiverInfo.length() > 2) {
            receiverInfo.setLength(receiverInfo.length() - 2);
        }
        return receiverInfo.toString();
    }

    private void requestLocationPermission() {
        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        // Kiểm tra GPS có được bật không
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS chưa được bật, hiển thị dialog hướng dẫn
            new AlertDialog.Builder(requireContext())
                    .setTitle("GPS chưa được bật")
                    .setMessage("Để lấy vị trí chính xác, vui lòng bật GPS trong cài đặt.")
                    .setPositiveButton("Mở cài đặt", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            return;
        }

        // Tất cả điều kiện đã thỏa mãn, lấy vị trí
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Cần quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        Toast.makeText(getContext(), "Đang lấy vị trí...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getAddressFromLocation(location);
                    } else {
                        Toast.makeText(getContext(), "Không lấy được vị trí hiện tại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getAddressFromLocation(Location location) {
        try {
            // Kiểm tra xem Geocoder có khả dụng không
            if (!Geocoder.isPresent()) {
                Toast.makeText(getContext(), "Geocoder không khả dụng trên thiết bị này", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị loading
            Toast.makeText(getContext(), "Đang lấy địa điểm...", Toast.LENGTH_SHORT).show();

            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Ưu tiên lấy tên địa điểm theo thứ tự
                String place = null;
                if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
                    place = address.getFeatureName();
                } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    place = address.getLocality();
                } else if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
                    place = address.getSubLocality();
                } else if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
                    place = address.getAdminArea();
                } else if (address.getCountryName() != null && !address.getCountryName().isEmpty()) {
                    place = address.getCountryName();
                }

                if (place != null && !place.isEmpty()) {
                    EditText etCaption = getView().findViewById(R.id.etCaption);
                    if (etCaption != null) {
                        String current = etCaption.getText().toString();
                        if (!current.contains(place)) {
                            if (!current.isEmpty()) current += " ";
                            etCaption.setText(current + place);
                            etCaption.setSelection(etCaption.getText().length());
                            Toast.makeText(getContext(), "Đã thêm địa điểm: " + place, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Địa điểm đã có trong caption", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy tên địa điểm", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Không tìm thấy địa điểm cho vị trí này", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi lấy địa điểm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, lấy vị trí
                getCurrentLocation();
            } else {
                // Quyền bị từ chối
                Toast.makeText(getContext(), "Cần quyền truy cập vị trí để lấy địa điểm", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
