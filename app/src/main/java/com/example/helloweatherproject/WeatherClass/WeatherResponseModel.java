package com.example.helloweatherproject.WeatherClass;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponseModel {

    //    @SerializedName("basic")
    public WeatherBasic basic;

    //    @SerializedName("update")
    public WeatherUpdate update;

    //    @SerializedName("now")
    public WeatherNow now;

    //    @SerializedName("status")
    public String status;

    public List<WeatherLifestyle> lifestyle;


    public List<WeatherDailyForecast> daily_forecast;

    public WeatherAqi air_now_city;

}
