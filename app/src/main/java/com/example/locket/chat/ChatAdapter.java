package com.example.locket.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.chat.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT_SENT = 1;
    private static final int VIEW_TYPE_TEXT_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;

    private List<ChatMessage> chatMessages;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> chatMessages, String currentUserId) {
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        boolean isSentByMe = message.getSenderId().equals(currentUserId);

        if (message.getType() == ChatMessage.MessageType.IMAGE || message.getType() == ChatMessage.MessageType.VIDEO) {
            return isSentByMe ? VIEW_TYPE_IMAGE_SENT : VIEW_TYPE_IMAGE_RECEIVED;
        } else { // TEXT
            return isSentByMe ? VIEW_TYPE_TEXT_SENT : VIEW_TYPE_TEXT_RECEIVED;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_TEXT_SENT:
                return new SentMessageViewHolder(inflater.inflate(R.layout.item_chat_message_sent, parent, false));
            case VIEW_TYPE_TEXT_RECEIVED:
                return new ReceivedMessageViewHolder(inflater.inflate(R.layout.item_chat_message_received, parent, false));
            case VIEW_TYPE_IMAGE_SENT:
                return new SentImageViewHolder(inflater.inflate(R.layout.item_chat_image_sent, parent, false));
            case VIEW_TYPE_IMAGE_RECEIVED:
                return new ReceivedImageViewHolder(inflater.inflate(R.layout.item_chat_image_received, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
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
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // ViewHolder for sent messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        void bind(ChatMessage message) {
            messageTextView.setText(message.getText());
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
            messageTextView.setText(message.getText());
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
            Glide.with(itemView.getContext())
                    .load(message.getMediaUrl())
                    .placeholder(R.drawable.avatar_placeholder)
                    .into(imageMessageView);
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
            Glide.with(itemView.getContext())
                    .load(message.getMediaUrl())
                    .placeholder(R.drawable.avatar_placeholder)
                    .into(imageMessageView);
        }
    }
}
