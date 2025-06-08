package com.example.weatherapp.Interfaces;

import com.example.weatherapp.Domains.ResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PresentWeatherApiService {
    @GET ("data/2.5/weather")
    Call<ResponseModel.WeatherItem> getPresentWeatherData(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

}
