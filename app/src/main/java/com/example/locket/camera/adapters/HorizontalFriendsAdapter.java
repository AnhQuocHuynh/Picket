package com.example.locket.camera.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.common.models.friendship.FriendsListResponse;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class HorizontalFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ALL = 0;
    private static final int TYPE_FRIEND = 1;
    private static final int TYPE_ADD_MORE = 2;
    private static final int MAX_VISIBLE_FRIENDS = 7; // Reduced by 1 to account for "All" item
    private static final int DOUBLE_TAP_DELAY = 300; // milliseconds

    private final Context context;
    private final List<FriendsListResponse.FriendData> allFriends;
    private final List<FriendsListResponse.FriendData> selectedFriends;
    private OnFriendSelectionListener listener;
    private boolean isAllSelected = true; // Default to "All" selected

    public interface OnFriendSelectionListener {
        void onFriendSelected(FriendsListResponse.FriendData friend);
        void onFriendDeselected(FriendsListResponse.FriendData friend);
        void onSelectionCleared(FriendsListResponse.FriendData friend);
        void onAllSelected();
        void onAllDeselected();
        void onShowRecipientPicker();
    }

    public HorizontalFriendsAdapter(Context context, 
                                   List<FriendsListResponse.FriendData> allFriends,
                                   List<FriendsListResponse.FriendData> selectedFriends) {
        this.context = context;
        this.allFriends = allFriends;
        this.selectedFriends = selectedFriends;
        
        // Initialize with "All" selected by default
        selectedFriends.clear();
        isAllSelected = true;
    }

    public void setOnFriendSelectionListener(OnFriendSelectionListener listener) {
        this.listener = listener;
    }

    public boolean isAllSelected() {
        return isAllSelected;
    }

    public void setAllSelected(boolean allSelected) {
        this.isAllSelected = allSelected;
        if (allSelected) {
            selectedFriends.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // First position is always "All" item
        if (position == 0) {
            return TYPE_ALL;
        }
        // Show "Add more" button if we have more friends than can be displayed
        if (position == getItemCount() - 1 && allFriends.size() > MAX_VISIBLE_FRIENDS) {
            return TYPE_ADD_MORE;
        }
        return TYPE_FRIEND;
    }

    @Override
    public int getItemCount() {
        if (allFriends.isEmpty()) {
            return 2; // "All" item + "Add more" button
        }
        // Show "All" + up to MAX_VISIBLE_FRIENDS + 1 (for add more button)
        return Math.min(allFriends.size() + 2, MAX_VISIBLE_FRIENDS + 2);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ALL) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_friend_all, parent, false);
            return new AllViewHolder(view);
        } else if (viewType == TYPE_ADD_MORE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_friend_add_more, parent, false);
            return new AddMoreViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_friend_horizontal, parent, false);
            return new FriendViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AllViewHolder) {
            ((AllViewHolder) holder).bind();
        } else if (holder instanceof AddMoreViewHolder) {
            ((AddMoreViewHolder) holder).bind();
        } else if (holder instanceof FriendViewHolder && position > 0 && position - 1 < allFriends.size()) {
            // Adjust position for friends (position 0 is "All", so friend index = position - 1)
            ((FriendViewHolder) holder).bind(allFriends.get(position - 1));
        }
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView img_avatar;
        private final TextView txt_name;
        private final View view_selection_border;
        private final ImageView img_selected_badge;
        
        private boolean awaitingSecondTap = false;
        private Handler tapHandler = new Handler();

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            img_avatar = itemView.findViewById(R.id.img_avatar);
            txt_name = itemView.findViewById(R.id.txt_name);
            view_selection_border = itemView.findViewById(R.id.view_selection_border);
            img_selected_badge = itemView.findViewById(R.id.img_selected_badge);
        }

        public void bind(FriendsListResponse.FriendData friend) {
            // Set name (max 10 characters)
            String displayName = friend.getDisplayName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = friend.getUsername() != null ? friend.getUsername() : "User";
            }
            if (displayName.length() > 10) {
                displayName = displayName.substring(0, 8) + "...";
            }
            txt_name.setText(displayName);

            // Load avatar
            if (friend.getProfilePicture() != null && !friend.getProfilePicture().isEmpty()) {
                Glide.with(context)
                        .load(friend.getProfilePicture())
                        .placeholder(R.drawable.ic_widget_empty_icon)
                        .error(R.drawable.ic_widget_empty_icon)
                        .into(img_avatar);
            } else {
                img_avatar.setImageResource(R.drawable.ic_widget_empty_icon);
            }

            // Update selection state
            boolean isSelected = selectedFriends.contains(friend);
            updateSelectionUI(isSelected, false);

            // Set click listener with double-tap detection
            itemView.setOnClickListener(v -> {
                if (awaitingSecondTap) {
                    // Double tap detected - clear selection
                    awaitingSecondTap = false;
                    tapHandler.removeCallbacksAndMessages(null);
                    handleDoubleTap(friend);
                } else {
                    // First tap - start waiting for second tap
                    awaitingSecondTap = true;
                    tapHandler.postDelayed(() -> {
                        awaitingSecondTap = false;
                        handleSingleTap(friend);
                    }, DOUBLE_TAP_DELAY);
                }
            });
        }

        private void handleSingleTap(FriendsListResponse.FriendData friend) {
            boolean isSelected = selectedFriends.contains(friend);
            
            if (isSelected) {
                // Deselect friend
                selectedFriends.remove(friend);
                updateSelectionUI(false, true);
                if (listener != null) {
                    listener.onFriendDeselected(friend);
                }
            } else {
                // If "All" is selected, deselect it first
                if (isAllSelected) {
                    isAllSelected = false;
                    if (listener != null) {
                        listener.onAllDeselected();
                    }
                    // Refresh "All" item
                    notifyItemChanged(0);
                }
                
                // Select friend
                selectedFriends.add(friend);
                updateSelectionUI(true, true);
                if (listener != null) {
                    listener.onFriendSelected(friend);
                }
            }
        }

        private void handleDoubleTap(FriendsListResponse.FriendData friend) {
            // Double tap - reset to default state (unselected)
            if (selectedFriends.contains(friend)) {
                selectedFriends.remove(friend);
                updateSelectionUI(false, true);
                if (listener != null) {
                    listener.onSelectionCleared(friend);
                }
            }
        }

        private void updateSelectionUI(boolean isSelected, boolean animate) {
            // If "All" is selected, show all friends as dimmed but not selected
            if (isAllSelected) {
                view_selection_border.setVisibility(View.GONE);
                img_selected_badge.setVisibility(View.GONE);
                itemView.setAlpha(0.7f);
            } else if (isSelected) {
                view_selection_border.setVisibility(View.VISIBLE);
                img_selected_badge.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
            } else {
                view_selection_border.setVisibility(View.GONE);
                img_selected_badge.setVisibility(View.GONE);
                itemView.setAlpha(0.5f);
            }

            if (animate) {
                // Add scale animation for selection feedback
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", 0.9f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", 0.9f, 1.0f);
                scaleX.setDuration(150);
                scaleY.setDuration(150);
                scaleX.start();
                scaleY.start();

                // Add badge animation
                if (isSelected) {
                    ObjectAnimator badgeScale = ObjectAnimator.ofFloat(img_selected_badge, "scaleX", 0.0f, 1.2f, 1.0f);
                    ObjectAnimator badgeScaleY = ObjectAnimator.ofFloat(img_selected_badge, "scaleY", 0.0f, 1.2f, 1.0f);
                    badgeScale.setDuration(200);
                    badgeScaleY.setDuration(200);
                    badgeScale.start();
                    badgeScaleY.start();
                }
            }
        }
    }

    class AllViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView img_all_avatar;
        private final TextView txt_all_label;
        private final ImageView img_all_selected_badge;

        public AllViewHolder(@NonNull View itemView) {
            super(itemView);
            img_all_avatar = itemView.findViewById(R.id.img_all_avatar);
            txt_all_label = itemView.findViewById(R.id.txt_all_label);
            img_all_selected_badge = itemView.findViewById(R.id.img_all_selected_badge);
        }

        public void bind() {
            // Update selection state
            updateAllSelectionUI(isAllSelected, false);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (isAllSelected) {
                    // Deselect "All"
                    isAllSelected = false;
                    updateAllSelectionUI(false, true);
                    if (listener != null) {
                        listener.onAllDeselected();
                    }
                } else {
                    // Select "All" and deselect all individual friends
                    isAllSelected = true;
                    selectedFriends.clear();
                    updateAllSelectionUI(true, true);
                    
                    // Refresh all friend items to show dimmed state
                    notifyItemRangeChanged(1, allFriends.size());
                    
                    if (listener != null) {
                        listener.onAllSelected();
                    }
                }
            });
        }

        private void updateAllSelectionUI(boolean isSelected, boolean animate) {
            if (isSelected) {
                img_all_selected_badge.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
                img_all_avatar.setBackgroundResource(R.drawable.friend_avatar_border_selected);
            } else {
                img_all_selected_badge.setVisibility(View.GONE);
                itemView.setAlpha(0.6f);
                img_all_avatar.setBackgroundResource(R.drawable.bg_widget_empty_circle_outline);
            }

            if (animate) {
                // Add scale animation for selection feedback
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", 0.9f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", 0.9f, 1.0f);
                scaleX.setDuration(150);
                scaleY.setDuration(150);
                scaleX.start();
                scaleY.start();

                // Add badge animation
                if (isSelected) {
                    ObjectAnimator badgeScale = ObjectAnimator.ofFloat(img_all_selected_badge, "scaleX", 0.0f, 1.2f, 1.0f);
                    ObjectAnimator badgeScaleY = ObjectAnimator.ofFloat(img_all_selected_badge, "scaleY", 0.0f, 1.2f, 1.0f);
                    badgeScale.setDuration(200);
                    badgeScaleY.setDuration(200);
                    badgeScale.start();
                    badgeScaleY.start();
                }
            }
        }
    }

    class AddMoreViewHolder extends RecyclerView.ViewHolder {
        public AddMoreViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShowRecipientPicker();
                }
            });
        }
    }
} 