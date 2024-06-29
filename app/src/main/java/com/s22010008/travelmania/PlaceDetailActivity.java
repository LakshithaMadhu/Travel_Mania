package com.s22010008.travelmania;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places; // Import Places
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

public class PlaceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_NAME = "place_name";
    public static final String EXTRA_PLACE_PHOTO_METADATA = "place_photo_metadata";
    public static final String EXTRA_PLACE_DESCRIPTION = "place_description";

    private ImageView placeImage;
    private TextView placeNameTextView;
    private TextView placeDescriptionTextView;
    private Button discussButton;
    private Button weatherButton;
    private DBHelper dbHelper;
    private String placeId;

    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__place_detail);

        placeImage = findViewById(R.id.imageView3);
        placeNameTextView = findViewById(R.id.textView5);
        placeDescriptionTextView = findViewById(R.id.textView12);
        discussButton = findViewById(R.id.discussButton);
        Button weatherButton = findViewById(R.id.weatherButton);
        dbHelper = DBHelper.getInstance(this);


        // Initialize Places SDK *before* creating the client
        Places.initialize(getApplicationContext(), "AIzaSyAK9rS4LSI-WQvTIHaGMQWkKOStVGK8SEw"); // Replace with your API key

        placesClient = com.google.android.libraries.places.api.Places.createClient(this);

        Intent intent = getIntent();
        if (intent != null) {
            double latitude = intent.getDoubleExtra("PLACE_LATITUDE", 0.0);
            double longitude = intent.getDoubleExtra("PLACE_LONGITUDE", 0.0);
            String placeName = intent.getStringExtra(EXTRA_PLACE_NAME);
            PhotoMetadata photoMetadata = intent.getParcelableExtra(EXTRA_PLACE_PHOTO_METADATA);
            String placeDescription = intent.getStringExtra(EXTRA_PLACE_DESCRIPTION);

            if (placeName != null) {
                placeNameTextView.setText(placeName);
            }

            if (photoMetadata != null) {
                fetchPlacePhoto(photoMetadata);
            }

            if (placeDescription != null) {
                placeDescriptionTextView.setText(placeDescription);
            } else {
                placeDescriptionTextView.setText("No description available.");
            }
        }

        placeId = getPlaceId();

        discussButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeId != null && !placeId.isEmpty()) {
                    Intent intent = new Intent(PlaceDetailActivity.this, DiscussActivity.class);
                    intent.putExtra("PLACE_NAME", placeNameTextView.getText().toString());
                    intent.putExtra("PLACE_ID", placeId);
                    startActivity(intent);
                } else {
                    Toast.makeText(PlaceDetailActivity.this, "Discussion for this place is not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the original intent that started PlaceDetailActivity
                Intent originalIntent = getIntent();
                if (originalIntent != null) {
                    double latitude = originalIntent.getDoubleExtra("PLACE_LATITUDE", 0.0);
                    double longitude = originalIntent.getDoubleExtra("PLACE_LONGITUDE", 0.0);
                    String placeName = originalIntent.getStringExtra(EXTRA_PLACE_NAME);

                    // Create a new intent for WeatherActivity
                    Intent weatherIntent = new Intent(PlaceDetailActivity.this, WeatherActivity.class);
                    weatherIntent.putExtra("LATITUDE", latitude);
                    weatherIntent.putExtra("LONGITUDE", longitude);
                    weatherIntent.putExtra("PLACE_NAME", placeName);
                    startActivity(weatherIntent);
                }
            }
        });
    }

    private void fetchPlacePhoto(PhotoMetadata photoMetadata) {
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500)
                .build();

        placesClient.fetchPhoto(photoRequest)
                .addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                    @Override
                    public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        Glide.with(PlaceDetailActivity.this).load(bitmap).into(placeImage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        placeImage.setImageResource(R.drawable.placeholder);
                    }
                });
    }

    private String getPlaceId() {
        // 1. Try to get placeId from Intent
        if (getIntent().hasExtra("PLACE_ID")) {
            String placeIdFromIntent = getIntent().getStringExtra("PLACE_ID");
            Log.d("PlaceDetailActivity", "Place ID from Intent: " + placeIdFromIntent);
            return placeIdFromIntent;
        }

        // 2. If not in Intent, try to get it from the database
        String placeName = getIntent().getStringExtra(EXTRA_PLACE_NAME);
        if (placeName != null && !placeName.isEmpty() && dbHelper != null) {
            String placeIdFromDB = dbHelper.getPlaceIdForName(placeName);
            if (placeIdFromDB != null) {
                Log.d("PlaceDetailActivity", "Place ID from Database: " + placeIdFromDB);
                return placeIdFromDB;
            } else {
                Log.w("PlaceDetailActivity", "Place ID not found in database for name: " + placeName);
            }
        } else {
            Log.w("PlaceDetailActivity", "Place name is null or empty, or DBHelper is null");
        }

        // 3. If still not found, generate a new one and store it
        String newPlaceId = generateAndStoreNewPlaceId(placeName);
        if (newPlaceId != null) {
            Log.d("PlaceDetailActivity", "Generated new Place ID: " + newPlaceId);
        } else {
            Log.e("PlaceDetailActivity", "Failed to generate and store new Place ID");
        }
        return newPlaceId; // Always return the new ID if it was generated
    }

    private String generateAndStoreNewPlaceId(String placeName) {
        if (placeName == null) {
            return null; // Can't generate a placeId without a placeName
        }

        String newPlaceId = java.util.UUID.randomUUID().toString();

        if (dbHelper != null) {
            long result = dbHelper.insertPlace(newPlaceId, placeName);
            if (result != -1) {
                Log.d("PlaceDetailActivity", "New placeId generated and stored: " + newPlaceId);
                return newPlaceId;
            } else {
                Log.e("PlaceDetailActivity", "Failed to insert new placeId");
                return null;
            }
        } else {
            Log.e("PlaceDetailActivity", "DBHelper is null, cannot store new placeId");
            return null;
        }
    }

    public void updatePlaceDescription(String description) {
        if (placeDescriptionTextView != null) {
            if (description != null) {
                placeDescriptionTextView.setText(description);
            } else {
                placeDescriptionTextView.setText("No description available.");
            }
        }
    }
}
