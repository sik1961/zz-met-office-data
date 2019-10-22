package com.sik.meto.data;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class MetoReporter {

    private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");

    private String MONTH_DATA_FORMAT = "%1$30s %2$15s %3$15s %4$15s %5$15s %6$15s";
    private String LOC_TIME_FORMAT = "%s %s";
    private float minTemp = 100.0f;
    private String minTempLocTime = "";
    private float maxTemp = -100.0f;
    private String maxTempLocTime = "";
    private int maxAfDays = 0;
    private String maxAfDaysLocTime = "";
    private float maxSunHours = 0.0f;
    private String maxSunHoursLocTime = "";

    public void printRecordsAndSummary(Set<MonthlyWeatherData> weatherData) {
        this.printRecordHeadings();

        weatherData.stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .forEach(this::printRecord);
        this.printExtremes();
    }

    public void printRecordHeadings() {
        System.out.println(String.format(MONTH_DATA_FORMAT, "Station", "Month", "Min.Temp", "Max.Temp", "FrostDays", "SunHours"));
        System.out.println(String.format(MONTH_DATA_FORMAT,"=======", "=====", "========", "========", "=========", "========"));
    }

    public void printRecord(MonthlyWeatherData monthData) {
        System.out.println(String.format(MONTH_DATA_FORMAT,monthData.getStationName(),
                monthData.getMonthStartDate().format(YYYY_MM),
                monthData.getTempMinC(),
                monthData.getTempMaxC(),
                monthData.getAfDays(),
                monthData.getSunHours()));

        this.updateExtremes(monthData);
    }

    public void printExtremes() {
        System.out.println("");
        System.out.println("Min.Temp C   : " + this.minTemp + " (" + this.minTempLocTime + ")");
        System.out.println("Max.Temp C   : " + this.maxTemp + " (" + this.maxTempLocTime + ")");
        System.out.println("Max AF Days C: " + this.maxAfDays + " (" + this.maxAfDaysLocTime + ")");
        System.out.println("Max.Sun Hours: " + this.maxSunHours + " (" + this.maxSunHoursLocTime + ")");
        System.out.println("");
    }

    private void updateExtremes(MonthlyWeatherData monthData) {
        if (monthData.getTempMinC() != null && monthData.getTempMinC() < this.minTemp) {
            this.minTemp = monthData.getTempMinC();
            this.minTempLocTime = String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM));
        }
        if (monthData.getTempMaxC() != null &&monthData.getTempMaxC() > this.maxTemp) {
            this.maxTemp = monthData.getTempMaxC();
            this.maxTempLocTime = String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM));
        }
        if (monthData.getAfDays() != null && monthData.getAfDays() > this.maxAfDays) {
            this.maxAfDays = monthData.getAfDays();
            this.maxAfDaysLocTime = String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM));
        }
        if (monthData.getSunHours() != null && monthData.getSunHours() > this.maxSunHours) {
            this.maxSunHours = monthData.getSunHours();
            this.maxSunHoursLocTime = String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM));
        }
    }

}
