package com.s22010008.travelmania;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;

public class MyBottomSheetDialogFragment extends DialogFragment {

    private int position;
    private WeakReference<DiscussActivity> discussActivityRef; // Use WeakReference

    public MyBottomSheetDialogFragment(int position, DiscussActivity activity) {
        this.position = position;
        this.discussActivityRef = new WeakReference<>(activity);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        Button editButton = view.findViewById(R.id.editButton);
        Button deleteButton = view.findViewById(R.id.deleteButton);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscussActivity discussActivity = discussActivityRef.get();
                if (discussActivity != null) {
                    Message messageToEdit = discussActivity.getMessageAtPosition(position);
                    if (messageToEdit != null) {
                        // Get current user ID from Firebase
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                        // Add null check for messageToEdit.getSenderId()
                        if (currentUserId != null && messageToEdit.getSenderId() != null &&
                                messageToEdit.getSenderId().equals(currentUserId)) {
                            // Allow edit operation
                            // Create an AlertDialog for editing
                            final EditText input = new EditText(getContext());
                            input.setText(messageToEdit.getMessageText());

                            new AlertDialog.Builder(getContext())
                                    .setTitle("Edit Message")
                                    .setView(input)
                                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            String updatedText = input.getText().toString();
                                            messageToEdit.setMessageText(updatedText);
                                            discussActivity.updateMessage(position, messageToEdit);
                                            dismiss(); // Dismiss the bottom sheet
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        } else {
                            Toast.makeText(getContext(), "You can only edit your own messages", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscussActivity discussActivity = discussActivityRef.get();
                if (discussActivity != null) {
                    Message messageToDelete = discussActivity.getMessageAtPosition(position);
                    if (messageToDelete != null) {
                        // Get current user ID from Firebase
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                        // Add null check for messageToDelete.getSenderId()
                        if (currentUserId != null && messageToDelete.getSenderId() != null &&
                                messageToDelete.getSenderId().equals(currentUserId)) {
                            // Allow delete operation
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Delete Message")
                                    .setMessage("Are you sure you want to delete this message?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            discussActivity.deleteMessage(position); // Delete in the activity
                                            dismiss();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            Toast.makeText(getContext(), "You can only delete your own messages", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        return view;
    }
}
