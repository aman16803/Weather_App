package com.example.weatherapp.Activities;

import com.example.weatherapp.Interfaces.PresentWeatherApiService;
import com.example.weatherapp.Interfaces.WeatherApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiController {
    private static final String url="http://api.openweathermap.org/";
    private static ApiController clientObject;
    private static Retrofit retrofit;

    ApiController(){
        retrofit= new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized ApiController getInstance(){
        if(clientObject==null)
            clientObject=new ApiController();
        return clientObject;
    }

    WeatherApiService getApi(){
        return retrofit.create(WeatherApiService.class);
    }

    PresentWeatherApiService getPresentApi(){
        return retrofit.create(PresentWeatherApiService.class);
    }
}
