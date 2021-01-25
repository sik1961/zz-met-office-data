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
    private float minTemp = 100.0f;
    private String minTempLocTime = "";
    private float maxTemp = -100.0f;
    private String maxTempLocTime = "";
    private int maxAfDays = 0;
    private String maxAfDaysLocTime = "";
    private float maxSunHours = 0.0f;
    private String maxSunHoursLocTime = "";
    private float maxRainfallMm = 0;
    private String maxRainfallMmLocTime = "";
    
    private FileWriter mainWriter;
    private FileWriter locationWriter;
    private FileWriter averageWriter;

    public MetoReporter() throws IOException {
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
            this.printRecordHeadings();
            locationData.getValue().stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .forEach(this::printRecord);
            locationWriter.close();
            System.out.println("Report written to: " + fileName);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void printRecordHeadings() throws IOException {
        writeLine(String.format(MONTH_DATA_FORMAT, "Station", "Month", "Min.Temp", "Max.Temp", "FrostDays", "RainMM", "SunHours"));
        writeLine(String.format(MONTH_DATA_FORMAT, "=======", "=====", "========", "========", "=========", "======", "========"));
    }

    public void printRecord(MonthlyWeatherData monthData) {
        try {
            writeLine(String.format(MONTH_DATA_FORMAT, monthData.getStationName(),
                    monthData.getMonthStartDate().format(YYYY_MM),
                    monthData.getTempMinC(),
                    monthData.getTempMaxC(),
                    monthData.getAfDays(),
                    monthData.getRainfallMm(),
                    monthData.getSunHours()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        this.updateExtremes(monthData);
    }

    public void printExtremes() throws IOException {
        writeAverageLine("");
        writeAverageLine(" Lowest Min.Temp C: " + this.minTemp + " (" + this.minTempLocTime + ")");
        writeAverageLine("Highest Max.Temp C: " + this.maxTemp + " (" + this.maxTempLocTime + ")");
        writeAverageLine("Highest AF Days   : " + this.maxAfDays + " (" + this.maxAfDaysLocTime + ")");
        writeAverageLine("Max. Rainfall Mm  : " + this.maxRainfallMm + " (" + this.maxRainfallMmLocTime + ")");
        writeAverageLine("Max. Sun Hours    : " + this.maxSunHours + " (" + this.maxSunHoursLocTime + ")");
        writeAverageLine("");
    }

    public void printYearlyAverages(Map<Integer,YearlyAverageWeatherData> yearlyAverageWeatherDataMap) throws IOException {
        writeAverageLine("");
        writeAverageLine(String.format(MONTH_DATA_FORMAT, "", "Year", "Min.Temp", "Max.Temp", "FrostDays", "RainMM", "SunHours"));
        writeAverageLine(String.format(MONTH_DATA_FORMAT, "", "====", "========", "========", "=========", "======", "========"));

        for (Integer year: yearlyAverageWeatherDataMap.keySet()) {
            YearlyAverageWeatherData yearData = yearlyAverageWeatherDataMap.get(year);
            writeAverageLine(String.format(MONTH_DATA_FORMAT, "",
                    yearData.getYearStartDate().getYear(),
                    DF.format(yearData.getAvgTempMinC()),
                    DF.format(yearData.getAvgTempMaxC()),
                    DF.format(yearData.getAvgAfDays()),
                    DF.format(yearData.getAvgRainfallMm()),
                    DF.format(yearData.getAvgSunHours())));
        }

        printExtremes();
        averageWriter.close();
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
        if (monthData.getRainfallMm() != null && monthData.getRainfallMm() > this.maxRainfallMm) {
            this.maxRainfallMm = monthData.getRainfallMm();
            this.maxRainfallMmLocTime = String.format(LOC_TIME_FORMAT,
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
    
    private void writeLine(String line) throws IOException {
        locationWriter.write(line + CR);
    }

    private void writeAverageLine(String averageLine) throws IOException {
        averageWriter.write(averageLine + CR);
    }
}
