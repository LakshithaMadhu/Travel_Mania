package com.s22010008.travelmania;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView emailTextView, nameTextView;
    private ImageView profileImageView;
    private Button editProfileButton;
    private Button logoutButton;
    private Button emergencyButton;
    private static final int EDIT_PROFILE_REQUEST = 2;
    static final int REQUEST_CODE_DISCUSS = 100; // Unique request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logoutButton = findViewById(R.id.logoutButton);

        auth = FirebaseAuth.getInstance();
        emailTextView = findViewById(R.id.emailTextView);
        nameTextView = findViewById(R.id.nameTextView);
        profileImageView = findViewById(R.id.profileImageView);
        editProfileButton = findViewById(R.id.editProfileButton);
        emergencyButton = findViewById(R.id.emergencyButton);

        user = auth.getCurrentUser(); // Fetch user once
        if (user != null) {
            emailTextView.setText(user.getEmail());
            nameTextView.setText(user.getDisplayName());
            loadProfileImage();
        }

        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        emergencyButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EmergencyActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
                startActivity(intent);
                finish();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // User refreshed, reload profile image and details
                    emailTextView.setText(user.getEmail());
                    nameTextView.setText(user.getDisplayName());
                    loadProfileImage();
                } else {
                    // Handle refresh failure
                    Toast.makeText(this, "Failed to refresh user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadProfileImage() {
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.user); // Set default image if no photo URL
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getBooleanExtra("PROFILE_UPDATED", false)) {
                // Refresh the user object to get the latest data
                user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    // Update name if changed
                    nameTextView.setText(user.getDisplayName());

                    // Load the new profile image
                    loadProfileImage();

                    // Signal DiscussActivity about the profile picture update
                    setResult(Activity.RESULT_OK, new Intent().putExtra("PROFILE_PICTURE_UPDATED", true));
                    finish(); // Finish ProfileActivity after setting the result
                } else {
                    // Handle the case where the user is unexpectedly null
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where profile was not updated
                Toast.makeText(this, "Profile update failed", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_DISCUSS && resultCode == Activity.RESULT_OK) {
            // Handle result from DiscussActivity if needed
            if (data != null && data.getBooleanExtra("PROFILE_PICTURE_UPDATED", false)) {
                // Profile picture was updated, refresh the UI
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    loadProfileImage();
                }
            }
        }
    }
}
