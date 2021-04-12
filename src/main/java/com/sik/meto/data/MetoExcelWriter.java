package com.sik.meto.data;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MetoExcelWriter {

    private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final DecimalFormat FDF = new DecimalFormat("##0.0");
    private static final DecimalFormat IDF = new DecimalFormat("##0");
    private static final String CR = "\n";

    MetoDataUtilities utility = new MetoDataUtilities();

    private String historicFileName;
    private FileOutputStream historicOut;
    private String summaryFileName;
    private FileOutputStream summaryOut;
    private String extremesFileName;
    private FileOutputStream extremesOut;

    public MetoExcelWriter() throws IOException {
        this.historicFileName = "/Users/sik/met-office/MetOfficeHistoricData.xlsx";
        this.historicOut = new FileOutputStream(this.historicFileName);
        this.summaryFileName = "/Users/sik/met-office/MetOfficeYearlyAverages.xlsx";
        this.summaryOut = new FileOutputStream(this.summaryFileName);
        this.extremesFileName = "/Users/sik/met-office/MetOfficeExtremes.xlsx";
        this.extremesOut = new FileOutputStream(this.extremesFileName);
    }

    public void writeHistoricWorkbook(Map<String, Set<MonthlyWeatherData>> locationData) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();

        for(String location: locationData.keySet()) {
            HSSFSheet sheet = workbook.createSheet(location);

            HSSFRow rowhead = sheet.createRow((short) 0);
            rowhead.createCell(0).setCellValue("Station");
            rowhead.createCell(1).setCellValue("Month");
            rowhead.createCell(2).setCellValue("Min.Temp");
            rowhead.createCell(3).setCellValue("Max.Temp");
            rowhead.createCell(4).setCellValue("FrostDays");
            rowhead.createCell(5).setCellValue("RainMM");
            rowhead.createCell(6).setCellValue("SunHours");

            AtomicInteger rowCount = new AtomicInteger();
            locationData.get(location).stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .forEach(r -> this.writeHistoricRow(r,sheet, rowCount.incrementAndGet()));
        }

        workbook.write(historicOut);
        historicOut.close();
        workbook.close();
        System.out.println(historicFileName + " Excel file has been generated successfully.");
    }

    public void writeAveragesWorkbook(Map<String, Set<MonthlyWeatherData>> locationData) throws IOException {
        Set<MonthlyWeatherData> everything = new HashSet<>();
        locationData.entrySet().forEach(m -> everything.addAll(m.getValue()));

        HSSFWorkbook workbook = new HSSFWorkbook();

        this.createAveragesWorksheet("All", workbook, utility.buildYearlyAvarageWeatherDataMap(everything));

        for (String location: locationData.keySet()) {
            this.createAveragesWorksheet(location, workbook, utility.buildYearlyAvarageWeatherDataMap(locationData.get(location)));
        }

        workbook.write(summaryOut);
        summaryOut.close();
        workbook.close();
        System.out.println(summaryFileName + " Excel file has been generated successfully.");
    }

    public void writeExtremesWorkbook(Map<String, WeatherExtremesData> extremesData) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet("Extremes");

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.createCell(0).setCellValue("Name");
        rowhead.createCell(1).setCellValue("Loc/Time");
        rowhead.createCell(2).setCellValue("Extreme");
        rowhead.createCell(3).setCellValue("Value");

        int rowNumber = 1;
        for(String location: extremesData.keySet()) {
            rowNumber = this.writeExtremesRows(sheet,location, extremesData.get(location),rowNumber);
        }

        workbook.write(extremesOut);
        extremesOut.close();
        workbook.close();
        System.out.println(extremesFileName + " Excel file has been generated successfully.");
    }

    private int writeExtremesRows(HSSFSheet sheet, String location, WeatherExtremesData weatherExtremesData, int row) {
        HSSFRow minTemp = sheet.createRow((short) row);
        createCell(minTemp,0,location);
        createCell(minTemp,1,weatherExtremesData.getMinTempLocTime());
        createCell(minTemp,2,"Min.Temp");
        createCell(minTemp,3,weatherExtremesData.getMinTemp());
        row++;
        HSSFRow maxTemp = sheet.createRow((short) row);
        createCell(maxTemp,0,location);
        createCell(maxTemp,1,weatherExtremesData.getMaxTempLocTime());
        createCell(maxTemp,2,"Max.Temp");
        createCell(maxTemp,3,weatherExtremesData.getMaxTemp());
        row++;
        HSSFRow maxRain = sheet.createRow((short) row);
        createCell(maxRain,0,location);
        createCell(maxRain,1,weatherExtremesData.getMaxRainfallMmLocTime());
        createCell(maxRain,2,"Max.RainfallMM");
        createCell(maxRain,3,weatherExtremesData.getMaxRainfallMm());
        row++;
        HSSFRow maxAFD = sheet.createRow((short) row);
        createCell(maxAFD,0,location);
        createCell(maxAFD,1,weatherExtremesData.getMaxAfDaysLocTime());
        createCell(maxAFD,2,"Max.AFDays");
        createCell(maxAFD,3,weatherExtremesData.getMaxAfDays());
        row++;
        HSSFRow maxSun = sheet.createRow((short) row);
        createCell(maxSun,0,location);
        createCell(maxSun,1,weatherExtremesData.getMaxSunHoursLocTime());
        createCell(maxSun,2,"Max.SunHours");
        createCell(maxSun,3,weatherExtremesData.getMaxSunHours());
        row++;
        row++;
        return row;
    }

    private void createAveragesWorksheet(String name, HSSFWorkbook workbook, Map<Integer, YearlyAverageWeatherData> averageData) {

        HSSFSheet sheet = workbook.createSheet(name);

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.createCell(0).setCellValue("Location");
        rowhead.createCell(1).setCellValue("Year");
        rowhead.createCell(2).setCellValue("Min.Temp");
        rowhead.createCell(3).setCellValue("Max.Temp");
        rowhead.createCell(4).setCellValue("FrostDays");
        rowhead.createCell(5).setCellValue("RainMM");
        rowhead.createCell(6).setCellValue("SunHours");

        int rowCount = 1;
        for (Integer year: averageData.keySet()) {
            this.writeAveragesRow(averageData.get(year), sheet, rowCount++, name);
        }
    }

    private void writeHistoricRow(MonthlyWeatherData monthData, HSSFSheet sheet, int rowNumber) {
        HSSFRow row = sheet.createRow((short) rowNumber);
        createCell(row,0,monthData.getStationName());
        createCell(row,1,monthData.getMonthStartDate().format(YYYY_MM));
        createCell(row,2,monthData.getTempMinC());
        createCell(row,3,monthData.getTempMaxC());
        createCell(row,4,monthData.getAfDays());
        createCell(row,5,monthData.getRainfallMm());
        createCell(row,6,monthData.getSunHours());
    }

    private void writeAveragesRow(YearlyAverageWeatherData averageData, HSSFSheet sheet, int rowNumber, String location) {
        HSSFRow row = sheet.createRow((short) rowNumber);
        createCell(row,0,location);
        createCell(row,1,averageData.getYearStartDate().getYear());
        createCell(row,2,averageData.getAvgTempMinC());
        createCell(row,3,averageData.getAvgTempMaxC());
        createCell(row,4,averageData.getAvgAfDays());
        createCell(row,5,averageData.getAvgRainfallMm());
        createCell(row,6,averageData.getAvgSunHours());
    }

    private void createCell(HSSFRow row, int column, String value) {
        if (value!=null) {
            row.createCell(column).setCellValue(value);
        } else {
            row.createCell(column).setBlank();
        }
    }
    private void createCell(HSSFRow row, int column, Float value) {
        if (value!=null && !value.isNaN()) {
            row.createCell(column).setCellValue(FDF.format(value));
        } else {
            row.createCell(column).setBlank();
        }
    }

    private void createCell(HSSFRow row, int column, Integer value) {
        if (value!=null) {
            row.createCell(column).setCellValue(IDF.format(value));
        } else {
            row.createCell(column).setBlank();
        }
    }

}
