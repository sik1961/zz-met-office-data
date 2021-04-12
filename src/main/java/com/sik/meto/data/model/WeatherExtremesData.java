package com.sik.meto.data.model;

import lombok.Data;

@Data
public class WeatherExtremesData {
    private float minTemp;
    private String minTempLocTime;
    private float maxTemp;
    private String maxTempLocTime;
    private int maxAfDays;
    private String maxAfDaysLocTime;
    private float maxSunHours;
    private String maxSunHoursLocTime;
    private float maxRainfallMm;
    private String maxRainfallMmLocTime;

    public WeatherExtremesData() {
        this.minTemp  = 100.0f;
        this.minTempLocTime = "";
        this.maxTemp = -100.0f;
        this.maxTempLocTime = "";
        this.maxAfDays = 0;
        this.maxAfDaysLocTime = "";
        this.maxSunHours = 0.0f;
        this.maxSunHoursLocTime = "";
        this.maxRainfallMm = 0;
        this.maxRainfallMmLocTime = "";
    }
}
