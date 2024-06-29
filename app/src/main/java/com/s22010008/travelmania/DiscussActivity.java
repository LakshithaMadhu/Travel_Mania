package com.s22010008.travelmania;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class DiscussActivity extends AppCompatActivity implements DiscussAdapter.OnItemClickListener {

    private EditText messageEditText;
    private RecyclerView recyclerView;
    private DiscussAdapter messageAdapter;
    private List<Message> messageList;
    private DBHelper dbHelper;
    private String placeId;
    private String placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discuss);

        // Initialize views and adapters
        messageEditText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);
        dbHelper = new DBHelper(this);

        messageList = new ArrayList<>();
        messageAdapter = new DiscussAdapter(messageList, this); // Pass 'this' as the listener
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve placeId and placeName from Intent
        Intent intent = getIntent();
        if (intent != null) {
            placeId = intent.getStringExtra("PLACE_ID");
            placeName = intent.getStringExtra("PLACE_NAME");
        }

        // Check if placeId is available, otherwise finish activity
        if (placeId == null) {
            Log.e("DiscussActivity", "Place ID is missing in Intent");
            Toast.makeText(this, "Error: Place ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load messages
        loadMessages(placeId);

        // Send button click listener
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        // Set place name
        TextView placeNameTextView = findViewById(R.id.textView5);
        placeNameTextView.setText(placeName != null ? placeName : "Discussion");
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName(); // Get sender name

            long messageId = dbHelper.insertMessage(messageText, placeId, placeName, senderId, senderName);
            if (messageId != -1) {
                Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                messageEditText.setText("");
                loadMessages(placeId);
            } else {
                Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages(String placeId) {
        messageList.clear();
        messageList.addAll(getAllMessages(placeId));
        messageAdapter.notifyDataSetChanged();
    }

    private List<Message> getAllMessages(String placeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Message> messages = new ArrayList<>();

        if (placeId == null) {
            Log.w("DiscussActivity", "getAllMessages() called with null placeId");
            return messages;
        }

        String selection = DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_ID + " = ?";
        String[] selectionArgs = { placeId };

        Cursor cursor = db.query(
                DiscussionContract.MessageEntry.TABLE_NAME,
                new String[]{
                        DiscussionContract.MessageEntry._ID,
                        DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE,
                        DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME,
                        DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_PHOTO_URL,
                        DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_ID,
                        DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME
                },
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int numMessages = cursor.getCount();
        Log.d("DiscussActivity", "Number of messages found for placeId " + placeId + ": " + numMessages);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        while (cursor.moveToNext()) {
            long messageId = cursor.getLong(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry._ID));
            String messageText = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE));
            String senderName = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME));
            String senderPhotoUrl = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_PHOTO_URL));
            String senderId = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_ID));
            String placeName = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME));

            // Override senderPhotoUrl if the sender is the current user and photo URL is available from FirebaseUser
            if (currentUserId != null && currentUserId.equals(senderId)) {
                if (currentUser.getPhotoUrl() != null) {
                    senderPhotoUrl = currentUser.getPhotoUrl().toString();
                }
            }

            Message message = new Message(messageText, senderName, senderPhotoUrl, messageId, senderId, placeId, placeName);
            messages.add(message);
        }
        cursor.close();
        return messages;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    public Message getMessageAtPosition(int position) {
        if (position >= 0 && position < messageList.size()) {
            return messageList.get(position);
        } else {
            return null;
        }
    }

    public void updateMessage(int position, Message updatedMessage) {
        boolean isUpdated = dbHelper.updateMessage(updatedMessage.getId(), updatedMessage.getMessageText());
        if (isUpdated) {
            messageList.set(position, updatedMessage);
            messageAdapter.notifyItemChanged(position);
            Toast.makeText(this, "Message updated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update message.", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteMessage(int position) {
        Message messageToDelete = messageList.get(position);
        boolean isDeleted = dbHelper.deleteMessage(messageToDelete.getId());
        if (isDeleted) {
            messageList.remove(position);
            messageAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Message deleted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete message.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemLongClick(int position) {
        // Handle long click event here
        MyBottomSheetDialogFragment bottomSheet = new MyBottomSheetDialogFragment(position, this);
        bottomSheet.show(getSupportFragmentManager(), "BottomSheetDialog");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ProfileActivity.REQUEST_CODE_DISCUSS && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getBooleanExtra("PROFILE_PICTURE_UPDATED", false)) {
                // Profile picture was updated, refresh messages and adapter
                loadMessages(placeId);
                messageAdapter.notifyDataSetChanged();
            }
        }
    }
}
