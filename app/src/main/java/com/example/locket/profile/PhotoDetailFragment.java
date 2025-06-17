package com.example.locket.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.Photo;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PhotoDetailFragment extends Fragment {

    private static final String ARG_PHOTO_URL = "photo_url";
    private static final String ARG_PHOTO_DATE = "photo_date";

    private ImageView ivFullPhoto;
    private TextView tvPhotoDate;
    private ImageButton btnBack;

    public static PhotoDetailFragment newInstance(String photoUrl, long photoDate) {
        PhotoDetailFragment fragment = new PhotoDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_URL, photoUrl);
        args.putLong(ARG_PHOTO_DATE, photoDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        ivFullPhoto = view.findViewById(R.id.ivFullPhoto);
        tvPhotoDate = view.findViewById(R.id.tvPhotoDate);
        btnBack = view.findViewById(R.id.btnBack);

        // Get arguments
        if (getArguments() != null) {
            String photoUrl = getArguments().getString(ARG_PHOTO_URL);
            long photoDate = getArguments().getLong(ARG_PHOTO_DATE);

            // Load image with Glide
            Glide.with(requireContext())
                 .load(photoUrl)
                 .placeholder(R.drawable.placeholder_image)
                 .into(ivFullPhoto);

            // Format and display date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            tvPhotoDate.setText(dateFormat.format(photoDate));
        }

        // Set click listener for back button
        btnBack.setOnClickListener(v -> {
            // Navigate back to profile
            Navigation.findNavController(view).navigateUp();
        });
    }
}
