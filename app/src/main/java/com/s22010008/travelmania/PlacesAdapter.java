package com.s22010008.travelmania;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> places;
    private PlacesClient placesClient;
    private DBHelper dbHelper;

    public PlacesAdapter(Context context) {
        this.context = context;
        Places.initialize(context, "AIzaSyAK9rS4LSI-WQvTIHaGMQWkKOStVGK8SEw");
        placesClient = Places.createClient(context);
        dbHelper = DBHelper.getInstance(context);
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.placeNameTextView.setText(place.getName());

        List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
        if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
            holder.bindPhotoMetadata(photoMetadataList.get(0));
        } else {
            holder.bindPhotoMetadata(null);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Place selectedPlace = places.get(currentPosition);
                String placeName = selectedPlace.getName();

                // Get or generate placeId
                String placeId = dbHelper.getPlaceIdForName(placeName);
                if (placeId == null) {
                    placeId = dbHelper.generateAndInsertPlaceId(placeName);
                }

                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra("PLACE_ID", placeId);
                intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_NAME, placeName);
                if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                    intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_PHOTO_METADATA, photoMetadataList.get(0));
                }
                context.startActivity(intent);

                new FetchWikipediaTask(context, placeName, placeId,
                        photoMetadataList != null && !photoMetadataList.isEmpty() ? photoMetadataList.get(0) : null,
                        summary -> {
                            if (context instanceof PlaceDetailActivity) {
                                ((PlaceDetailActivity) context).updatePlaceDescription(summary);
                            }
                        }).execute();
            }
        });
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView placeNameTextView;
        ImageView placeImageView;
        Button placeBtn;
        ProgressBar progressBar;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeNameTextView = itemView.findViewById(R.id.place_name);
            placeImageView = itemView.findViewById(R.id.place_image);
            placeBtn = itemView.findViewById(R.id.read_more_button);
            progressBar = itemView.findViewById(R.id.progress_bar);

            placeBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Place selectedPlace = places.get(position);
                    String placeName = selectedPlace.getName();

                    // Get or generate placeId
                    String placeId = dbHelper.getPlaceIdForName(placeName);
                    if (placeId == null) {
                        placeId = dbHelper.generateAndInsertPlaceId(placeName);
                    }

                    Intent intent = new Intent(context, PlaceDetailActivity.class);
                    intent.putExtra("PLACE_LATITUDE", selectedPlace.getLatLng().latitude);
                    intent.putExtra("PLACE_LONGITUDE", selectedPlace.getLatLng().longitude);
                    intent.putExtra("PLACE_ID", placeId);
                    intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_NAME, placeName);
                    List<PhotoMetadata> photoMetadataList = selectedPlace.getPhotoMetadatas();
                    if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                        intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_PHOTO_METADATA, photoMetadataList.get(0));
                    }
                    context.startActivity(intent);

                    new FetchWikipediaTask(context, placeName, placeId,
                            photoMetadataList != null && !photoMetadataList.isEmpty() ? photoMetadataList.get(0) : null,
                            summary -> {
                                if (context instanceof PlaceDetailActivity) {
                                    ((PlaceDetailActivity) context).updatePlaceDescription(summary);
                                }
                            }).execute();
                }
            });
        }

        public void bindPhotoMetadata(PhotoMetadata photoMetadata) {
            if (photoMetadata != null) {
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500)
                        .build();

                progressBar.setVisibility(View.VISIBLE);

                placesClient.fetchPhoto(photoRequest)
                        .addOnSuccessListener(fetchPhotoResponse -> {
                            Bitmap bitmap = fetchPhotoResponse.getBitmap();
                            Glide.with(context)
                                    .load(bitmap)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(placeImageView);
                            progressBar.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(exception -> {
                            Log.e("PlacesAdapter", "Error fetching photo: " + exception.getMessage());
                            placeImageView.setImageResource(R.drawable.placeholder);
                            progressBar.setVisibility(View.GONE);
                        });
            } else {
                placeImageView.setImageResource(R.drawable.placeholder);
            }
        }
    }

    private static class FetchWikipediaTask extends AsyncTask<Void, Void, String> {

        private final Context context;
        private final String placeName;
        private final PhotoMetadata photoMetadata;
        private final String placeId;
        private final WikipediaSummaryListener listener;

        public interface WikipediaSummaryListener {
            void onSummaryFetched(String summary);
        }

        public FetchWikipediaTask(Context context, String placeName, String placeId, PhotoMetadata photoMetadata, WikipediaSummaryListener listener) {
            this.context = context;
            this.placeName = placeName;
            this.placeId = placeId;
            this.photoMetadata = photoMetadata;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return fetchWikipediaSummary(placeName);
        }

        @Override
        protected void onPostExecute(String wikiSummary) {
            if (listener != null) {
                listener.onSummaryFetched(wikiSummary);
            }
        }

        private String fetchWikipediaSummary(String placeName) {
            String wikiUrl = "https://en.wikipedia.org/w/api.php";
            String encodedPlaceName;
            try {
                encodedPlaceName = URLEncoder.encode(placeName, "UTF-8");
            } catch (Exception e) {
                Log.e("PlacesAdapter", "Error encoding place name: " + e.getMessage());
                return "No description available.";
            }

            String url = wikiUrl + "?action=query&format=json&prop=extracts&exintro&explaintext&redirects=1&titles=" + encodedPlaceName;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject query = jsonObject.getJSONObject("query");
                    JSONObject pages = query.getJSONObject("pages");
                    String firstPageKey = pages.keys().next();
                    JSONObject firstPage = pages.getJSONObject(firstPageKey);
                    if (firstPage.has("extract")) {
                        return firstPage.getString("extract");
                    } else {
                        Log.w("PlacesAdapter", "No Wikipedia summary available for: " + placeName);
                        return "No description available.";
                    }
                }
            } catch (Exception e) {
                Log.e("PlacesAdapter", "Error fetching Wikipedia summary: " + e.getMessage());
            }
            return "No description available.";
        }
    }

    public void shutdown() {
        placesClient = null;
    }
}
