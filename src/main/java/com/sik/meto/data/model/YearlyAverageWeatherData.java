package com.sik.meto.data.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class YearlyAverageWeatherData  implements Comparable<YearlyAverageWeatherData> {

    protected static final String LOC = "Location";
    protected static final String LAT = "Lat";
    protected static final String LON = "Lon";

    private LocalDate yearStartDate;
    private Float totTempMaxC;
    private int countTempMaxC;
    private Float totTempMinC;
    private int countTempMinC;
    private Float totTempMedC;
    private int countTempMedC;
    private Float totAfDays;
    private int countAfDays;
    private Float totRainfallMm;
    private int countRainfallMm;
    private Float totSunHours;
    private int countSunHours;

    public Float getAvgTempMaxC() {
        return this.totTempMaxC / this.countTempMaxC;
    }

    public Float getAvgTempMinC() {
        return this.totTempMinC / this.countTempMinC;
    }

    public Float getAvgTempMedC() {
        return this.totTempMedC / this.countTempMedC;
    }

    public Float getAvgAfDays() {
        return this.totAfDays / this.countAfDays;
    }

    public Float getAvgRainfallMm() {
        return this.totRainfallMm / this.countRainfallMm;
    }

    public Float getAvgSunHours() {
        return this.totSunHours / this.countSunHours;
    }

    @Override
    public int compareTo(YearlyAverageWeatherData that) {
        if (this.yearStartDate.isBefore(that.yearStartDate)) {
            return -1;
        } else if (this.yearStartDate.isAfter(that.yearStartDate)) {
            return 1;
        } else {
            return 0;
        }
    }
}
