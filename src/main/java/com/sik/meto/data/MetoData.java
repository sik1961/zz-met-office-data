package com.sik.meto.data;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MetoData {



    public static void main(String[] args) {
        MetoDataManager manager = new MetoDataManager();
        MetoReporter reporter = new MetoReporter();

        Set<MonthlyWeatherData> weatherData = manager.getMonthlyData();

        Set<MonthlyWeatherData> locationSpecificData = manager.filterByStation(weatherData, "Paisley");

        reporter.printRecordsAndSummary(weatherData);
//        sheffData.stream()
//                .sorted()
//                .collect(Collectors.toCollection(LinkedHashSet::new))
//                .forEach(System.out::println);





    }


}
