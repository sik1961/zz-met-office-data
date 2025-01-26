package com.sik.meto.data.scheduler;

import com.sik.meto.data.model.MonthlyWeatherData;
import com.sik.meto.data.service.MetoDataHandler;
import com.sik.meto.data.service.MetoDataUtilities;
import com.sik.meto.data.service.MetoExcelWriter;
import com.sik.meto.data.service.MetoFTPHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MetoDataScheduler {

    private Set<MonthlyWeatherData> weatherData;

    @Scheduled(cron = "0 0/1 * * * *")
    public void doStuff() throws IOException {
        MetoDataHandler manager = new MetoDataHandler();
        MetoDataUtilities utilities = new MetoDataUtilities();
        MetoExcelWriter excelWriter = new MetoExcelWriter();
        MetoFTPHandler ftpHandler = new MetoFTPHandler();

        this.weatherData = manager.getMonthlyData();

        Map<String,Set<MonthlyWeatherData>> dataByLocation = weatherData.stream()
                .sorted()
                .collect(Collectors.groupingBy(MonthlyWeatherData::getStationName,Collectors.toSet()));

        LinkedHashMap<String, Set<MonthlyWeatherData>> sortedMap = new LinkedHashMap<>();

        dataByLocation.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

        excelWriter.writeHistoricWorkbook(sortedMap);

        excelWriter.writeAveragesWorkbook(sortedMap);

        excelWriter.writeExtremesWorkbook(utilities.buildExtremesMap(weatherData));

        ftpHandler.uploadFiles();
    }


}
