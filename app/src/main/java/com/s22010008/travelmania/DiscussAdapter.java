package com.s22010008.travelmania;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DiscussAdapter extends RecyclerView.Adapter<DiscussAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private OnItemClickListener clickListener;

    public DiscussAdapter(List<Message> messageList, OnItemClickListener clickListener) {
        this.messageList = messageList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        final Message message = messageList.get(position);
        holder.messageTextView.setText(message.getMessageText());

        // Set the sender's name if available
        String senderName = message.getSenderName();
        if (senderName != null && !senderName.isEmpty()) {
            holder.senderNameTextView.setText(senderName);
            holder.senderNameTextView.setVisibility(View.VISIBLE); // Ensure it's visible
        } else {
            holder.senderNameTextView.setVisibility(View.GONE); // Hide if no name
        }

        // Load sender's photo using Glide
        if (message.getSenderPhotoUrl() != null && !message.getSenderPhotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(message.getSenderPhotoUrl())
                    .circleCrop() // Apply circle crop transformation
                    .into(holder.senderPhotoImageView); // Assuming you have an ImageView in your ViewHolder
        } else {
            // Set a default image if no photo URL is available
            holder.senderPhotoImageView.setImageResource(R.drawable.user);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemLongClick(position);
                }
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView senderNameTextView;
        ImageView senderPhotoImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
            senderPhotoImageView = itemView.findViewById(R.id.profileImageView);
        }
    }

    public interface OnItemClickListener {
        void onItemLongClick(int position);
    }
}
