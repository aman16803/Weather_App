package com.example.weatherapp.Interfaces;

import com.example.weatherapp.Domains.ResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("data/2.5/forecast")
    Call<ResponseModel> getFiveDayWeatherForecast(
            @Query("q") String city,        // City name
            @Query("appid") String apiKey,// API key
            @Query("units") String units
            //@Query("units") String units    // Units (metric or imperial)
    );

}
