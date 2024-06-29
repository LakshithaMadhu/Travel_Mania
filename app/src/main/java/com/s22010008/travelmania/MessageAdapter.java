package com.s22010008.travelmania;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private Context context;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageTextView.setText(message.getMessageText());
        holder.senderNameTextView.setText(message.getSenderName());

        if (message.getSenderPhotoUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(message.getSenderPhotoUrl())
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.user);
        }

        // Handling long-press events
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    // Show your BottomSheetDialogFragment
                    MyBottomSheetDialogFragment bottomSheet = new MyBottomSheetDialogFragment(clickedPosition, (DiscussActivity) context);
                    bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "BottomSheetDialog");
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
        public TextView messageTextView;
        public TextView senderNameTextView;
        public ImageView profileImageView;

        public MessageViewHolder(View view) {
            super(view);
            messageTextView = view.findViewById(R.id.messageTextView);
            senderNameTextView = view.findViewById(R.id.senderNameTextView);
            profileImageView = view.findViewById(R.id.profileImageView);
        }
    }

    public void updateMessage(int position, Message updatedMessage) {
        if (position >= 0 && position < messageList.size()) {
            messageList.set(position, updatedMessage);
            notifyItemChanged(position);
        }
    }
}
