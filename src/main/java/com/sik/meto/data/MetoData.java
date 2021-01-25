package com.sik.meto.data;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetoData {



    public static void main(String[] args) throws IOException {
        MetoDataManager manager = new MetoDataManager();
        MetoReporter reporter = new MetoReporter();

        Set<MonthlyWeatherData> weatherData = manager.getMonthlyData();

        Set<MonthlyWeatherData> locationSpecificData = manager.filterByStation(weatherData, "Paisley");

        Map<String,Set<MonthlyWeatherData>> dataByLocation = weatherData.stream()
                .collect(Collectors.groupingBy(MonthlyWeatherData::getStationName,Collectors.toSet()));

        reporter.printLocations(dataByLocation);

        //reporter.printRecordsAndSummary(weatherData);
        reporter.printYearlyAverages(manager.getYearlyAverageWeatherDataMap());





//        sheffData.stream()
//                .sorted()
//                .collect(Collectors.toCollection(LinkedHashSet::new))
//                .forEach(System.out::println);





    }


}
