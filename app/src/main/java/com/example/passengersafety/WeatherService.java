package com.example.passengersafety;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {

    @GET("data/2.5/weather?")
    Call<WeatherResponse> getCurrentWeatherData(@Query("zip") String zip,  @Query("appid") String app_id);

    //Call<WeatherResponse> getCurrentWeatherData(@Query("lat") String lat, @Query("lon") String lon, @Query("appid") String app_id);

}
