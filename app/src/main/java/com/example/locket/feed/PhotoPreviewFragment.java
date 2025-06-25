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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.Address;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class PhotoPreviewFragment extends Fragment {

    private ImageView imageView;
    private ImageButton btnPost, btnCancel, btnDownload;
    private NavController navController;
    private String message = "";
    private FusedLocationProviderClient fusedLocationClient;

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
        btnPost = view.findViewById(R.id.btnPost);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDownload = view.findViewById(R.id.btnDownload);
        EditText etCaption = view.findViewById(R.id.etCaption);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        String imageUri = getArguments() != null ? getArguments().getString("image_uri") : null;
        if (imageUri != null) {
            imageView.setImageURI(Uri.parse(imageUri));
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
                int drawableEnd = 2; // 0: left, 1: top, 2: right, 3: bottom
                if (etCaption.getCompoundDrawables()[drawableEnd] != null) {
                    int iconStart = etCaption.getWidth() - etCaption.getPaddingRight() - etCaption.getCompoundDrawables()[drawableEnd].getBounds().width();
                    if (event.getX() >= iconStart) {
                        // Bấm vào icon location
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
                        } else {
                            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                                if (location != null) {
                                    try {
                                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                        if (addresses != null && !addresses.isEmpty()) {
                                            String place = addresses.get(0).getFeatureName();
                                            if (place == null) place = addresses.get(0).getLocality();
                                            if (place == null) place = addresses.get(0).getAdminArea();
                                            if (place == null) place = addresses.get(0).getCountryName();
                                            if (place != null) {
                                                String current = etCaption.getText().toString();
                                                if (!current.contains(place)) {
                                                    if (!current.isEmpty()) current += " ";
                                                    etCaption.setText(current + place);
                                                    etCaption.setSelection(etCaption.getText().length());
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "Lỗi lấy địa điểm", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Không lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        return true;
                    }
                }
            }
            return false;
        });

        btnPost.setOnClickListener(v -> {
            message = etCaption.getText().toString().trim();
            Toast.makeText(getContext(), "Đăng ảnh!", Toast.LENGTH_SHORT).show();
            // TODO: Gửi ảnh và message nếu cần
            navController.popBackStack();
        });

        btnCancel.setOnClickListener(v -> navController.popBackStack());

        btnDownload.setOnClickListener(v -> {
            if (getContext() == null) return;
            if (imageUri == null) {
                Toast.makeText(getContext(), "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Uri uri = Uri.parse(imageUri);
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) throw new IOException("Không thể mở ảnh");

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "Locket_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Locket");

                Uri outUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (outUri == null) throw new IOException("Không thể lưu ảnh");

                OutputStream outputStream = getContext().getContentResolver().openOutputStream(outUri);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();

                Toast.makeText(getContext(), "Đã lưu ảnh vào thư viện!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lưu ảnh thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
