package com.example.passengersafety;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {

    @SerializedName("main")
    public Main main;
}

class Main{
    @SerializedName("temp")
    public float temp;
}


