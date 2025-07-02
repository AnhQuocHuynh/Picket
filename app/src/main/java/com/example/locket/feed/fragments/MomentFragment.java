package com.example.locket.feed.fragments;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.locket.R;
import com.example.locket.common.repository.viewmodels.MomentViewModel;

public class MomentFragment extends Fragment {
    private static final String TAG = "MomentFragment";
    private MomentViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Fragment created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_moment, container, false);
        
        // Initialize ViewModel to trigger data loading
        viewModel = new ViewModelProvider(this).get(MomentViewModel.class);
        
        // Observe data for debugging
        viewModel.getAllMoments().observe(getViewLifecycleOwner(), momentEntities -> {
            Log.d(TAG, "Received moments: " + (momentEntities != null ? momentEntities.size() : 0));
            if (momentEntities != null && !momentEntities.isEmpty()) {
                Log.d(TAG, "First moment: " + momentEntities.get(0).getUser() + " - " + momentEntities.get(0).getCaption());
            }
        });
        
        return view;
    }
}
