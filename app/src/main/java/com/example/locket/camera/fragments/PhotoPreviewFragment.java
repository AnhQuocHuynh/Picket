package com.example.locket.camera.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.locket.R;
import com.example.locket.camera.adapters.HorizontalFriendsAdapter;
import com.example.locket.camera.bottomsheets.RecipientPickerBottomSheet;
import com.example.locket.common.models.auth.LoginResponse;
import com.example.locket.common.models.friendship.FriendsListResponse;
import com.example.locket.common.models.post.PostResponse;
import com.example.locket.common.network.ImageUploadService;
import com.example.locket.common.repository.FriendshipRepository;
import com.example.locket.common.repository.PostRepository;
import com.example.locket.common.utils.SharedPreferencesUser;
import com.example.locket.common.utils.SuccessNotificationDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.provider.MediaStore;
import android.os.Environment;

public class PhotoPreviewFragment extends Fragment {
    private static final String ARG_IMAGE_BITMAP = "image_bitmap";
    private static final String ARG_IMAGE_BYTES = "image_bytes";
    private static final String ARG_VIDEO_PATH = "video_path";

    // UI Components
    private ImageView img_preview;
    private VideoView videoViewPreview;
    private EditText edt_add_message;
    private RecyclerView rv_friends_horizontal;
    private ImageView img_cancel;
    private ImageView img_retake;
    private LinearLayout layout_send;
    private ImageView img_send;
    private LottieAnimationView lottie_check;
    private ProgressBar progress_bar;
    private TextView txt_recipient_count;
    private ImageView img_save;

    // Data
    private Bitmap imageBitmap;
    private byte[] imageBytes;
    private String message = "";
    private List<FriendsListResponse.FriendData> allFriends = new ArrayList<>();
    private List<FriendsListResponse.FriendData> selectedFriends = new ArrayList<>();
    private String videoPath;

    // Components
    private HorizontalFriendsAdapter friendsAdapter;
    private FriendshipRepository friendshipRepository;
    private PostRepository postRepository;
    private ImageUploadService imageUploadService;
    private LoginResponse loginResponse;
    private SuccessNotificationDialog successDialog;

    // Interface for communication with parent
    public interface PhotoPreviewListener {
        void onCancel();
        void onRetake();
        void onSendComplete();
    }

    private PhotoPreviewListener listener;

