package com.example.locket.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.locket.R;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private ViewPager2 viewPagerFeed;
    private FeedAdapter feedAdapter;
    private List<Post> postList;

    private ImageView btnGridView;
    private ImageView btnCamera;
    private ImageView btnShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho fragment này
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ view
        viewPagerFeed = view.findViewById(R.id.viewPagerFeed);
        btnGridView = view.findViewById(R.id.btnGridView);
        btnCamera = view.findViewById(R.id.btnCamera);
        btnShare = view.findViewById(R.id.btnShare);

        // Tạo dữ liệu mẫu
        createSamplePosts();

        // Cài đặt adapter
        feedAdapter = new FeedAdapter(postList);
        viewPagerFeed.setAdapter(feedAdapter);

        // Cài đặt sự kiện click
        setupClickListeners();
    }

    private void createSamplePosts() {
        postList = new ArrayList<>();
        // Lưu ý: URL ảnh và avatar hiện tại chỉ là chuỗi giữ chỗ.
        // Bạn cần thay thế bằng URL thật và dùng thư viện Glide/Picasso để tải ảnh.
        postList.add(new Post("url1", "Đi chơi với bạn bè!", "avatar_url1", "Alice", "5 phút"));
        postList.add(new Post("url2", "Hoàng hôn thật đẹp.", "avatar_url2", "Bob", "12 phút"));
        postList.add(new Post("url3", "Cún con mới của mình.", "avatar_url3", "Charlie", "30 phút"));
        postList.add(new Post("url4", "Bữa tối ngon tuyệt.", "avatar_url4", "Diana", "1 giờ"));
    }

    private void setupClickListeners() {
        btnGridView.setOnClickListener(v -> {
            // Xử lý sự kiện xem dạng lưới
            Toast.makeText(getContext(), "Chuyển sang dạng lưới", Toast.LENGTH_SHORT).show();
        });

        btnCamera.setOnClickListener(v -> {
            // Xử lý sự kiện mở camera
            Toast.makeText(getContext(), "Mở camera", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            // Xử lý sự kiện chia sẻ
            Toast.makeText(getContext(), "Chia sẻ", Toast.LENGTH_SHORT).show();
        });
    }
}
