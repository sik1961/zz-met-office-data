package com.sik.meto.data;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetoData {



    public static void main(String[] args) throws IOException {
        MetoDataHandler manager = new MetoDataHandler();
        //MetoReporter reporter = new MetoReporter();
        MetoExcelWriter excelWriter = new MetoExcelWriter();

        Set<MonthlyWeatherData> weatherData = manager.getMonthlyData();

        Set<MonthlyWeatherData> locationSpecificData = manager.filterByStation(weatherData, "Paisley");

        Map<String,Set<MonthlyWeatherData>> dataByLocation = weatherData.stream()
                .sorted()
                .collect(Collectors.groupingBy(MonthlyWeatherData::getStationName,Collectors.toSet()));

        LinkedHashMap<String, Set<MonthlyWeatherData>> sortedMap = new LinkedHashMap<>();

        dataByLocation.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

        //reporter.printLocations(dataByLocation);

        //reporter.printRecordsAndSummary(weatherData);
        //reporter.printYearlyAverages(manager.getYearlyAverageWeatherDataMap());

        excelWriter.writeHistoricWorkbook(sortedMap);
        excelWriter.writeSummaryWorkbook(manager.getYearlyAverageWeatherDataMap());

//        sheffData.stream()
//                .sorted()
//                .collect(Collectors.toCollection(LinkedHashSet::new))
//                .forEach(System.out::println);

    }


}