    public static PhotoPreviewFragment newInstance(Bitmap bitmap, byte[] bytes) {
        PhotoPreviewFragment fragment = new PhotoPreviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_BITMAP, bitmap);
        args.putByteArray(ARG_IMAGE_BYTES, bytes);
        fragment.setArguments(args);
        return fragment;
    }

    public static PhotoPreviewFragment newInstanceForVideo(String videoPath) {
        PhotoPreviewFragment fragment = new PhotoPreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_PATH, videoPath);
        fragment.setArguments(args);
        return fragment;
    }

    public void setPhotoPreviewListener(PhotoPreviewListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageBitmap = getArguments().getParcelable(ARG_IMAGE_BITMAP);
            imageBytes = getArguments().getByteArray(ARG_IMAGE_BYTES);
            videoPath = getArguments().getString(ARG_VIDEO_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
        setupRecyclerView();
        setupClickListeners();
        setupMessageWatcher();
        loadFriendsList();

        // Display the captured image or video
        if (videoPath != null) {
            img_preview.setVisibility(View.GONE);
            videoViewPreview.setVisibility(View.VISIBLE);
            videoViewPreview.setVideoURI(Uri.parse(videoPath));
            videoViewPreview.setOnPreparedListener(mp -> {
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();
                if (videoViewPreview instanceof com.example.locket.camera.utils.AutoFitVideoView) {
                    ((com.example.locket.camera.utils.AutoFitVideoView) videoViewPreview).setVideoSize(videoWidth, videoHeight);
                }
                mp.setLooping(true);
                videoViewPreview.start();
            });
            img_save.setVisibility(View.VISIBLE);
        } else if (imageBitmap != null) {
            img_preview.setVisibility(View.VISIBLE);
            videoViewPreview.setVisibility(View.GONE);
            img_preview.setImageBitmap(imageBitmap);
            img_save.setVisibility(View.GONE);
        }
    }

    private void initViews(View view) {
        img_preview = view.findViewById(R.id.img_preview);
        videoViewPreview = view.findViewById(R.id.videoViewPreview);
        edt_add_message = view.findViewById(R.id.edt_add_message);
        rv_friends_horizontal = view.findViewById(R.id.rv_friends_horizontal);
        img_cancel = view.findViewById(R.id.img_cancel);
        img_retake = view.findViewById(R.id.img_retake);
        layout_send = view.findViewById(R.id.layout_send);
        img_send = view.findViewById(R.id.img_send);
        lottie_check = view.findViewById(R.id.lottie_check);
        progress_bar = view.findViewById(R.id.progress_bar);
        txt_recipient_count = view.findViewById(R.id.txt_recipient_count);
        img_save = view.findViewById(R.id.img_save);
    }

    private void initData() {
        loginResponse = SharedPreferencesUser.getLoginResponse(requireContext());
        friendshipRepository = new FriendshipRepository(requireContext());
        postRepository = new PostRepository(requireContext());
        imageUploadService = new ImageUploadService(requireContext());
        successDialog = new SuccessNotificationDialog(requireContext());
    }

    private void setupRecyclerView() {
        friendsAdapter = new HorizontalFriendsAdapter(requireContext(), allFriends, selectedFriends);
        friendsAdapter.setOnFriendSelectionListener(new HorizontalFriendsAdapter.OnFriendSelectionListener() {
            @Override
            public void onFriendSelected(FriendsListResponse.FriendData friend) {
                if (!selectedFriends.contains(friend)) {
                    selectedFriends.add(friend);
                }
                updateRecipientCount();
                updateSendButtonState();
                Log.d("PhotoPreview", "✅ Friend selected: " + friend.getDisplayName());
            }

            @Override
            public void onFriendDeselected(FriendsListResponse.FriendData friend) {
                selectedFriends.remove(friend);
                updateRecipientCount();
                updateSendButtonState();
                Log.d("PhotoPreview", "❌ Friend deselected: " + friend.getDisplayName());
            }

            @Override
            public void onSelectionCleared(FriendsListResponse.FriendData friend) {
                selectedFriends.remove(friend);
                updateRecipientCount();
                updateSendButtonState();
                Log.d("PhotoPreview", "🔄 Friend selection cleared: " + friend.getDisplayName());
            }

            @Override
            public void onAllSelected() {
                selectedFriends.clear();
                selectedFriends.addAll(allFriends);
                updateRecipientCount();
                updateSendButtonState();
                Log.d("PhotoPreview", "🌟 All friends selected");
            }

            @Override
            public void onAllDeselected() {
                selectedFriends.clear();
                updateRecipientCount();
                updateSendButtonState();
                Log.d("PhotoPreview", "❌ All friends deselected");
            }

            @Override
            public void onShowRecipientPicker() {
                showRecipientPickerDialog();
            }
        });

        rv_friends_horizontal.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_friends_horizontal.setAdapter(friendsAdapter);
    }

    private void setupClickListeners() {
        img_cancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
        });

        img_retake.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRetake();
            }
        });

        layout_send.setOnClickListener(v -> sendImage());

    }

    private void setupMessageWatcher() {
        edt_add_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                message = s.toString().trim();
            }
        });
    }

    private void loadFriendsList() {
        friendshipRepository.getFriendsList(new FriendshipRepository.FriendsListCallback() {
            @Override
            public void onSuccess(FriendsListResponse friendsListResponse) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (friendsListResponse != null && friendsListResponse.getData() != null) {
                            allFriends.clear();

                            // Filter out current user from friends list
                            List<FriendsListResponse.FriendData> filteredFriends = filterCurrentUserFromFriends(friendsListResponse.getData());
                            allFriends.addAll(filteredFriends);

                            friendsAdapter.notifyDataSetChanged();
                            updateRecipientCount();
                            Log.d("PhotoPreview", "✅ Friends list loaded: " + allFriends.size() + " friends (current user filtered)");
                        }
                    });
                }
            }

            @Override
            public void onError(String message, int code) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e("PhotoPreview", "❌ Failed to load friends: " + message);
                        Toast.makeText(requireContext(), "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onLoading(boolean isLoading) {
                // Handle loading state if needed
            }
        });
    }

    private void showRecipientPickerDialog() {
        RecipientPickerBottomSheet bottomSheet = RecipientPickerBottomSheet.newInstance(
                new ArrayList<>(allFriends),
                new ArrayList<>(selectedFriends)
        );

        bottomSheet.setOnRecipientsSelectedListener(new RecipientPickerBottomSheet.OnRecipientsSelectedListener() {
            @Override
            public void onRecipientsSelected(List<FriendsListResponse.FriendData> recipients) {
                selectedFriends.clear();
                selectedFriends.addAll(recipients);

                // If all friends are selected, switch to "All" mode
                if (recipients.size() == allFriends.size()) {
                    friendsAdapter.setAllSelected(true);
                } else {
                    friendsAdapter.setAllSelected(false);
                    friendsAdapter.notifyDataSetChanged();
                }

                updateRecipientCount();
                updateSendButtonState();
                Log.d("PhotoPreview", "✅ Recipients updated from picker: " + recipients.size() + " selected");
            }
        });

        bottomSheet.show(getParentFragmentManager(), "recipient_picker");
    }

    private void updateRecipientCount() {
        int totalFriends = allFriends.size();
        int selectedCount = selectedFriends.size();

        // Update the count badge
        if (friendsAdapter != null && friendsAdapter.isAllSelected()) {
            txt_recipient_count.setText("(Tất cả " + totalFriends + ")");
        } else {
            txt_recipient_count.setText("(" + selectedCount + "/" + totalFriends + ")");
        }

        Log.d("PhotoPreview", "📊 Recipient count updated: " + selectedCount + "/" + totalFriends);
    }

    private void updateSendButtonState() {
        // Enable send button if "All" is selected or at least one friend is selected
        boolean canSend = (friendsAdapter != null && friendsAdapter.isAllSelected()) || !selectedFriends.isEmpty();
        layout_send.setEnabled(canSend);
        layout_send.setAlpha(canSend ? 1.0f : 0.5f);
    }

    /**
     * Filter out current user from friends list
     */
    private List<FriendsListResponse.FriendData> filterCurrentUserFromFriends(List<FriendsListResponse.FriendData> friendsList) {
        List<FriendsListResponse.FriendData> filteredFriends = new ArrayList<>();

        // Get current user info
        String currentUserId = null;
        String currentUsername = null;

        if (loginResponse != null && loginResponse.getUser() != null) {
            currentUserId = loginResponse.getUser().getId();
            currentUsername = loginResponse.getUser().getUsername();
        }

        // Filter out current user
        for (FriendsListResponse.FriendData friend : friendsList) {
            boolean isCurrentUser = false;

            // Check by ID
            if (currentUserId != null && currentUserId.equals(friend.getId())) {
                isCurrentUser = true;
            }

            // Check by username as backup
            if (!isCurrentUser && currentUsername != null && currentUsername.equals(friend.getUsername())) {
                isCurrentUser = true;
            }

            // Add to filtered list if not current user
            if (!isCurrentUser) {
                filteredFriends.add(friend);
            } else {
                Log.d("PhotoPreview", "🚫 Filtered out current user: " + friend.getDisplayName() + " (" + friend.getUsername() + ")");
            }
        }

        Log.d("PhotoPreview", "📊 Friends filter result: " + friendsList.size() + " -> " + filteredFriends.size());
        return filteredFriends;
    }

    private void sendImage() {
        if (videoPath != null) {
            // Upload video
            setLoadingState(true);
            Log.d("PhotoPreview", "🚀 Starting video upload...");
            try {
                File videoFile = new File(videoPath);
                byte[] videoBytes = new byte[(int) videoFile.length()];
                FileInputStream fis = new FileInputStream(videoFile);
                fis.read(videoBytes);
                fis.close();
                // Nếu imageUploadService có uploadVideo thì dùng, nếu không thì dùng uploadImage
                imageUploadService.uploadImage(videoBytes, new ImageUploadService.UploadCallback() {
                    @Override
                    public void onUploadComplete(String videoUrl, boolean success) {
                        if (success && videoUrl != null) {
                            Log.d("PhotoPreview", "✅ Video uploaded successfully: " + videoUrl);
                            createPost(videoUrl, message);
                        } else {
                            Log.e("PhotoPreview", "❌ Video upload failed");
                            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                setLoadingState(false);
                                Toast.makeText(requireContext(), "Upload video thất bại", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                    @Override
                    public void onUploadProgress(int progress) {
                        Log.d("PhotoPreview", "📤 Upload progress: " + progress + "%");
                    }
                    @Override
                    public void onError(String message, int code) {
                        Log.e("PhotoPreview", "❌ Upload error: " + message + " (Code: " + code + ")");
                        if (getActivity() != null) getActivity().runOnUiThread(() -> {
                            setLoadingState(false);
                            Toast.makeText(requireContext(), "Lỗi upload: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                setLoadingState(false);
                Toast.makeText(requireContext(), "Không thể đọc file video", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (imageBytes == null || imageBytes.length == 0) {
            Toast.makeText(requireContext(), "Không có dữ liệu ảnh để gửi", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoadingState(true);
        Log.d("PhotoPreview", "🚀 Starting image upload...");
        imageUploadService.uploadImage(imageBytes, new ImageUploadService.UploadCallback() {
            @Override
            public void onUploadComplete(String imageUrl, boolean success) {
                if (success && imageUrl != null) {
                    Log.d("PhotoPreview", "✅ Image uploaded successfully: " + imageUrl);
                    createPost(imageUrl, message);
                } else {
                    Log.e("PhotoPreview", "❌ Image upload failed");
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        setLoadingState(false);
                        Toast.makeText(requireContext(), "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                    });
                }
            }
            @Override
            public void onUploadProgress(int progress) {
                Log.d("PhotoPreview", "📤 Upload progress: " + progress + "%");
            }
            @Override
            public void onError(String message, int code) {
                Log.e("PhotoPreview", "❌ Upload error: " + message + " (Code: " + code + ")");
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(requireContext(), "Lỗi upload: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void createPost(String imageUrl, String caption) {
        Log.d("PhotoPreview", "📝 Creating post with imageUrl: " + imageUrl);

        if (friendsAdapter.isAllSelected()) {
            Log.d("PhotoPreview", "📤 Sending to all " + allFriends.size() + " friends");
            // TODO: In future, modify PostRepository to accept recipient list for sending to all
            // For now, sending to all friends when "All" is selected
        } else if (!selectedFriends.isEmpty()) {
            Log.d("PhotoPreview", "📤 Sending to " + selectedFriends.size() + " selected friends");
            // TODO: In future, modify PostRepository to accept recipient list for selective sending
            // For now, sending to all friends regardless of selection
        } else {
            Log.d("PhotoPreview", "⚠️ No friends selected!");
            return;
        }

        postRepository.createPost(imageUrl, caption, new PostRepository.PostCallback() {
            @Override
            public void onSuccess(PostResponse postResponse) {
                Log.d("PhotoPreview", "✅ Post created successfully!");
                getActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    showSuccessState();
                });
            }

            @Override
            public void onError(String message, int code) {
                Log.e("PhotoPreview", "❌ Create post error: " + message + " (Code: " + code + ")");
                getActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    Toast.makeText(requireContext(), "Tạo bài viết thất bại: " + message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLoading(boolean isLoading) {
                Log.d("PhotoPreview", "⏳ Post creation loading: " + isLoading);
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progress_bar.setVisibility(View.VISIBLE);
            img_cancel.setVisibility(View.GONE);
            img_retake.setVisibility(View.GONE);
            img_send.setVisibility(View.GONE);
            edt_add_message.setEnabled(false);
            rv_friends_horizontal.setEnabled(false);
        } else {
            progress_bar.setVisibility(View.GONE);
            img_cancel.setVisibility(View.VISIBLE);
            img_retake.setVisibility(View.VISIBLE);
            img_send.setVisibility(View.VISIBLE);
            edt_add_message.setEnabled(true);
            rv_friends_horizontal.setEnabled(true);
        }
    }

    private void showSuccessState() {
        // Hide loading state
        progress_bar.setVisibility(View.GONE);

        // Show custom success dialog
        String message;
        if (friendsAdapter != null && friendsAdapter.isAllSelected()) {
            message = "Ảnh đã được gửi đến tất cả " + allFriends.size() + " bạn bè";
        } else if (selectedFriends.size() > 0) {
            message = "Ảnh đã được gửi đến " + selectedFriends.size() + " bạn bè";
        } else {
            message = "Ảnh của bạn đã được chia sẻ với bạn bè";
        }

        successDialog.show("Gửi thành công!", message, new SuccessNotificationDialog.OnDismissListener() {
            @Override
            public void onDismiss() {
                // Notify parent fragment that sending is complete
                if (listener != null) {
                    listener.onSendComplete();
                }
            }
        });
    }


} 