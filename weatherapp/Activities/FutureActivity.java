package com.example.weatherapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.weatherapp.Adapters.FutureAdapter;
import com.example.weatherapp.Domains.Future;
import com.example.weatherapp.Domains.ResponseModel;
import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityFutureBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FutureActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private static final String API_KEY = WEATHER_API_KEY;
    ActivityFutureBinding binding;
    ArrayList<Future> items;
    String city= "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityFutureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.scrollView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        Intent intent= getIntent();
        city= intent.getStringExtra("city");

        binding.citytxt.setText(city);
        loadWeatherData(intent.getStringExtra("city"));

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void loadWeatherData(String city) {
        Call<ResponseModel> call = ApiController
                .getInstance()
                .getApi()
                .getFiveDayWeatherForecast(city, API_KEY, "metric");

        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.body() != null && response.body().getList() != null) {
                    List<ResponseModel.WeatherItem> forecastList = response.body().getList();

                    binding.scrollView.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    if (forecastList != null && !forecastList.isEmpty()) {
                        // Set today's data (first item)
                        ResponseModel.WeatherItem today = forecastList.get(0);

                        double temp = today.getMain().getTemp();
                        binding.tempTxt.setText(String.format("%d", (int) temp) + "°C");

                        String weather = today.getWeather().get(0).getMain();
                        binding.weatherTxt.setText(weather);

                        binding.pressure.setText(today.getMain().getPressure() + "mBar");
                        binding.humidity.setText(today.getMain().getHumidity() + "%");
                        binding.windSpeed.setText(today.getWind().getSpeed() + " m/s");

                        changeBG(weather);

                        // Build future forecast
                        items = new ArrayList<>();
                        HashMap<String, String> dailyForecast = new HashMap<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE \nMMM d", Locale.getDefault());

                        for (ResponseModel.WeatherItem item : forecastList) {
                            long timestamp = item.getDt();
                            Date date = new Date(timestamp * 1000);
                            String dayWithDate = sdf.format(date);

                            if (!dailyForecast.containsKey(dayWithDate)) {
                                dailyForecast.put(dayWithDate, "");

                                items.add(new Future(
                                        dayWithDate,
                                        changePic(item.getWeather().get(0).getMain()),
                                        item.getWeather().get(0).getMain(),
                                        (int) item.getMain().getTemp_max(),
                                        (int) item.getMain().getTemp_min()
                                ));
                            }
                        }

                        initRecyclerView(items);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.e("Weather", "API call failed: " + t.getMessage());
            }
        });
    }

    private void initRecyclerView(ArrayList<Future> items) {

        recyclerView= findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter= new FutureAdapter(items);
        recyclerView.setAdapter(adapter);

    }

    private void changeBG(String weather){
        ConstraintLayout layoutBG= findViewById(R.id.background);
        ImageView img= findViewById(R.id.weatherImg);
        switch (weather.toLowerCase()){
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
    private String changePic(String weather){
        String img= "";
        switch (weather.toLowerCase()){
            case "clear sky":
            case "sunny":
            case "clear":
                img= "sunny";
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
                img= "cloudy";
                break;

            case "rain":
            case "heavy rain":
            case "Light rain":
                img= "rainy";
                break;

            case "snow":
                img= "snowy";
                break;

            default:
                img= "sunny";

        }
        return img;
    }

}