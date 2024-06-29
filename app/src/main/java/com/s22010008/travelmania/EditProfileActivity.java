package com.s22010008.travelmania;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.media3.common.util.Log;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Button chooseImageButton;
    private EditText nameEditText;
    private Button saveButton;
    private Uri selectedImageUri;

    private static final int REQUEST_IMAGE_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImageView = findViewById(R.id.profileImageView);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        nameEditText = findViewById(R.id.nameEditText);
        saveButton = findViewById(R.id.saveButton);

        chooseImageButton.setOnClickListener(view -> openImageChooser());

        saveButton.setOnClickListener(view -> saveChanges());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void saveChanges() {
        String newName = nameEditText.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Update user's display name
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("EditProfileActivity", "User profile updated.");
                            setResultAndFinish(true); // Indicate profile updated
                        } else {
                            Log.e("EditProfileActivity", "Failed to update profile: " + task.getException());
                            Toast.makeText(EditProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Upload profile picture to Firebase Storage (if selected)
            if (selectedImageUri != null) {
                uploadProfilePicture(user, selectedImageUri);
            } else {
                Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
                setResultAndFinish(true); // Indicate profile updated
            }
        }
    }

    private void uploadProfilePicture(FirebaseUser user, Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + user.getUid() + ".jpg");

        UploadTask uploadTask = profilePicRef.putFile(imageUri);
        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get download URL of the uploaded image
                profilePicRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // Update user's photo URL in Firebase Authentication
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadUri)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.d("EditProfileActivity", "User profile picture updated.");
                                    setResultAndFinish(true); // Indicate profile updated
                                } else {
                                    Log.e("EditProfileActivity", "Failed to update profile picture: " + updateTask.getException());
                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }).addOnFailureListener(e -> {
                    Log.e("EditProfileActivity", "Failed to get download URL: " + e.getMessage());
                    Toast.makeText(EditProfileActivity.this, "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Handle unsuccessful upload
                Log.e("EditProfileActivity", "Failed to upload profile picture: " + task.getException());
                Toast.makeText(EditProfileActivity.this, "Failed to upload profile picture.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setResultAndFinish(boolean profileUpdated) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("PROFILE_UPDATED", profileUpdated);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data.getData();
            Log.d("EditProfileActivity", "Selected Image URI: " + selectedImageUri); // Log the URI
            Glide.with(this).load(selectedImageUri).into(profileImageView);
        }
    }
}
