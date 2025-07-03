package com.example.locket.feed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.example.locket.R;
import com.example.locket.feed.adapters.CategoryFilterAdapter;
import com.example.locket.feed.adapters.ViewAllMomentAdapter;
import com.example.locket.feed.adapters.ViewMomentAdapter;
import com.example.locket.common.repository.viewmodels.MomentViewModel;
import com.example.locket.common.database.entities.MomentEntity;
import com.example.locket.common.repository.MomentRepository;
import com.example.locket.common.models.post.CategoriesResponse;
import com.example.locket.common.models.post.CommentRequest;
import com.example.locket.common.models.post.CommentResponse;
import com.example.locket.common.network.PostApiService;
import com.example.locket.common.network.client.AuthApiClient;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.models.auth.LoginResponse;
import com.makeramen.roundedimageview.RoundedImageView;
import com.example.locket.chat.MessageThreadManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewMomentFragment extends Fragment {
    private RelativeLayout relative_view_all_moment;
    private RelativeLayout relative_view_moment;
    private RecyclerView rv_view_moment;
    private RecyclerView rv_category_filter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewMomentAdapter viewMomentAdapter;
    private ViewAllMomentAdapter viewAllMomentAdapter;
    private CategoryFilterAdapter categoryFilterAdapter;
    private MomentViewModel viewModel;
    
    // Message sending components
    private EditText txt_send_message;
    private LinearLayout layout_send_message;
    private ImageView img_send_message;
    private LinearLayout layout_send;
    private ImageView img_send;
    private ProgressBar progress_bar;
    private LottieAnimationView lottie_check;
    private PostApiService postApiService;
    private LoginResponse loginResponse;
    private LinearLayoutManager layoutManager;
    private PagerSnapHelper snapHelper;
    
    // Message thread integration
    private MessageThreadManager messageThreadManager;
    
    // Data for filtering
    private List<MomentEntity> allMoments = new ArrayList<>();
    private String currentSelectedCategory = "Tất cả";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_moment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các view
        rv_view_moment = view.findViewById(R.id.rv_view_moment);
        RecyclerView rv_view_all_moment = view.findViewById(R.id.rv_view_all_moment);
        rv_category_filter = view.findViewById(R.id.rv_category_filter);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        RoundedImageView img_capture = view.findViewById(R.id.img_capture);
        ImageView img_all_moment = view.findViewById(R.id.img_all_moment);
        ImageView img_back_all_moments = view.findViewById(R.id.img_back_all_moments);
        relative_view_all_moment = view.findViewById(R.id.relative_view_all_moment);
        relative_view_moment = view.findViewById(R.id.relative_view_moment);

        // Initialize message sending components
        txt_send_message = view.findViewById(R.id.txt_send_message);
        layout_send_message = view.findViewById(R.id.layout_send_message);
        img_send_message = view.findViewById(R.id.img_send_message);
        layout_send = view.findViewById(R.id.layout_send);
        img_send = view.findViewById(R.id.img_send);
        progress_bar = view.findViewById(R.id.progress_bar);
        lottie_check = view.findViewById(R.id.lottie_check);

        // Initialize API service and user info
        postApiService = AuthApiClient.getAuthClient().create(PostApiService.class);
        loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MomentViewModel.class);

        // Initialize MessageThreadManager
        messageThreadManager = new MessageThreadManager(requireContext());
        
        // Test integration on debug builds
        if (android.util.Log.isLoggable("ViewMomentFragment", android.util.Log.DEBUG)) {
            com.example.locket.chat.MomentCommentIntegrationHelper.logIntegrationStatus();
            com.example.locket.chat.MomentCommentIntegrationHelper.isIntegrationReady(requireContext());
        }

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("ViewMomentFragment", "Pull to refresh triggered");
            viewModel.refreshData();
        });

        // Thiết lập RecyclerView cho chế độ xem từng moment (vertical, như ViewPager)
        layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        rv_view_moment.setLayoutManager(layoutManager);
        // Khởi tạo adapter với danh sách ban đầu là rỗng
        viewMomentAdapter = new ViewMomentAdapter(requireContext(), new ArrayList<>());
        rv_view_moment.setAdapter(viewMomentAdapter);
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rv_view_moment);

        // Thiết lập RecyclerView cho chế độ xem tất cả các moment (grid 3 cột)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        rv_view_all_moment.setLayoutManager(gridLayoutManager);
        viewAllMomentAdapter = new ViewAllMomentAdapter(new ArrayList<>(), requireContext());
        rv_view_all_moment.setAdapter(viewAllMomentAdapter);

        // Setup category filter
        setupCategoryFilter();

        // Setup ViewAllMomentAdapter click listeners
        setupViewAllMomentAdapterListeners();

        // Setup message sending functionality
        setupMessageSending();

        // Sự kiện khi nhấn nút capture: chuyển về trang LiveCameraFragment
        img_capture.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            if (viewPager != null) {
                viewPager.setCurrentItem(0, true);
                sendPositionSwipeViewpage2(0);
            } else {
                Log.e("ViewMomentFragment", "ViewPager2 not found!");
            }
        });

        // Sự kiện khi nhấn nút xem all moment: hiển thị grid view
        img_all_moment.setOnClickListener(v -> {
            relative_view_all_moment.setVisibility(View.VISIBLE);
            relative_view_moment.setVisibility(View.GONE);
            // Reset filter về "Tất cả" khi mở view
            currentSelectedCategory = "Tất cả";
            // Set to position 0 only if there are categories available
            if (categoryFilterAdapter.getItemCount() > 0) {
                categoryFilterAdapter.setSelectedPosition(0);
            }
            filterMomentsByCategory(currentSelectedCategory);
            sendPositionSwipeViewpage2(0);
        });

        // Sự kiện khi nhấn nút back từ all moments view
        img_back_all_moments.setOnClickListener(v -> {
            relative_view_all_moment.setVisibility(View.GONE);
            relative_view_moment.setVisibility(View.VISIBLE);
        });

        // Lắng nghe sự kiện cuộn của RecyclerView view moment để gửi vị trí hiện tại
        rv_view_moment.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // Khi cuộn dừng
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int position = layoutManager.getPosition(centerView);
                        Log.d("RecyclerView", "Item hiện tại: " + position);
                        sendPositionSwipeViewpage2(position);
                    }
                }
            }
        });

        // 📊 Quan sát LiveData từ Repository để cập nhật UI
        observeData();
    }

    private void observeData() {
        viewModel.getAllMoments().observe(getViewLifecycleOwner(), momentEntities -> {
            // Stop refresh animation
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }

            Log.d("ViewMomentFragment", "Received " + (momentEntities != null ? momentEntities.size() : 0) + " moments");
            
            // Cập nhật dữ liệu cho cả hai adapter
            if (momentEntities != null) {
                allMoments = momentEntities;
                viewMomentAdapter.setFilterList(momentEntities);
                
                // Update category counts and filter
                updateCategoryCounts();
                filterMomentsByCategory(currentSelectedCategory);
            }
        });
    }

    // Phương thức gửi vị trí cuộn qua LocalBroadcast (sử dụng cho các thành phần khác nếu cần)
    private void sendPositionSwipeViewpage2(int position) {
        Intent intent = new Intent("send_position_swipe_viewpage2");
        intent.putExtra("position", position);
        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        relative_view_all_moment.setVisibility(View.GONE);
        relative_view_moment.setVisibility(View.VISIBLE);
        rv_view_moment.scrollToPosition(0);
    }

    /**
     * Setup category filter RecyclerView and adapter
     */
    private void setupCategoryFilter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), 
                LinearLayoutManager.HORIZONTAL, false);
        rv_category_filter.setLayoutManager(layoutManager);

        // Initialize with empty categories first (will be populated when data loads)
        List<CategoryFilterAdapter.CategoryItem> emptyCategories = new ArrayList<>();
        categoryFilterAdapter = new CategoryFilterAdapter(emptyCategories, requireContext());
        rv_category_filter.setAdapter(categoryFilterAdapter);

        categoryFilterAdapter.setOnCategorySelectedListener((category, position) -> {
            Log.d("CategoryFilter", "Selected category: " + category + " at position: " + position);
            currentSelectedCategory = category;
            filterMomentsByCategory(category);
        });

        // Fetch real categories from API
        fetchCategoriesFromAPI();
    }

    /**
     * 🏷️ Fetch categories from API and update filter bar
     */
    private void fetchCategoriesFromAPI() {
        viewModel.fetchAvailableCategories(new MomentRepository.CategoriesCallback() {
            @Override
            public void onCategoriesReceived(List<CategoriesResponse.CategoryData> apiCategories) {
                if (apiCategories != null && !apiCategories.isEmpty()) {
                    // Convert API categories to CategoryFilterAdapter.CategoryItem
                    List<CategoryFilterAdapter.CategoryItem> filterCategories = convertApiCategoriesToFilterItems(apiCategories);
                    
                    Log.d("CategoryFilter", "Received " + filterCategories.size() + " categories from API");
                    
                    // Update the filter adapter with real data
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            categoryFilterAdapter.updateCategories(filterCategories);
                        });
                    }
                } else {
                    Log.w("CategoryFilter", "No categories received from API, will use local data when available");
                }
            }

            @Override
            public void onError(String error) {
                Log.e("CategoryFilter", "Failed to fetch categories: " + error);
                // Will fall back to local categorization when moments are loaded
            }
        });
    }

    /**
     * 🔄 Convert API CategoryData to CategoryFilterAdapter.CategoryItem (only categories with images)
     */
    private List<CategoryFilterAdapter.CategoryItem> convertApiCategoriesToFilterItems(List<CategoriesResponse.CategoryData> apiCategories) {
        List<CategoryFilterAdapter.CategoryItem> filterItems = new ArrayList<>();
        
        for (CategoriesResponse.CategoryData apiCategory : apiCategories) {
            // Only add categories that have images (count > 0)
            if (apiCategory.getCount() > 0) {
                CategoryFilterAdapter.CategoryItem filterItem = new CategoryFilterAdapter.CategoryItem(
                    apiCategory.getLabel(),  // Use label as display name
                    apiCategory.getIcon(),   // Use API icon
                    apiCategory.getCount()   // Use API count
                );
                filterItems.add(filterItem);
            }
        }
        
        Log.d("CategoryFilter", "Converted " + filterItems.size() + " categories with images from API");
        return filterItems;
    }

    /**
     * Setup click listeners for ViewAllMomentAdapter
     */
    private void setupViewAllMomentAdapterListeners() {
        viewAllMomentAdapter.setOnMomentClickListener(new ViewAllMomentAdapter.OnMomentClickListener() {
            @Override
            public void onMomentClick(MomentEntity moment, int position) {
                // Navigate back to moment view and show specific moment
                relative_view_all_moment.setVisibility(View.GONE);
                relative_view_moment.setVisibility(View.VISIBLE);
                
                // Find position in the full list and scroll to it
                int momentPosition = findMomentPositionInFullList(moment);
                if (momentPosition >= 0) {
                    rv_view_moment.scrollToPosition(momentPosition);
                }
            }

            @Override
            public void onCategoryClick(String category) {
                // Filter by the clicked category
                currentSelectedCategory = category;
                // Update the filter bar selection
                updateFilterSelection(category);
                filterMomentsByCategory(category);
            }
        });
    }

    /**
     * Filter moments by category
     */
    private void filterMomentsByCategory(String category) {
        if (allMoments == null || allMoments.isEmpty()) {
            return;
        }

        List<MomentEntity> filteredMoments;
        
        if ("Tất cả".equals(category)) {
            filteredMoments = new ArrayList<>(allMoments);
        } else {
            filteredMoments = allMoments.stream()
                    .filter(moment -> {
                        String momentCategory = extractCategoryFromMoment(moment);
                        return category.equals(momentCategory);
                    })
                    .collect(Collectors.toList());
        }

        viewAllMomentAdapter.setFilterList(filteredMoments);
        Log.d("CategoryFilter", "Filtered " + filteredMoments.size() + " moments for category: " + category);
    }

    /**
     * Update category counts based on current moments and show only categories with images
     */
    private void updateCategoryCounts() {
        if (allMoments == null || categoryFilterAdapter == null) {
            return;
        }

        // Count moments for each category
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (MomentEntity moment : allMoments) {
            String category = extractCategoryFromMoment(moment);
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        // Create new categories list with only categories that have images
        List<CategoryFilterAdapter.CategoryItem> availableCategories = new ArrayList<>();
        
        // Always add "Tất cả" first if there are any moments
        if (!allMoments.isEmpty()) {
            availableCategories.add(new CategoryFilterAdapter.CategoryItem("Tất cả", "📸", allMoments.size()));
        }

        // Get category mapping for icons
        Map<String, String> categoryIcons = getCategoryIconMapping();

        // Add other categories that have images (count > 0)
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            String categoryName = entry.getKey();
            int count = entry.getValue();
            
            // Skip "Tất cả" as we already added it, and only add categories with images
            if (!"Tất cả".equals(categoryName) && count > 0) {
                String icon = categoryIcons.getOrDefault(categoryName, "📋");
                availableCategories.add(new CategoryFilterAdapter.CategoryItem(categoryName, icon, count));
            }
        }

        // Update the adapter with only available categories
        categoryFilterAdapter.updateCategories(availableCategories);
        
        Log.d("CategoryFilter", "Updated categories: " + availableCategories.size() + " categories with images");
    }

    /**
     * Get mapping of category names to their emoji icons
     */
    private Map<String, String> getCategoryIconMapping() {
        Map<String, String> iconMapping = new HashMap<>();
        iconMapping.put("Tất cả", "📸");
        iconMapping.put("Nghệ thuật", "🎨");
        iconMapping.put("Động vật", "🐾");
        iconMapping.put("Con người", "👥");
        iconMapping.put("Phong cảnh", "🌄");
        iconMapping.put("Đồ ăn", "🍽️");
        iconMapping.put("Vui nhộn", "😄");
        iconMapping.put("Thời trang", "👗");
        iconMapping.put("Thể thao", "⚽");
        iconMapping.put("Công nghệ", "💻");
        iconMapping.put("Khác", "📋");
        return iconMapping;
    }

    /**
     * Extract category from moment - Using actual category field from API
     */
    private String extractCategoryFromMoment(MomentEntity moment) {
        // Use actual category field from Post API
        if (moment.getCategory() != null && !moment.getCategory().trim().isEmpty()) {
            return moment.getCategory();
        }
        
        // Fallback to keyword matching if no category field
        if (moment.getCaption() != null) {
            String caption = moment.getCaption().toLowerCase();
            
            if (caption.contains("animal") || caption.contains("động vật") || caption.contains("cat") || caption.contains("dog")) {
                return "Động vật";
            } else if (caption.contains("art") || caption.contains("nghệ thuật") || caption.contains("painting")) {
                return "Nghệ thuật";
            } else if (caption.contains("food") || caption.contains("đồ ăn") || caption.contains("meal")) {
                return "Đồ ăn";
            } else if (caption.contains("landscape") || caption.contains("phong cảnh") || caption.contains("nature")) {
                return "Phong cảnh";
            } else if (caption.contains("people") || caption.contains("con người") || caption.contains("person")) {
                return "Con người";
            }
        }
        
        return "Khác";
    }

    /**
     * Find position of moment in the full unfiltered list
     */
    private int findMomentPositionInFullList(MomentEntity targetMoment) {
        for (int i = 0; i < allMoments.size(); i++) {
            MomentEntity moment = allMoments.get(i);
            if (moment.getId() != null && moment.getId().equals(targetMoment.getId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Update filter selection based on category name
     */
    private void updateFilterSelection(String categoryName) {
        if (categoryFilterAdapter != null) {
            categoryFilterAdapter.setSelectedCategory(categoryName);
            Log.d("CategoryFilter", "Updated filter selection to: " + categoryName);
        }
    }

    /**
     * Setup message sending functionality with TextWatcher and click listeners
     */
    private void setupMessageSending() {
        // Initially disable send button
        updateSendButtonState(false);

        // Add TextWatcher to EditText
        txt_send_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable/disable send button based on text content
                boolean hasText = s.toString().trim().length() > 0;
                updateSendButtonState(hasText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Click listener for send button in message input layout
        layout_send_message.setOnClickListener(v -> {
            String message = txt_send_message.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageReaction(message);
            }
        });

        // Click listener for main send button
        if (layout_send != null) {
            layout_send.setOnClickListener(v -> {
                String message = txt_send_message.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessageReaction(message);
                }
            });
        }
    }

    /**
     * Update send button state (enabled/disabled)
     */
    private void updateSendButtonState(boolean enabled) {
        if (layout_send_message != null) {
            layout_send_message.setAlpha(enabled ? 1.0f : 0.5f);
            layout_send_message.setEnabled(enabled);
            layout_send_message.setBackgroundResource(enabled ? 
                R.drawable.background_btn_continue_check : 
                R.drawable.background_btn_continue_un_check);
        }
    }

    /**
     * Get the currently visible moment from RecyclerView
     */
    private MomentEntity getCurrentVisibleMoment() {
        if (layoutManager == null || viewMomentAdapter == null) {
            return null;
        }

        View centerView = snapHelper.findSnapView(layoutManager);
        if (centerView != null) {
            int position = layoutManager.getPosition(centerView);
            if (position >= 0 && position < viewMomentAdapter.getItemCount()) {
                return viewMomentAdapter.getMomentAtPosition(position);
            }
        }
        return null;
    }

    /**
     * Send message reaction to the current moment
     */
    private void sendMessageReaction(String message) {
        // Check network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(requireContext(), "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user authentication
        if (loginResponse == null || loginResponse.getIdToken() == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current visible moment
        MomentEntity currentMoment = getCurrentVisibleMoment();
        if (currentMoment == null) {
            Toast.makeText(requireContext(), "Không thể xác định khoảnh khắc hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showSendingState();

        // Create comment request
        CommentRequest commentRequest = new CommentRequest(message);
        String token = "Bearer " + loginResponse.getIdToken();

        // Send comment via API
        Call<CommentResponse> call = postApiService.addComment(token, currentMoment.getId(), commentRequest);
        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (!isAdded()) return; // Check if fragment is still attached

                if (response.isSuccessful() && response.body() != null) {
                    // Success - handle comment success
                    handleCommentSuccess(response.body(), message, currentMoment);
                    
                    showSuccessState();
                    txt_send_message.setText(""); // Clear message input
                    Toast.makeText(requireContext(), "Đã gửi tin nhắn", Toast.LENGTH_SHORT).show();
                    
                    // Reset UI after delay
                    new android.os.Handler().postDelayed(() -> {
                        if (isAdded()) {
                            resetSendButtonState();
                        }
                    }, 2000);
                } else {
                    // Error
                    showErrorState();
                    String errorMsg = "Không thể gửi tin nhắn";
                    if (response.code() == 401) {
                        errorMsg = "Phiên đăng nhập hết hạn";
                    } else if (response.code() == 404) {
                        errorMsg = "Khoảnh khắc không tồn tại";
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                if (!isAdded()) return;
                
                showErrorState();
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ViewMomentFragment", "Error sending message", t);
            }
        });
    }

    /**
     * Show loading state while sending message
     */
    private void showSendingState() {
        if (progress_bar != null && img_send != null && lottie_check != null) {
            progress_bar.setVisibility(View.VISIBLE);
            img_send.setVisibility(View.GONE);
            lottie_check.setVisibility(View.GONE);
        }
        // Disable input while sending
        txt_send_message.setEnabled(false);
        layout_send_message.setEnabled(false);
    }

    /**
     * Show success state after message sent
     */
    private void showSuccessState() {
        if (progress_bar != null && img_send != null && lottie_check != null) {
            progress_bar.setVisibility(View.GONE);
            img_send.setVisibility(View.GONE);
            lottie_check.setVisibility(View.VISIBLE);
            lottie_check.playAnimation();
        }
        // Re-enable input
        txt_send_message.setEnabled(true);
        layout_send_message.setEnabled(true);
    }

    /**
     * Show error state if message sending failed
     */
    private void showErrorState() {
        resetSendButtonState();
        // Re-enable input
        txt_send_message.setEnabled(true);
        layout_send_message.setEnabled(true);
    }

    /**
     * Reset send button to initial state
     */
    private void resetSendButtonState() {
        if (progress_bar != null && img_send != null && lottie_check != null) {
            progress_bar.setVisibility(View.GONE);
            img_send.setVisibility(View.VISIBLE);
            lottie_check.setVisibility(View.GONE);
        }
        updateSendButtonState(txt_send_message.getText().toString().trim().length() > 0);
    }

    /**
     * Check network availability
     */
    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = 
            (android.net.ConnectivityManager) requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Handle successful comment submission - create moment comment message in chat thread
     * This preserves existing functionality while adding message thread integration
     */
    private void handleCommentSuccess(CommentResponse response, String commentText, MomentEntity currentMoment) {
        Log.d("ViewMomentFragment", "🚀 DEBUG_COMMENT_SUCCESS: Starting moment comment success handling");
        Log.d("ViewMomentFragment", "📝 Comment text: '" + commentText + "'");
        Log.d("ViewMomentFragment", "🔍 Response details: " + (response != null ? "SUCCESS" : "NULL"));
        Log.d("ViewMomentFragment", "📋 Current moment: " + (currentMoment != null ? currentMoment.getId() : "NULL"));
        
        // Validation checks
        if (currentMoment == null) {
            Log.e("ViewMomentFragment", "❌ DEBUG_COMMENT_SUCCESS_FAILED: Current moment is null");
            return;
        }
        
        if (commentText == null || commentText.trim().isEmpty()) {
            Log.e("ViewMomentFragment", "❌ DEBUG_COMMENT_SUCCESS_FAILED: Comment text is null or empty");
            return;
        }
        
        if (response == null) {
            Log.w("ViewMomentFragment", "⚠️ DEBUG_COMMENT_SUCCESS_WARNING: CommentResponse is null, but proceeding with message creation");
        }
        
        // Don't block the UI - run asynchronously
        new Thread(() -> {
            try {
                Log.d("ViewMomentFragment", "🔄 DEBUG_ASYNC_START: Starting async message creation thread");
                
                // Check if MessageThreadManager is available
                if (messageThreadManager == null) {
                    Log.e("ViewMomentFragment", "❌ DEBUG_ASYNC_FAILED: MessageThreadManager is null");
                    return;
                }
                
                // Log message thread manager state
                messageThreadManager.logDebugInfo();
                
                // Check user authentication
                if (!messageThreadManager.isUserAuthenticated()) {
                    Log.e("ViewMomentFragment", "❌ DEBUG_ASYNC_FAILED: User is not authenticated");
                    return;
                }
                
                Log.d("ViewMomentFragment", "📋 Moment details for message creation:");
                Log.d("ViewMomentFragment", "   - ID: " + currentMoment.getId());
                Log.d("ViewMomentFragment", "   - User: " + currentMoment.getUser());
                Log.d("ViewMomentFragment", "   - Image URL: " + currentMoment.getImageUrl());
                Log.d("ViewMomentFragment", "   - Caption: " + currentMoment.getCaption());
                
                // Use async user ID resolution for better reliability
                messageThreadManager.extractMomentOwnerIdAsync(currentMoment, momentOwnerId -> {
                    if (momentOwnerId == null || momentOwnerId.trim().isEmpty()) {
                        Log.e("ViewMomentFragment", "❌ DEBUG_USER_MAPPING_FAILED: Could not extract moment owner ID");
                        Log.e("ViewMomentFragment", "   - Moment user field: '" + currentMoment.getUser() + "'");
                        Log.e("ViewMomentFragment", "   - This indicates username to user ID mapping failed");
                        Log.e("ViewMomentFragment", "   - Check UserMappingHelper cache and friends list sync");
                        return;
                    }
                    
                    Log.d("ViewMomentFragment", "✅ DEBUG_USER_MAPPING_SUCCESS: Resolved moment owner ID: " + momentOwnerId);
                    Log.d("ViewMomentFragment", "   - Username: '" + currentMoment.getUser() + "' -> User ID: '" + momentOwnerId + "'");
                    
                    // Validate moment data before creating message
                    String momentId = currentMoment.getId();
                    String momentImageUrl = currentMoment.getImageUrl();
                    
                    if (momentId == null || momentId.trim().isEmpty()) {
                        Log.e("ViewMomentFragment", "❌ DEBUG_VALIDATION_FAILED: Moment ID is null or empty");
                        return;
                    }
                    
                    if (momentImageUrl == null || momentImageUrl.trim().isEmpty()) {
                        Log.w("ViewMomentFragment", "⚠️ DEBUG_VALIDATION_WARNING: Moment image URL is null or empty");
                        momentImageUrl = ""; // Set empty string as fallback
                    }
                    
                    Log.d("ViewMomentFragment", "🚀 DEBUG_MESSAGE_CREATION: Starting moment comment message creation");
                    Log.d("ViewMomentFragment", "📋 Final message data:");
                    Log.d("ViewMomentFragment", "   - Comment text: '" + commentText + "'");
                    Log.d("ViewMomentFragment", "   - Moment ID: '" + momentId + "'");
                    Log.d("ViewMomentFragment", "   - Moment image URL: '" + momentImageUrl + "'");
                    Log.d("ViewMomentFragment", "   - Moment owner ID: '" + momentOwnerId + "'");
                    Log.d("ViewMomentFragment", "   - Current user ID: '" + messageThreadManager.getCurrentUserId() + "'");
                    
                    // Create moment comment message in chat thread
                    messageThreadManager.createMomentCommentMessage(
                        commentText,
                        momentId,
                        momentImageUrl,
                        momentOwnerId,
                        new MessageThreadManager.MomentCommentCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("ViewMomentFragment", "✅ DEBUG_MESSAGE_CREATION_SUCCESS: Moment comment message created successfully");
                                Log.d("ViewMomentFragment", "🎯 Message should now appear in ChatActivity between users");
                                Log.d("ViewMomentFragment", "📱 Users can open ChatActivity to see the moment comment message");
                                
                                // Optional: Show a subtle notification to user (if required)
                                if (isAdded() && getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Log.d("ViewMomentFragment", "💬 Comment sent and message created");
                                        // Could show a toast or subtle indicator here if needed
                                    });
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("ViewMomentFragment", "❌ DEBUG_MESSAGE_CREATION_FAILED: Failed to create moment comment message");
                                Log.e("ViewMomentFragment", "   - Error: " + error);
                                Log.e("ViewMomentFragment", "   - Comment was still posted successfully to the moment");
                                Log.e("ViewMomentFragment", "   - Only the chat message creation failed");
                                
                                // Don't show error to user - comment posting was still successful
                                // This is a background feature that shouldn't interrupt the main flow
                                
                                // Log additional debugging info
                                Log.e("ViewMomentFragment", "🔍 DEBUG_ERROR_ANALYSIS:");
                                Log.e("ViewMomentFragment", "   - Check Firebase database connection");
                                Log.e("ViewMomentFragment", "   - Verify chat room ID generation");
                                Log.e("ViewMomentFragment", "   - Ensure user authentication is valid");
                                Log.e("ViewMomentFragment", "   - Check if ChatMessage model is correct");
                            }
                        }
                    );
                });
                
            } catch (Exception e) {
                Log.e("ViewMomentFragment", "❌ DEBUG_ASYNC_EXCEPTION: Exception in handleCommentSuccess", e);
                Log.e("ViewMomentFragment", "   - Exception type: " + e.getClass().getSimpleName());
                Log.e("ViewMomentFragment", "   - Exception message: " + e.getMessage());
                
                // Don't crash or affect the comment success flow
                // This is additional functionality that shouldn't break the main feature
            }
        }).start();
    }
}

