package com.s22010008.travelmania;

import static com.s22010008.travelmania.R.id.search_button;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.model.RectangularBounds;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public class Dashboard extends AppCompatActivity implements PermissionsHelper.PermissionCallback {


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private PermissionsHelper permissionsHelper;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private EditText searchLocation;
    private PlacesClient placesClient;
    private RecyclerView recyclerView;
    private PlacesAdapter placesAdapter;
    private int totalPredictionsCount = 0;
    private int fetchedPredictionsCount = 0;
    private static final int REQUEST_CHECK_SETTINGS = 5000;
    private CountingIdlingResource idlingResource = new CountingIdlingResource("PlacesApiCalls");

    private double searchRadiusInMeters = 500;  // Set default radius to 5 km

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        permissionsHelper = new PermissionsHelper(this, this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        Button showNearbyPlacesButton = findViewById(R.id.location_button); // Assuming this is your new button
        recyclerView = findViewById(R.id.places_recycler_view);
        ImageButton profileButton = findViewById(R.id.imageButton2);
        Button searchButton = findViewById(search_button);
        searchLocation = findViewById(R.id.editTextLocation);

        setSearchRadius(5, "km");

        // Initialize Places API
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesAdapter = new PlacesAdapter(this);
        recyclerView.setAdapter(placesAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        showNearbyPlacesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveLocation();
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, ProfileActivity.class));
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchLocation.getText().toString();
                Log.d("Dashboard", "Search Query: " + searchQuery);
                if (!searchQuery.trim().isEmpty()) {
                    searchForPlace(searchQuery);
                } else {
                    Toast.makeText(Dashboard.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        permissionsHelper.requestLocationPermission();
    }


    @Override
    public void onPermissionGranted(String permission) {
        if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            getDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

     // Define a request code

    // Method to check and request location updates
    private void getDeviceLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(Dashboard.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Log.e("Dashboard", "Error resolving location settings: ", sendEx);
                        Toast.makeText(Dashboard.this, "Error resolving location settings", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle other location settings errors
                    Log.e("Dashboard", "Location settings error: ", e);
                    Toast.makeText(Dashboard.this, "Location settings error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to retrieve location after settings are resolved
    private void retrieveLocation() {
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            lastKnownLocation = task.getResult();
                            Log.e("Dashboard", "Latitude: " + lastKnownLocation.getLatitude());
                            Log.e("Dashboard", "Longitude: " + lastKnownLocation.getLongitude());

                            findNearbyPlaces(); // Call findNearbyPlaces after getting location
                        } else {
                            // Handle the case where location is null
                            requestNewLocationData(); // Request new location data if location is null
                        }
                    } else {
                        // Handle location request failure
                        Log.e("Dashboard", "Location request failed: " + task.getException());
                        Toast.makeText(Dashboard.this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Dashboard", "SecurityException: " + e.getMessage());
        }
    }


    // Add this new method to request a fresh location update
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000); // Update interval in milliseconds
        mLocationRequest.setFastestInterval(2000); // Fastest update interval

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied, attempt to request location updates
                try {
                    fusedLocationProviderClient.requestLocationUpdates(
                            mLocationRequest,
                            new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    lastKnownLocation = locationResult.getLastLocation();
                                    // Update your UI or logic with the new location here
                                    androidx.media3.common.util.Log.d("Dashboard", "New Location: " + lastKnownLocation);
                                }
                            },
                            null
                    );
                } catch (SecurityException e) {
                    androidx.media3.common.util.Log.e("Dashboard", "SecurityException: " + e.getMessage());
                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(Dashboard.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        androidx.media3.common.util.Log.e("Dashboard", "Error resolving location settings: ", sendEx);
                        Toast.makeText(Dashboard.this, "Error resolving location settings", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    androidx.media3.common.util.Log.e("Dashboard", "Location settings error: ", e);
                    Toast.makeText(Dashboard.this, "Location settings error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                retrieveLocation();
            } else {
                // User did not enable location settings
                Toast.makeText(this, "Location access is required to show nearby places", Toast.LENGTH_SHORT).show();
                // Don't automatically fetch places here
            }
        }
    }

    private void findNearbyPlaces() {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);
        List<Place> allPlaces = new ArrayList<>();
        Set<Place> uniquePlaces = new HashSet<>();
        idlingResource.increment();

        String[] queries = {
                "Sri Lanka waterfalls",
                "Sri Lanka lakes",
                "Sri Lanka national parks",
                "Sri Lanka botanical gardens",
                "Sri Lanka safari",
                "Sri Lanka beaches",
                "Sri Lanka zoo",
                "Sri Lanka ancient cities",
                "Sri Lanka temples",
                "Sri Lanka tea plantations",
                "Sri Lanka mountains",
                "Sri Lanka cultural sites",
                "Sri Lanka hiking trails",
                "Sri Lanka wildlife sanctuaries",
                "Sri Lanka UNESCO World Heritage Sites",
                "Sri Lanka museums",
                "Sri Lanka adventure sports",
                "Sri Lanka city tours",
                "Sri Lanka bird watching",
                "Sri Lanka diving spots",
                "Sri Lanka surfing",
                "Sri Lanka luxury resorts",
                "Sri Lanka heritage tours",
                "Sri Lanka historical landmarks",
                "Sri Lanka elephant orphanage",
                "Sri Lanka rainforest",
                "Sri Lanka whale watching",
                "Sri Lanka rock climbing",
                "Sri Lanka waterfalls trekking",
                "Sri Lanka photography tours",
                "Sri Lanka cultural villages",
                "Sri Lanka art galleries",
                "Sri Lanka pilgrimage sites",
                "Kataragama Kiri Wehera",
                "Temple of the Tooth",
                "Dambulla Raja Maha Viharaya",
                "Lotus Tower Sri Lanka",
                "Ambulugala Tower",
                "Sigiriya Sri Lanka",
                "Koneshwaram Temple",
                "Kalutara Temple",
                "Kelaniya Temple"
        };

        double radiusInLatLngDegreesLat = searchRadiusInMeters / 111000.0;
        double radiusInLatLngDegreesLng = searchRadiusInMeters / (111000.0 * Math.cos(Math.toRadians(lastKnownLocation.getLatitude())));

        totalPredictionsCount = 0;
        fetchedPredictionsCount = 0;

        for (String query : queries) {
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setCountry("LK")
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setLocationBias(RectangularBounds.newInstance(
                            new LatLng(lastKnownLocation.getLatitude() - radiusInLatLngDegreesLat,
                                    lastKnownLocation.getLongitude() - radiusInLatLngDegreesLng),
                            new LatLng(lastKnownLocation.getLatitude() + radiusInLatLngDegreesLat,
                                    lastKnownLocation.getLongitude() + radiusInLatLngDegreesLng)

                    ))
                    .build();

            placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindAutocompletePredictionsResponse response = task.getResult();
                        totalPredictionsCount += response.getAutocompletePredictions().size();
                        Log.d("Dashboard", "Predictions count: " + response.getAutocompletePredictions().size());
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            fetchPlaceDetails(prediction.getPlaceId(), uniquePlaces); // Pass the Set
                        }
                    } else {
                        Log.e("Dashboard", "Place response exception: ", task.getException());
                        Toast.makeText(Dashboard.this, "Failed to find nearby places.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Helper method to fetch full Place details
    private void fetchPlaceDetails(String placeId, Set<Place> uniquePlaces) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);

        placesClient.fetchPlace(FetchPlaceRequest.newInstance(placeId, placeFields))
                .addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();

                        if (isValidPlace(place)) {
                            uniquePlaces.add(place); // Add to the Set
                        }

                        fetchedPredictionsCount++;
                        if (fetchedPredictionsCount == totalPredictionsCount) {
                            displayNearbyPlaces(new ArrayList<>(uniquePlaces));
                            idlingResource.decrement(); // Convert Set to List for display
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Dashboard", "Error fetching place details: " + exception.getMessage());
                        fetchedPredictionsCount++;
                        if (fetchedPredictionsCount == totalPredictionsCount) {
                            displayNearbyPlaces(new ArrayList<>(uniquePlaces));
                            idlingResource.decrement();// Handle failure by displaying existing places
                        }
                    }
                });
    }

    private void searchForPlace(String query) {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(token)
                .setCountry("LK")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onSuccess(FindAutocompletePredictionsResponse response) {
                        List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                        Log.d("Dashboard", "Predictions count: " + predictions.size());
                        List<Place> matchingPlaces = new ArrayList<>();

                        for (AutocompletePrediction prediction : predictions) {
                            fetchPlaceDetails(prediction.getPlaceId(),
                                    new OnSuccessListener<FetchPlaceResponse>() {
                                        @Override
                                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                            Place place = fetchPlaceResponse.getPlace();

                                            if (isValidPlace(place)) {
                                                matchingPlaces.add(place);
                                            }

                                            if (matchingPlaces.size() == predictions.size()) {
                                                displayNearbyPlaces(matchingPlaces);
                                            }
                                        }
                                    },
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.e("Dashboard", "Error fetching place details: " + exception.getMessage());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Dashboard", "Error searching for places: ", exception);
                        Toast.makeText(Dashboard.this, "Failed to search for places", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public IdlingResource getIdlingResource() {
        return idlingResource;
    }

    private void displayNearbyPlaces(List<Place> places) {
        Log.d("Dashboard", "Displaying places, count: " + places.size());
        placesAdapter.setPlaces(places);
        placesAdapter.notifyDataSetChanged();
    }


    // Method to set the search radius (can be called from other parts of your app)
    public void setSearchRadius(double radius, String unit) {
        if (unit.equalsIgnoreCase("km")) {
            this.searchRadiusInMeters = radius * 1000; // Convert kilometers to meters
        } else if (unit.equalsIgnoreCase("miles")) {
            this.searchRadiusInMeters = radius * 1609.34; // Convert miles to meters
        } else {
            this.searchRadiusInMeters = radius; // Assume the provided radius is in meters
        }
    }

    private void fetchPlaceDetails(String placeId, OnSuccessListener<FetchPlaceResponse> successListener, OnFailureListener failureListener) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
        placesClient.fetchPlace(request)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        placesClient = null;
    }

    private boolean isValidPlace(Place place) {
        String name = place.getName() != null ? place.getName().toLowerCase() : "";

        if (name.contains("gate") ||
                name.contains("car park") || name.contains("parking") || name.contains("air force") || name.contains("surgery")
                || name.contains("hospital") || name.contains("sri lanka telecom") ||
                name.contains("kids park") || name.contains("zoomtech") || name.contains("vets")
                || name.contains("insurance") || name.contains("air force") || name.contains("rental cars ") ||
                name.contains("chair") || name.contains("junction") || name.contains("ticket counter") || name.contains("sri lanka") ||
                name.contains("clothing") || name.contains("temple trees") ||
                (place.getTypes() != null && place.getTypes().contains(Place.Type.PARKING))) {
            return false;
        }

        return true;
    }

}
