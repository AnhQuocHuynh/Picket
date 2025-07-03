package com.example.locket.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.chat.model.ChatMessage;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final String TAG = "ChatAdapter";

    private static final int VIEW_TYPE_TEXT_SENT = 1;
    private static final int VIEW_TYPE_TEXT_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
    private static final int VIEW_TYPE_MOMENT_COMMENT_SENT = 5;
    private static final int VIEW_TYPE_MOMENT_COMMENT_RECEIVED = 6;

    private List<ChatMessage> chatMessages;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> chatMessages, String currentUserId) {
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
        Log.d(TAG, "🔧 ChatAdapter initialized with " + 
            (chatMessages != null ? chatMessages.size() : 0) + " messages for user: " + currentUserId);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= chatMessages.size()) {
            Log.e(TAG, "❌ Invalid position: " + position + " (size: " + chatMessages.size() + ")");
            return VIEW_TYPE_TEXT_SENT; // Safe fallback
        }
        
        ChatMessage message = chatMessages.get(position);
        if (message == null) {
            Log.e(TAG, "❌ Message at position " + position + " is null");
            return VIEW_TYPE_TEXT_SENT; // Safe fallback
        }
        
        boolean isSentByMe = message.getSenderId() != null && message.getSenderId().equals(currentUserId);
        
        Log.d(TAG, "🔍 Message type determination - Position: " + position + 
            ", Type: " + message.getType() + ", SentByMe: " + isSentByMe);

        switch (message.getType()) {
            case IMAGE:
            case VIDEO:
                return isSentByMe ? VIEW_TYPE_IMAGE_SENT : VIEW_TYPE_IMAGE_RECEIVED;
            case MOMENT_COMMENT:
                Log.d(TAG, "📋 MOMENT_COMMENT message detected - using ViewType: " + 
                    (isSentByMe ? "SENT" : "RECEIVED"));
                return isSentByMe ? VIEW_TYPE_MOMENT_COMMENT_SENT : VIEW_TYPE_MOMENT_COMMENT_RECEIVED;
            case TEXT:
            default:
                return isSentByMe ? VIEW_TYPE_TEXT_SENT : VIEW_TYPE_TEXT_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Log.d(TAG, "🏗️ Creating ViewHolder for type: " + getViewTypeName(viewType));
        
        try {
            switch (viewType) {
                case VIEW_TYPE_TEXT_SENT:
                    return new SentMessageViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false));
                case VIEW_TYPE_TEXT_RECEIVED:
                    return new ReceivedMessageViewHolder(inflater.inflate(R.layout.item_chat_message_received, parent, false));
                case VIEW_TYPE_IMAGE_SENT:
                    return new SentImageViewHolder(inflater.inflate(R.layout.item_chat_image_sent, parent, false));
                case VIEW_TYPE_IMAGE_RECEIVED:
                    return new ReceivedImageViewHolder(inflater.inflate(R.layout.item_chat_image_received, parent, false));
                case VIEW_TYPE_MOMENT_COMMENT_SENT:
                    Log.d(TAG, "🎯 Creating SentMomentCommentViewHolder");
                    return new SentMomentCommentViewHolder(inflater.inflate(R.layout.item_chat_moment_comment_sent, parent, false));
                case VIEW_TYPE_MOMENT_COMMENT_RECEIVED:
                    Log.d(TAG, "🎯 Creating ReceivedMomentCommentViewHolder");
                    return new ReceivedMomentCommentViewHolder(inflater.inflate(R.layout.item_chat_moment_comment_received, parent, false));
                default:
                    Log.w(TAG, "⚠️ Unknown view type: " + viewType + ", falling back to text sent");
                    return new SentMessageViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false));
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating ViewHolder for type " + viewType + ": " + e.getMessage(), e);
            // Fallback to simple text message
            return new SentMessageViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < 0 || position >= chatMessages.size()) {
            Log.e(TAG, "❌ Invalid bind position: " + position);
            return;
        }
        
        ChatMessage message = chatMessages.get(position);
        if (message == null) {
            Log.e(TAG, "❌ Message at position " + position + " is null");
            return;
        }
        
        Log.d(TAG, "📋 Binding message at position " + position + " - Type: " + message.getType());
        
        try {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_TEXT_SENT:
                    ((SentMessageViewHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_TEXT_RECEIVED:
                    ((ReceivedMessageViewHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_SENT:
                    ((SentImageViewHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_RECEIVED:
                    ((ReceivedImageViewHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MOMENT_COMMENT_SENT:
                    Log.d(TAG, "🎯 Binding SentMomentCommentViewHolder");
                    ((SentMomentCommentViewHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MOMENT_COMMENT_RECEIVED:
                    Log.d(TAG, "🎯 Binding ReceivedMomentCommentViewHolder");
                    ((ReceivedMomentCommentViewHolder) holder).bind(message);
                    break;
                default:
                    Log.w(TAG, "⚠️ Unknown ViewHolder type for binding: " + holder.getItemViewType());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error binding ViewHolder at position " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages != null ? chatMessages.size() : 0;
    }
    
    /**
     * Helper method to get view type name for logging
     */
    private String getViewTypeName(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TEXT_SENT: return "TEXT_SENT";
            case VIEW_TYPE_TEXT_RECEIVED: return "TEXT_RECEIVED";
            case VIEW_TYPE_IMAGE_SENT: return "IMAGE_SENT";
            case VIEW_TYPE_IMAGE_RECEIVED: return "IMAGE_RECEIVED";
            case VIEW_TYPE_MOMENT_COMMENT_SENT: return "MOMENT_COMMENT_SENT";
            case VIEW_TYPE_MOMENT_COMMENT_RECEIVED: return "MOMENT_COMMENT_RECEIVED";
            default: return "UNKNOWN(" + viewType + ")";
        }
    }

    // ViewHolder for sent messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        void bind(ChatMessage message) {
            if (messageTextView != null && message != null) {
                messageTextView.setText(message.getText());
            }
        }
    }

    // ViewHolder for received messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        void bind(ChatMessage message) {
            if (messageTextView != null && message != null) {
                messageTextView.setText(message.getText());
            }
        }
    }

    // ViewHolder for sent images
    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageMessageView;

        SentImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMessageView = itemView.findViewById(R.id.imageMessageView);
        }

        void bind(ChatMessage message) {
            if (imageMessageView != null && message != null) {
                Glide.with(itemView.getContext())
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.avatar_placeholder)
                        .into(imageMessageView);
            }
        }
    }

    // ViewHolder for received images
    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageMessageView;

        ReceivedImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMessageView = itemView.findViewById(R.id.imageMessageView);
        }

        void bind(ChatMessage message) {
            if (imageMessageView != null && message != null) {
                Glide.with(itemView.getContext())
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.avatar_placeholder)
                        .into(imageMessageView);
            }
        }
    }

    // ViewHolder for sent moment comments
    static class SentMomentCommentViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView momentImageView;
        private TextView commentTextView;
        private TextView timestampTextView;

        SentMomentCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            momentImageView = itemView.findViewById(R.id.momentImageView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            
            Log.d(TAG, "🏗️ SentMomentCommentViewHolder created - Views found: " +
                "momentImage=" + (momentImageView != null) + 
                ", commentText=" + (commentTextView != null) + 
                ", timestamp=" + (timestampTextView != null));
        }

        void bind(ChatMessage message) {
            Log.d(TAG, "🎯 DEBUG_VIEWHOLDER_BIND: Binding sent moment comment");
            
            if (message == null) {
                Log.e(TAG, "❌ DEBUG_VIEWHOLDER_BIND_FAILED: Message is null");
                return;
            }
            
            Log.d(TAG, "📋 Message data: " +
                "text='" + message.getText() + "', " +
                "momentImageUrl='" + message.getMomentImageUrl() + "', " +
                "timestamp=" + message.getTimestamp());

            // Set comment text
            if (commentTextView != null) {
                String commentText = message.getText();
                if (commentText != null && !commentText.trim().isEmpty()) {
                    commentTextView.setText(commentText);
                    commentTextView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "✅ Comment text set: " + commentText);
                } else {
                    commentTextView.setText("No comment");
                    commentTextView.setVisibility(View.VISIBLE);
                    Log.w(TAG, "⚠️ Comment text is empty, showing placeholder");
                }
            } else {
                Log.e(TAG, "❌ commentTextView is null");
            }

            // Format and set timestamp
            if (timestampTextView != null) {
                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String timeText = timeFormat.format(new Date(message.getTimestamp()));
                    timestampTextView.setText(timeText);
                    timestampTextView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "✅ Timestamp set: " + timeText);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error formatting timestamp: " + e.getMessage(), e);
                    timestampTextView.setText("--:--");
                    timestampTextView.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(TAG, "❌ timestampTextView is null");
            }

            // Load moment image with enhanced error handling
            if (momentImageView != null) {
                String imageUrl = message.getMomentImageUrl();
                Log.d(TAG, "🖼️ Loading moment image: " + imageUrl);
                
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                    
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .apply(options)
                            .into(momentImageView);
                    
                    Log.d(TAG, "✅ Started loading moment image");
                } else {
                    Log.w(TAG, "⚠️ Moment image URL is null or empty, showing placeholder");
                    momentImageView.setImageResource(R.drawable.avatar_placeholder);
                }
            } else {
                Log.e(TAG, "❌ momentImageView is null");
            }
            
            Log.d(TAG, "✅ DEBUG_VIEWHOLDER_BIND_SUCCESS: Sent moment comment binding completed");
        }
    }

    // ViewHolder for received moment comments
    static class ReceivedMomentCommentViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView momentImageView;
        private TextView commentTextView;
        private TextView timestampTextView;

        ReceivedMomentCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            momentImageView = itemView.findViewById(R.id.momentImageView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            
            Log.d(TAG, "🏗️ ReceivedMomentCommentViewHolder created - Views found: " +
                "momentImage=" + (momentImageView != null) + 
                ", commentText=" + (commentTextView != null) + 
                ", timestamp=" + (timestampTextView != null));
        }

        void bind(ChatMessage message) {
            Log.d(TAG, "🎯 DEBUG_VIEWHOLDER_BIND: Binding received moment comment");
            
            if (message == null) {
                Log.e(TAG, "❌ DEBUG_VIEWHOLDER_BIND_FAILED: Message is null");
                return;
            }
            
            Log.d(TAG, "📋 Message data: " +
                "text='" + message.getText() + "', " +
                "momentImageUrl='" + message.getMomentImageUrl() + "', " +
                "timestamp=" + message.getTimestamp());

            // Set comment text
            if (commentTextView != null) {
                String commentText = message.getText();
                if (commentText != null && !commentText.trim().isEmpty()) {
                    commentTextView.setText(commentText);
                    commentTextView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "✅ Comment text set: " + commentText);
                } else {
                    commentTextView.setText("No comment");
                    commentTextView.setVisibility(View.VISIBLE);
                    Log.w(TAG, "⚠️ Comment text is empty, showing placeholder");
                }
            } else {
                Log.e(TAG, "❌ commentTextView is null");
            }

            // Format and set timestamp
            if (timestampTextView != null) {
                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String timeText = timeFormat.format(new Date(message.getTimestamp()));
                    timestampTextView.setText(timeText);
                    timestampTextView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "✅ Timestamp set: " + timeText);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error formatting timestamp: " + e.getMessage(), e);
                    timestampTextView.setText("--:--");
                    timestampTextView.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(TAG, "❌ timestampTextView is null");
            }

            // Load moment image with enhanced error handling
            if (momentImageView != null) {
                String imageUrl = message.getMomentImageUrl();
                Log.d(TAG, "🖼️ Loading moment image: " + imageUrl);
                
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                    
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .apply(options)
                            .into(momentImageView);
                    
                    Log.d(TAG, "✅ Started loading moment image");
                } else {
                    Log.w(TAG, "⚠️ Moment image URL is null or empty, showing placeholder");
                    momentImageView.setImageResource(R.drawable.avatar_placeholder);
                }
            } else {
                Log.e(TAG, "❌ momentImageView is null");
            }
            
            Log.d(TAG, "✅ DEBUG_VIEWHOLDER_BIND_SUCCESS: Received moment comment binding completed");
        }
    }
}
