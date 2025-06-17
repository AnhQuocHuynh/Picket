package com.example.locket.feed;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.locket.R;
import com.example.locket.camera.CameraFragment;

public class HomeFragment extends Fragment {

    private GestureDetectorCompat gestureDetector;
    private NavController navController;
    private Button btnTakePhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);

        // Set up gesture detector for swipe navigation
        gestureDetector = new GestureDetectorCompat(requireContext(), new SwipeGestureListener());

        // Set touch listener on the view to capture swipe gestures
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // Button to take photo
        btnTakePhoto.setOnClickListener(v -> {
            // TODO: Implement camera functionality
            // For now, just show a message
        });
    }

    /**
     * Custom gesture listener to handle swipe gestures for navigation
     */
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // Horizontal swipe detection (left or right)
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Swipe right - navigate to Messages
                            onSwipeRight();
                            result = true;
                        } else {
                            // Swipe left - navigate to History
                            onSwipeLeft();
                            result = true;
                        }
                    }
                }
                // Vertical swipe detection (up)
                else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY < 0) {
                            // Swipe up - navigate to Feed
                            onSwipeUp();
                            result = true;
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    private void onSwipeLeft() {
        // Navigate to History (Profile) screen
        navController.navigate(R.id.historyFragment);
    }

    private void onSwipeRight() {
        // Navigate to Messages screen
        navController.navigate(R.id.messageFragment);
    }

    private void onSwipeUp() {
        // Navigate to Feed screen
        navController.navigate(R.id.feedFragment);
    }
}
