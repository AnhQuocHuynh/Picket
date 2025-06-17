package com.example.locket.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;

public class ProfileFragment extends Fragment {

    private RecyclerView rvPhotoHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPhotoHistory = view.findViewById(R.id.rvPhotoHistory);
        // Use a grid layout for the photo history
        rvPhotoHistory.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        // TODO: Set up history adapter with photos
    }
}
