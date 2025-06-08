package com.example.weatherapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;

import com.example.weatherapp.Adapters.HourlyAdapter;
import com.example.weatherapp.Domains.Hourly;
import com.example.weatherapp.Domains.ResponseModel;
import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private static final String TAG = "WeatherActivity";
    private static final String API_KEY = WEATHER_API_KEY; // Your OpenWeatherMap API key
    private String city;
    ActivityMainBinding binding;
    ArrayList<Hourly> items;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.loadingSpinner.bringToFront();

        if (isLocationPermissionGranted() && locationManager == null) {
            getLocation();  // Restart location fetching if not already started
        }
        else {
            showPermissionSettingsDialog();
        }
        //getLocation();

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FutureActivity.class);
                intent.putExtra("city", city);
                startActivity(intent);
            }
        });

        binding.refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
                searchCity();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MainActivity.this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchCity() {
        //binding.search.setQuery(city, false);

        processData(city);
        processTodayApiData(city);
        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                binding.progressBar.setVisibility(View.VISIBLE);
                city = s;
                processData(city);
                processTodayApiData(city);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            //city = addresses.get(0).getSubAdminArea().split(" ")[0];

            String subAdmin = addresses.get(0).getSubAdminArea();
            if (subAdmin != null && !subAdmin.isEmpty()) {
                city = subAdmin.split(" ")[0];
            } else {
                city= addresses.get(0).getLocality();
            }

            searchCity();

            binding.search.setQuery(city, false);
            binding.loadingSpinner.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();  // Retry location
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            showPermissionSettingsDialog();
        }
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Location permission is required to fetch weather updates. Please enable it in app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (isLocationPermissionGranted() && locationManager == null) {
            getLocation();  // Restart location fetching if not already started
        }
    }

    private void processTodayApiData(String city) {
        Call<ResponseModel> call = ApiController
                .getInstance()
                .getApi()
                .getFiveDayWeatherForecast(city, API_KEY, "metric");

        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, retrofit2.Response<ResponseModel> response) {
                if (response.body() != null && response.body().getList() != null) {
                    binding.loadingSpinner.setVisibility(View.GONE);
                    binding.progressBar.setVisibility(View.GONE);

                    List<ResponseModel.WeatherItem> forecastList = response.body().getList();
                    items = new ArrayList<>();

                    long currentTime = System.currentTimeMillis() / 1000;

                    if (forecastList != null) {
                        for (int i = 0; i < 8 && i < forecastList.size(); i++) {
                            ResponseModel.WeatherItem data = forecastList.get(i);
                            //long forecastTime = Long.parseLong(data.getDt_txt());
                            String forecastTime = convertTo12HourFormat(data.getDt_txt());
                            double temp = data.getMain().getTemp();
                            String weather = data.getWeather().get(0).getMain();

                            items.add(new Hourly(forecastTime, temp, changeWeatherImg(weather)));

                        /*if (forecastTime >= currentTime) {
                            // Process only upcoming data points
                            double temp = data.getMain().getTemp(); // Temperature
                            String weather = data.getWeather().get(0).getMain(); // Weather Condition
                            double windSpeed = data.getWind().getSpeed(); // Wind Speed

                            String formattedTime = convertTo12HourFormat(data.getDt_txt());

                            Log.d("WeatherApp", "Time: " + formattedTime + ", Temp: " + temp + "°C, Weather: " + weather + ", Wind: " + windSpeed + " m/s");

                        }*/
                        }
                        iniRecyclerView(items);
                    } else {
                        Toast.makeText(getApplicationContext(),"Enter valid city name!",Toast.LENGTH_LONG).show();
                        // Handle the error response here
                        //Log.e("Weather", "Request failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                binding.loadingSpinner.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Failed to fetch data: " + t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processData(String city) {
        Call<ResponseModel.WeatherItem> call = ApiController
                .getInstance()
                .getPresentApi()
                .getPresentWeatherData(city, API_KEY, "metric");

        call.enqueue(new Callback<ResponseModel.WeatherItem>() {
            @Override
            public void onResponse(Call<ResponseModel.WeatherItem> call, Response<ResponseModel.WeatherItem> response) {
                ResponseModel.WeatherItem data = response.body();
                binding.loadingSpinner.setVisibility(View.GONE);
                if (data != null) {
                    double temperature = data.getMain().getTemp();
                    binding.temp.setText(String.format("%d", (int) temperature) + "°C");

                    double min_temp = data.getMain().getTemp_min();
                    double max_temp = data.getMain().getTemp_max();
                    binding.minMaxTemp.setText(String.format("H:%d°C L:%d°C", (int) max_temp, (int) min_temp));

                    int pressure = data.getMain().getPressure();
                    binding.pressure.setText(pressure + " mbar");

                    int humidity = data.getMain().getHumidity();
                    binding.humidity.setText(humidity + "%");

                    binding.windSpeed.setText(data.getWind().getSpeed() + " m/s");

                    String weather = data.getWeather().get(0).getMain();
                    binding.whether.setText(weather);

                    binding.date.setText(day(System.currentTimeMillis()) + ", " + date());

                    changeBG(weather);
                } else {
                    // Handle the error response here
                    binding.search.setQuery(city, false);
                    Log.e("Weather", "Request failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseModel.WeatherItem> call, Throwable t) {
                Log.d("Weather", t.toString());
            }
        });
    }

    private String date() {
        Calendar calendar = Calendar.getInstance();  // Initialize Calendar
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d | hh:mm a", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private String day(Long timestamp) {
        Calendar calendar = Calendar.getInstance();  // Initialize Calendar
        if (timestamp != null) {
            calendar.setTimeInMillis(timestamp * 1000);  // Convert seconds to milliseconds
        }
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(calendar.getTime());
    }

    private String convertTo12HourFormat(String dateTime) {
        try {
            // OpenWeatherMap API gives time in this format: "yyyy-MM-dd HH:mm:ss"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            //SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh a", Locale.getDefault());

            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);  // Convert to 12-hour format (e.g., "03:00 PM")
        } catch (Exception e) {
            e.printStackTrace();
            return dateTime;  // Return original if there's an error
        }
    }

    private void changeBG(String weather) {
        ImageView img = findViewById(R.id.weatherimg);
        switch (weather.toLowerCase()) {
            case "clear sky":
            case "sunny":
            case "clear":
                img.setImageResource(R.drawable.sunny);
                break;

            case "few clouds":
            case "scattered clouds":
            case "broken clouds":
            case "mist":
            case "haze":
            case "foggy":
            case "overcast clouds":
            case "moderate rain":
            case "overcast":
            case "clouds":
                img.setImageResource(R.drawable.cloudy);
                break;

            case "rain":
            case "heavy rain":
            case "Light rain":
                img.setImageResource(R.drawable.rainy);
                break;

            case "snow":
                img.setImageResource(R.drawable.snowy);
                break;

            default:
                img.setImageResource(R.drawable.sunny);

        }
    }

    private String changeWeatherImg(String weather) {
        String img = "sunny";
        switch (weather.toLowerCase()) {
            case "clear sky":
            case "sunny":
            case "clear":
                img = "sunny";
                break;

            case "few clouds":
            case "scattered clouds":
            case "broken clouds":
            case "mist":
            case "haze":
            case "foggy":
            case "overcast clouds":
            case "moderate rain":
            case "overcast":
            case "clouds":
                img = "cloudy";
                break;

            case "rain":
            case "heavy rain":
            case "Light rain":
                img = "rainy";
                break;

            case "snow":
                img = "snowy";
                break;
        }
        return img;
    }

    private void iniRecyclerView(ArrayList<Hourly> items) {
        for (Hourly item : items) {
            Log.d("abc", "Time: " + item.getHour() + ", Temp: " + item.getTemp());
        }
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter = new HourlyAdapter(items);
        recyclerView.setAdapter(adapter);
    }


    /*private void processPresentApiData(String city) {
        String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the response from the weather API
                        try {
                            JSONObject main = response.getJSONObject("main");
                            Log.d("abc", "Response: " + response.toString());

                            binding.city.setText(city);

                            double temperature = main.getDouble("temp");
                            // Convert temperature from Kelvin to Celsius

                            temperature = temperature - 273.15;
                            binding.temp.setText(String.format("%d",(int)temperature)+"°C");

                            double min_temp= main.getDouble("temp_min");
                            min_temp= min_temp- 273.15;
                            double max_temp= main.getDouble("temp_max");
                            max_temp= max_temp- 273.15;
                            binding.minMaxTemp.setText(String.format("H:%d°C L:%d°C", (int)max_temp, (int)min_temp));

                            int pressure= main.getInt("pressure");
                            binding.pressure.setText(pressure+"mBar");

                            int humidity= main.getInt("humidity");
                            binding.humidity.setText(humidity+"%");

                            binding.windSpeed.setText(response.getJSONObject("wind").getString("speed") + " m/s");

                            JSONArray weatherArray = response.getJSONArray("weather");
                            String weather = weatherArray.getJSONObject(0).getString("main");
                            binding.whether.setText(weather);

                            binding.date.setText(day(System.currentTimeMillis())+ " ," + date());

                            changeBG(weather);

                            //weatherTextView.setText("Current Temperature: " + String.format("%.2f", temperature) + "°C");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Log.e(TAG, "Error: " + error.getMessage());
                    }
                });

        // Add the request to the request queue
        requestQueue.add(jsonObjectRequest);
    }*/
}