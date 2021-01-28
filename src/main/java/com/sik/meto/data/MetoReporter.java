package com.sik.meto.data;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class MetoReporter {

    private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final DecimalFormat DF = new DecimalFormat("###.0");
    private static final String CR = "\n";

    private String MONTH_DATA_FORMAT = "%1$30s %2$15s %3$15s %4$15s %5$15s %6$15s %7$15s";
    private String LOC_TIME_FORMAT = "%s %s";
    
    private WeatherExtremesData extremes;
    
    private FileWriter mainWriter;
    private FileWriter locationWriter;
    private FileWriter averageWriter;

    public MetoReporter() throws IOException {
        this.extremes = new WeatherExtremesData();
        this.mainWriter = new FileWriter("/Users/sik/met-office/zz-metoffice-full.txt");
        this.averageWriter = new FileWriter("/Users/sik/met-office/metoffice-averages-extremes.txt");
    }

//    public void printRecordsAndSummary(Set<MonthlyWeatherData> weatherData) throws IOException {
//        this.printRecordHeadings();
//
//        weatherData.stream()
//                .sorted()
//                .collect(Collectors.toCollection(LinkedHashSet::new))
//                .forEach(this::printRecord);
//
//        mainWriter.close();
//    }

    public void printLocations(Map<String,Set<MonthlyWeatherData>> locationData) throws IOException {
        locationData.entrySet().forEach(this::printLocation);
    }


    private void printLocation(Map.Entry<String,Set<MonthlyWeatherData>> locationData) {
        try {
            String fileName = "/Users/sik/met-office/metoffice-"
                    + locationData.getValue().stream().findFirst().get().getStationName()+ ".txt";
            locationWriter = new FileWriter(fileName);
            this.printRecordHeadings(locationWriter);
            locationData.getValue().stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .forEach(r -> this.printRecord(r,locationWriter));
            locationWriter.close();
            System.out.println("Report written to: " + fileName);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void printRecordHeadings(FileWriter writer) throws IOException {
        writeLine(String.format(MONTH_DATA_FORMAT, "Station", "Month", "Min.Temp", "Max.Temp", "FrostDays", "RainMM", "SunHours"),writer);
        writeLine(String.format(MONTH_DATA_FORMAT, "=======", "=====", "========", "========", "=========", "======", "========"),writer);
    }

    public void printRecord(MonthlyWeatherData monthData, FileWriter writer) {
        try {
            writeLine(String.format(MONTH_DATA_FORMAT, monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM),
                    monthData.getTempMinC(),
                    monthData.getTempMaxC(),
                    monthData.getAfDays(),
                    monthData.getRainfallMm(),
                    monthData.getSunHours()),writer);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        this.updateExtremes(monthData);
    }

    public void printExtremes(FileWriter writer) throws IOException {
        writeLine("", writer);
        writeLine(" Lowest Min.Temp C: " + extremes.getMinTemp() + " (" + extremes.getMinTempLocTime() + ")", writer);
        writeLine("Highest Max.Temp C: " + extremes.getMaxTemp() + " (" + extremes.getMaxTempLocTime() + ")", writer);
        writeLine("Highest AF Days   : " + extremes.getMaxAfDays() + " (" + extremes.getMaxAfDaysLocTime() + ")", writer);
        writeLine("Max. Rainfall Mm  : " + extremes.getMaxRainfallMm() + " (" + extremes.getMaxRainfallMmLocTime() + ")", writer);
        writeLine("Max. Sun Hours    : " + extremes.getMaxSunHours() + " (" + extremes.getMaxSunHoursLocTime() + ")", writer);
        writeLine("", writer);
    }

    public void printYearlyAverages(Map<Integer,YearlyAverageWeatherData> yearlyAverageWeatherDataMap) throws IOException {
        writeLine("", averageWriter);
        writeLine(String.format(MONTH_DATA_FORMAT, "", "Year", "Min.Temp", "Max.Temp", "FrostDays", "RainMM", "SunHours"), averageWriter);
        writeLine(String.format(MONTH_DATA_FORMAT, "", "====", "========", "========", "=========", "======", "========"), averageWriter);

        for (Integer year: yearlyAverageWeatherDataMap.keySet()) {
            YearlyAverageWeatherData yearData = yearlyAverageWeatherDataMap.get(year);
            writeLine(String.format(MONTH_DATA_FORMAT, "",
                    yearData.getYearStartDate().getYear(),
                    DF.format(yearData.getAvgTempMinC()),
                    DF.format(yearData.getAvgTempMaxC()),
                    DF.format(yearData.getAvgAfDays()),
                    DF.format(yearData.getAvgRainfallMm()),
                    DF.format(yearData.getAvgSunHours())), averageWriter);
        }

        printExtremes(averageWriter);
        averageWriter.close();
    }

    private void updateExtremes(MonthlyWeatherData monthData) {
        if (monthData.getTempMinC() != null && monthData.getTempMinC() < this.extremes.getMinTemp()) {
            this.extremes.setMinTemp(monthData.getTempMinC());
            this.extremes.setMinTempLocTime(String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM)));
        }
        if (monthData.getTempMaxC() != null &&monthData.getTempMaxC() > this.extremes.getMaxTemp()) {
            this.extremes.setMaxTemp(monthData.getTempMaxC());
            this.extremes.setMaxTempLocTime(String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM)));
        }
        if (monthData.getAfDays() != null && monthData.getAfDays() > this.extremes.getMaxAfDays()) {
            this.extremes.setMaxAfDays(monthData.getAfDays());
            this.extremes.setMaxAfDaysLocTime(String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM)));
        }
        if (monthData.getRainfallMm() != null && monthData.getRainfallMm() > this.extremes.getMaxRainfallMm()) {
            this.extremes.setMaxRainfallMm(monthData.getRainfallMm());
            this.extremes.setMaxRainfallMmLocTime(String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM)));
        }
        if (monthData.getSunHours() != null && monthData.getSunHours() > this.extremes.getMaxSunHours()) {
            this.extremes.setMaxSunHours(monthData.getSunHours());
            this.extremes.setMaxSunHoursLocTime(String.format(LOC_TIME_FORMAT,
                    monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM)));
        }
    }
    
    private void writeLine(String line, FileWriter writer) throws IOException {
        writer.write(line + CR);
    }

    private void writeAverageLine(String averageLine, FileWriter writer) throws IOException {
        writer.write(averageLine + CR);
    }
}
