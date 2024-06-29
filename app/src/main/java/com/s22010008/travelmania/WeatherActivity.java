package com.s22010008.travelmania;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeatherActivity extends AppCompatActivity {

    private TextView cityNameTextView, temperatureTextView, conditionsTextView, humidityTextView,
            windSpeedTextView;
    private ImageView weatherIconImageView;
    private RecyclerView hourlyForecastRecyclerView, dailyForecastRecyclerView;

    public static Map<String, Integer> weatherIconMap;
    private WeatherAdapter hourlyAdapter, dailyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        cityNameTextView = findViewById(R.id.tv_city_name);
        temperatureTextView = findViewById(R.id.tv_temperature);
        conditionsTextView = findViewById(R.id.tv_conditions);
        humidityTextView = findViewById(R.id.tv_humidity);
        windSpeedTextView = findViewById(R.id.tv_wind_speed);
        weatherIconImageView = findViewById(R.id.weather_icon);
        hourlyForecastRecyclerView = findViewById(R.id.hourly_forecast_recyclerview);
        dailyForecastRecyclerView = findViewById(R.id.daily_forecast_recyclerview);

        hourlyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dailyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        hourlyAdapter = new WeatherAdapter(new ArrayList<>());
        hourlyForecastRecyclerView.setAdapter(hourlyAdapter);

        dailyAdapter = new WeatherAdapter(new ArrayList<>());
        dailyForecastRecyclerView.setAdapter(dailyAdapter);

        initializeWeatherIconMap();

        Intent intent = getIntent();
        if (intent != null) {
            String placeName = intent.getStringExtra("PLACE_NAME");
            double latitude = intent.getDoubleExtra("LATITUDE", 0.0);
            double longitude = intent.getDoubleExtra("LONGITUDE", 0.0);
            if (placeName != null) {
                cityNameTextView.setText(placeName);
                try {
                    fetchWeatherData(latitude, longitude);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void initializeWeatherIconMap() {
        weatherIconMap = new HashMap<>();
        weatherIconMap.put("0", R.drawable.ic_sunny);
        weatherIconMap.put("1", R.drawable.ic_partlycloudy);
        weatherIconMap.put("2", R.drawable.ic_partlycloudy);
        weatherIconMap.put("3", R.drawable.ic_cloudy);
        weatherIconMap.put("45", R.drawable.ic_foggy);
        weatherIconMap.put("48", R.drawable.ic_foggy);
        weatherIconMap.put("51", R.drawable.ic_rainy);
        weatherIconMap.put("53", R.drawable.ic_rainy);
        weatherIconMap.put("55", R.drawable.ic_rainy);
        weatherIconMap.put("56", R.drawable.ic_rainy);
        weatherIconMap.put("57", R.drawable.ic_rainy);
        weatherIconMap.put("61", R.drawable.ic_rainy);
        weatherIconMap.put("63", R.drawable.ic_rainy);
        weatherIconMap.put("65", R.drawable.ic_rainy);
        weatherIconMap.put("66", R.drawable.ic_rainy);
        weatherIconMap.put("67", R.drawable.ic_rainy);
        weatherIconMap.put("71", R.drawable.ic_snow);
        weatherIconMap.put("73", R.drawable.ic_snow);
        weatherIconMap.put("75", R.drawable.ic_snow);
        weatherIconMap.put("77", R.drawable.ic_snow);
        weatherIconMap.put("80", R.drawable.ic_rainy);
        weatherIconMap.put("81", R.drawable.ic_rainy);
        weatherIconMap.put("82", R.drawable.ic_rainy);
        weatherIconMap.put("85", R.drawable.ic_snow);
        weatherIconMap.put("86", R.drawable.ic_snow);
        weatherIconMap.put("95", R.drawable.ic_thunder);
        weatherIconMap.put("96", R.drawable.ic_thunder);
        weatherIconMap.put("99", R.drawable.ic_thunder);
    }

    private void fetchWeatherData(double latitude, double longitude) throws UnsupportedEncodingException {

        String url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8") +
                "&longitude=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8") +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m" + // Removed ,time
                "&daily=temperature_2m_max,temperature_2m_min,weathercode,windspeed_10m_max" + // Removed ,time
                "&current_weather=true&timezone=" + URLEncoder.encode("Asia/Colombo", "UTF-8");

        Log.d("WeatherActivity", "Request URL: " + url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("WeatherActivity", "Error fetching weather: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(WeatherActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        Log.d("WeatherActivity", "Response successful: Code " + response.code());
                        String responseData = response.body().string();
                        Log.d("WeatherActivity", "JSON Response: " + responseData);
                        JSONObject json = new JSONObject(responseData);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                updateWeatherUI(json);
                            } catch (JSONException e) {
                                Log.e("WeatherActivity", "Error parsing weather data: " + e.getMessage(), e);
                                Toast.makeText(WeatherActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("WeatherActivity", "Error parsing JSON: " + e.getMessage(), e);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(WeatherActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
                else {
                    Log.e("WeatherActivity", "Response not successful: Code " + response.code());

                }
            }
        });
    }


    private void updateWeatherUI(JSONObject json) throws JSONException {
        // Current Weather
        JSONObject currentWeather = json.getJSONObject("current_weather");
        double currentTemperature = currentWeather.getDouble("temperature");
        String currentWeatherCode = currentWeather.getString("weathercode");
        double currentWindSpeed = currentWeather.getDouble("windspeed");

        // Extract humidity from hourly data
        JSONArray hourlyRelativeHumidity = json.getJSONObject("hourly").getJSONArray("relativehumidity_2m");
        int humidity = hourlyRelativeHumidity.optInt(0, 0); // Assuming first element is current humidity



        // Update current weather UI elements
        temperatureTextView.setText(String.format("%.1f°C", currentTemperature));
        conditionsTextView.setText(getWeatherDescription(currentWeatherCode));
        humidityTextView.setText("Humidity: " + humidity + "%");
        windSpeedTextView.setText("Wind Speed: " + currentWindSpeed + " km/h");


        weatherIconImageView.setImageResource(weatherIconMap.getOrDefault(currentWeatherCode, R.drawable.placeholder));

        // Hourly Forecast
        JSONArray hourlyForecastTimes = json.getJSONObject("hourly").getJSONArray("time");
        JSONArray hourlyForecastTemperatures = json.getJSONObject("hourly").getJSONArray("temperature_2m");
        JSONArray hourlyForecastWeathercodes = json.getJSONObject("hourly").getJSONArray("weathercode");
        JSONArray hourlyForecastWindSpeeds = json.getJSONObject("hourly").getJSONArray("windspeed_10m");

        List<WeatherData> hourlyDataList = new ArrayList<>();
        for (int i = 0; i < hourlyForecastTemperatures.length(); i++) {
            double hourlyTemp = hourlyForecastTemperatures.getDouble(i);
            String hourlyCode = hourlyForecastWeathercodes.getString(i);
            String hourlyTime = formatTime(hourlyForecastTimes.getString(i));
            double hourlyWindSpeed = hourlyForecastWindSpeeds.getDouble(i);
            hourlyDataList.add(new WeatherData(hourlyTemp, getWeatherDescription(hourlyCode), hourlyCode, hourlyTime, hourlyWindSpeed));
        }

        // Daily Forecast
        JSONArray dailyForecastTimes = json.getJSONObject("daily").getJSONArray("time");
        JSONArray dailyForecastMaxTemperatures = json.getJSONObject("daily").getJSONArray("temperature_2m_max");
        JSONArray dailyForecastMinTemperatures = json.getJSONObject("daily").getJSONArray("temperature_2m_min");
        JSONArray dailyForecastWeathercodes = json.getJSONObject("daily").getJSONArray("weathercode");
        JSONArray dailyForecastWindSpeeds = json.getJSONObject("daily").getJSONArray("windspeed_10m_max");

        List<WeatherData> dailyDataList = new ArrayList<>();
        for (int i = 0; i < dailyForecastMaxTemperatures.length(); i++) {
            double dailyMaxTemp = dailyForecastMaxTemperatures.getDouble(i);
            double dailyMinTemp = dailyForecastMinTemperatures.getDouble(i);
            String dailyCode = dailyForecastWeathercodes.getString(i);
            String dailyTime = formatDate(dailyForecastTimes.getString(i));
            double dailyWindSpeed = dailyForecastWindSpeeds.getDouble(i);
            dailyDataList.add(new WeatherData(dailyMaxTemp, dailyMinTemp, getWeatherDescription(dailyCode), dailyCode, dailyTime, dailyWindSpeed));
        }

        Log.d("WeatherActivity", "Current Temperature: " + currentTemperature);
        Log.d("WeatherActivity", "Hourly Data Size: " + hourlyDataList.size());
        Log.d("WeatherActivity", "Daily Data Size: " + dailyDataList.size());

        hourlyAdapter.updateDataList(hourlyDataList);
        hourlyAdapter.notifyDataSetChanged();

        dailyAdapter.updateDataList(dailyDataList);
        dailyAdapter.notifyDataSetChanged();
    }

    private String formatTime(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("h a", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTime;
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault()); // Example format
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    private String getWeatherDescription(String weatherCode) {
        switch (weatherCode) {
            case "0":
                return "Clear Sky";
            case "1":
            case "2":
                return "Partly Cloudy";
            case "3":
                return "Cloudy";
            case "45":
            case "48":
                return "Foggy";
            case "51":
            case "53":
            case "55":
            case "56":
            case "57":
                return "Drizzle";
            case "61":
            case "63":
            case "65":
                return "Rain";
            case "66":
            case "67":
                return "Freezing Rain";
            case "71":
            case "73":
            case "75":
            case "77":
                return "Snowfall";
            case "80":
            case "81":
            case "82":
                return "Rain Showers";
            case "85":
            case "86":
                return "Snow Showers";
            case "95":
            case "96":
            case "99":
                return "Thunderstorm";
            default:
                return "Unknown";
        }
    }

    public static class WeatherData {
        private double temperature;
        private double minTemperature;
        private String conditions;
        private String weatherCode;
        private String time;
        private double windSpeed;

        public WeatherData(double temperature, String conditions, String weatherCode, String time, double windSpeed) {
            this.temperature = temperature;
            this.conditions = conditions;
            this.weatherCode = weatherCode;
            this.time = time;
            this.windSpeed = windSpeed;
        }

        public WeatherData(double maxTemperature, double minTemperature, String conditions, String weatherCode, String time, double windSpeed) {
            this.temperature = maxTemperature;
            this.minTemperature = minTemperature;
            this.conditions = conditions;
            this.weatherCode = weatherCode;
            this.time = time;
            this.windSpeed = windSpeed;
        }

        public double getTemperature() {
            return temperature;
        }

        public double getMinTemperature() {
            return minTemperature;
        }

        public String getConditions() {
            return conditions;
        }

        public String getWeatherCode() {
            return weatherCode;
        }

        public String getTime() {
            return time;
        }

        public double getWindSpeed() {
            return windSpeed;
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

        private List<WeatherData> dataList;

        public WeatherAdapter(List<WeatherData> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item, parent, false);
            return new WeatherViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
            WeatherData data = dataList.get(position);
            holder.temperatureTextView.setText(String.format(Locale.getDefault(), "%.1f°C", data.getTemperature()));
            holder.conditionsTextView.setText(data.getConditions());
            holder.timeTextView.setText(data.getTime());

            Glide.with(holder.itemView)
                    .load(weatherIconMap.getOrDefault(data.getWeatherCode(), R.drawable.placeholder))
                    .into(holder.weatherIconImageView);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public void updateDataList(List<WeatherData> newDataList) {
            this.dataList = newDataList;
            notifyDataSetChanged();
        }

        public class WeatherViewHolder extends RecyclerView.ViewHolder {
            private TextView temperatureTextView, conditionsTextView, timeTextView;
            private ImageView weatherIconImageView;

            public WeatherViewHolder(@NonNull View itemView) {
                super(itemView);
                temperatureTextView = itemView.findViewById(R.id.temperature);
                conditionsTextView = itemView.findViewById(R.id.description);
                timeTextView = itemView.findViewById(R.id.time);
                weatherIconImageView = itemView.findViewById(R.id.weather_icon);
            }
        }
    }

}
