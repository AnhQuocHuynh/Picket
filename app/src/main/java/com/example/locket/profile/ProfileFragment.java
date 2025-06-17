package com.example.locket.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.Photo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements PhotosAdapter.OnPhotoClickListener {

    private RecyclerView rvPhotoHistory;
    private CircleImageView ivUserAvatar;
    private TextView tvUsername;
    private Button btnLogout;
    private PhotosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        rvPhotoHistory = view.findViewById(R.id.rvPhotoHistory);
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Setup user info
        tvUsername.setText("John Doe"); // Replace with actual username

        // Load avatar with Glide
        Glide.with(requireContext())
                .load(R.drawable.default_avatar) // Replace with actual user avatar
                .into(ivUserAvatar);

        // Setup logout button
        btnLogout.setOnClickListener(v -> {
            // TODO: Implement logout functionality
            // For now, navigate to login screen
            if (getActivity() != null) {
                Navigation.findNavController(view).navigate(R.id.loginFragment);
            }
        });

        // Setup RecyclerView
        setupPhotoGrid();

        // Load sample data for demo
        loadSamplePhotos();
    }

    private void setupPhotoGrid() {
        // Create a grid layout manager with span count of 4
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);

        // Configure layout manager to handle full width for headers
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Headers take full width, photos take 1 span
                return adapter.getItemViewType(position) == 0 ? 7 : 1;
            }
        });

        rvPhotoHistory.setLayoutManager(layoutManager);

        // Set up adapter
        adapter = new PhotosAdapter(requireContext(), this);
        rvPhotoHistory.setAdapter(adapter);
    }

    private void loadSamplePhotos() {
        // Create sample photo data
        List<Photo> photos = new ArrayList<>();

        // Generate some sample photos for June 2025
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JUNE, 1);

        // June photos
        for (int i = 1; i <= 30; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            Photo photo = new Photo("photo_june_" + i,
                    "https://picsum.photos/id/1/300/300", cal.getTime());
            photos.add(photo);
        }

        // May photos
        cal.set(2025, Calendar.MAY, 1);
        for (int i = 15; i <= 25; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            Photo photo = new Photo("photo_may_" + i,
                    "https://fastly.picsum.photos/id/0" + (i + 30) + "/300/300", cal.getTime());
            photos.add(photo);
        }

        // April photos
        cal.set(2025, Calendar.APRIL, 1);
        for (int i = 5; i <= 12; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            Photo photo = new Photo("photo_april_" + i,
                    "https://picsum.photos/id/2" + (i + 50) + "/300/300", cal.getTime());
            photos.add(photo);
        }

        // Set photos to adapter
        adapter.setPhotosWithHeaders(photos);
    }

    @Override
    public void onPhotoClick(Photo photo) {
        // Navigate to photo detail screen
        Bundle args = new Bundle();
        args.putString("photo_url", photo.getImageUrl());
        args.putLong("photo_date", photo.getDate().getTime());

        // Navigate to photo detail fragment
        Navigation.findNavController(requireView()).navigate(R.id.photoDetailFragment, args);
    }
}
