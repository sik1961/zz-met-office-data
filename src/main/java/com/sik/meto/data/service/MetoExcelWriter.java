package com.sik.meto.data.service;

import com.sik.meto.data.model.MonthlyWeatherData;
import com.sik.meto.data.model.WeatherExtremesData;
import com.sik.meto.data.model.YearlyAverageWeatherData;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class MetoExcelWriter {

    private static final Logger LOG = LoggerFactory.getLogger(MetoExcelWriter.class);
    private static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MM = DateTimeFormatter.ofPattern("MM");
    private static final DecimalFormat FDF = new DecimalFormat("##0.0");
    private static final DecimalFormat IDF = new DecimalFormat("##0");
    private static final String CR = "\n";
    private static final String FILE_PATH = "/Users/sik/met-office/";
    private static final String XLSX_EXT = ".xlsx";
    private static final String ZIP_EXT = ".zip";
    private static final String MO_HISTORIC = "MetOfficeHistoricData";
    private static final String MO_AVERAGES = "MetOfficeYearlyAverages";
    private static final String MO_EXTREMES = "MetOfficeExtremes";
    private static final String T_STATION = "Station";
    private static final String T_YEAR = "Year";
    private static final String T_MONTH = "Month";
    private static final String T_MINTEMP = "Min.Temp";
    private static final String T_MEDTEMP = "Med.Temp";
    private static final String T_MAXTEMP = "Max.Temp";
    private static final String T_FROSTDAYS = "FrostDays";
    private static final String T_RAINMM = "RainMM";
    private static final String T_SUNHOURS = "SunHours";
    private static final String T_ALL = "All";
    private static final String T_MAXRAINFALLMM = "Max.RainfallMM";
    private static final String T_AFDAYS = "Max.AFDays";
    private static final String T_EXTREMES = "Extremes";
    private static final String T_NAME = "Extreme";
    private static final String T_EXTREME = "Extreme";
    private static final String T_LOCTIME = "Loc/Time";
    private static final String T_VALUE = "Value";

    private static final String MSG_SUCCESS = " file has been generated successfully.";

    MetoDataUtilities utility = new MetoDataUtilities();

    private String historicXlsFileName;
    private String historicZipFileName;
    private FileOutputStream historicXlsOut;
    private String summaryXlsFileName;
    private String summaryZipFileName;
    private FileOutputStream summaryXlsOut;
    private String extremesXlsFileName;
    private String extremesZipFileName;
    private FileOutputStream extremesXlsOut;

    public MetoExcelWriter() throws IOException {
        this.historicXlsFileName = FILE_PATH + MO_HISTORIC + XLSX_EXT;
        this.historicZipFileName = FILE_PATH + MO_HISTORIC + ZIP_EXT;
        this.historicXlsOut = new FileOutputStream(this.historicXlsFileName);
        this.summaryXlsFileName = FILE_PATH + MO_AVERAGES + XLSX_EXT;
        this.summaryZipFileName = FILE_PATH + MO_AVERAGES + ZIP_EXT;
        this.summaryXlsOut = new FileOutputStream(this.summaryXlsFileName);
        this.extremesXlsFileName = FILE_PATH + MO_EXTREMES + XLSX_EXT;
        this.extremesZipFileName = FILE_PATH + MO_EXTREMES + ZIP_EXT;
        this.extremesXlsOut = new FileOutputStream(this.extremesXlsFileName);
    }

    public void writeHistoricWorkbook(Map<String, Set<MonthlyWeatherData>> locationData) throws IOException {

        HSSFWorkbook workbook = new HSSFWorkbook();

        for(String location: locationData.keySet()) {
            HSSFSheet sheet = workbook.createSheet(location);

            HSSFRow rowhead = sheet.createRow((short) 0);
            rowhead.createCell(0).setCellValue(T_STATION);
            rowhead.createCell(1).setCellValue(T_YEAR);
            rowhead.createCell(2).setCellValue(T_MONTH);
            rowhead.createCell(3).setCellValue(T_MINTEMP);
            rowhead.createCell(4).setCellValue(T_MEDTEMP);
            rowhead.createCell(5).setCellValue(T_MAXTEMP);
            rowhead.createCell(6).setCellValue(T_FROSTDAYS);
            rowhead.createCell(7).setCellValue(T_RAINMM);
            rowhead.createCell(8).setCellValue(T_SUNHOURS);

            AtomicInteger rowCount = new AtomicInteger();
            locationData.get(location).stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .forEach(r -> this.writeHistoricRow(r,sheet, rowCount.incrementAndGet()));
        }

        workbook.write(historicXlsOut);
        historicXlsOut.close();
        workbook.close();

        LOG.info(historicXlsFileName + MSG_SUCCESS);

        try (
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(historicZipFileName))) {
            File fileToZip = new File(historicXlsFileName);
            zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
            Files.copy(fileToZip.toPath(), zipOut);
        }

        LOG.info(historicZipFileName + MSG_SUCCESS);
    }

    public void writeAveragesWorkbook(Map<String, Set<MonthlyWeatherData>> locationData) throws IOException {
        Set<MonthlyWeatherData> everything = new HashSet<>();
        locationData.entrySet().forEach(m -> everything.addAll(m.getValue()));

        HSSFWorkbook workbook = new HSSFWorkbook();

        this.createAveragesWorksheet(T_ALL, workbook, utility.buildYearlyAvarageWeatherDataMap(everything));

        for (String location: locationData.keySet()) {
            this.createAveragesWorksheet(location, workbook, utility.buildYearlyAvarageWeatherDataMap(locationData.get(location)));
        }

        workbook.write(summaryXlsOut);
        summaryXlsOut.close();
        workbook.close();

        LOG.info(summaryXlsFileName + MSG_SUCCESS);

        try (
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(summaryZipFileName))) {
            File fileToZip = new File(summaryXlsFileName);
            zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
            Files.copy(fileToZip.toPath(), zipOut);
        }

        LOG.info(summaryZipFileName + MSG_SUCCESS);
    }

    public void writeExtremesWorkbook(Map<String, WeatherExtremesData> extremesData) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet(T_EXTREMES);

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.createCell(0).setCellValue(T_NAME);
        rowhead.createCell(1).setCellValue(T_LOCTIME);
        rowhead.createCell(2).setCellValue(T_EXTREME);
        rowhead.createCell(3).setCellValue(T_VALUE);

        int rowNumber = 1;
        for(String location: extremesData.keySet()) {
            rowNumber = this.writeExtremesRows(sheet,location, extremesData.get(location),rowNumber);
        }

        workbook.write(extremesXlsOut);
        extremesXlsOut.close();
        workbook.close();

        LOG.info(extremesXlsFileName + MSG_SUCCESS);

        try (
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(extremesZipFileName))) {
            File fileToZip = new File(extremesXlsFileName);
            zipOut.putNextEntry(new ZipEntry(fileToZip.getName()));
            Files.copy(fileToZip.toPath(), zipOut);
        }

        LOG.info(extremesZipFileName + MSG_SUCCESS);
    }

    private int writeExtremesRows(HSSFSheet sheet, String location, WeatherExtremesData weatherExtremesData, int row) {
        HSSFRow minTemp = sheet.createRow((short) row);
        createCell(minTemp,0,location);
        createCell(minTemp,1,weatherExtremesData.getMinTempLocTime());
        createCell(minTemp,2, T_MINTEMP);
        createCell(minTemp,3,weatherExtremesData.getMinTemp());
        row++;
        HSSFRow maxTemp = sheet.createRow((short) row);
        createCell(maxTemp,0,location);
        createCell(maxTemp,1,weatherExtremesData.getMaxTempLocTime());
        createCell(maxTemp,2, T_MAXTEMP);
        createCell(maxTemp,3,weatherExtremesData.getMaxTemp());
        row++;
        HSSFRow maxRain = sheet.createRow((short) row);
        createCell(maxRain,0,location);
        createCell(maxRain,1,weatherExtremesData.getMaxRainfallMmLocTime());
        createCell(maxRain,2, T_MAXRAINFALLMM);
        createCell(maxRain,3,weatherExtremesData.getMaxRainfallMm());
        row++;
        HSSFRow maxAFD = sheet.createRow((short) row);
        createCell(maxAFD,0,location);
        createCell(maxAFD,1,weatherExtremesData.getMaxAfDaysLocTime());
        createCell(maxAFD,2, T_AFDAYS);
        createCell(maxAFD,3,weatherExtremesData.getMaxAfDays());
        row++;
        HSSFRow maxSun = sheet.createRow((short) row);
        createCell(maxSun,0,location);
        createCell(maxSun,1,weatherExtremesData.getMaxSunHoursLocTime());
        createCell(maxSun,2, T_SUNHOURS);
        createCell(maxSun,3,weatherExtremesData.getMaxSunHours());
        row++;
        row++;
        return row;
    }

    private void createAveragesWorksheet(String name, HSSFWorkbook workbook, Map<Integer, YearlyAverageWeatherData> averageData) {

        HSSFSheet sheet = workbook.createSheet(name);

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.createCell(0).setCellValue(T_STATION);
        rowhead.createCell(1).setCellValue(T_YEAR);
        rowhead.createCell(2).setCellValue(T_MINTEMP);
        rowhead.createCell(3).setCellValue(T_MEDTEMP);
        rowhead.createCell(4).setCellValue(T_MAXTEMP);
        rowhead.createCell(5).setCellValue(T_FROSTDAYS);
        rowhead.createCell(6).setCellValue(T_RAINMM);
        rowhead.createCell(7).setCellValue(T_SUNHOURS);

        int rowCount = 1;
        for (Integer year: averageData.keySet()) {
            this.writeAveragesRow(averageData.get(year), sheet, rowCount++, name);
        }
    }

    private void writeHistoricRow(MonthlyWeatherData monthData, HSSFSheet sheet, int rowNumber) {
        HSSFRow row = sheet.createRow((short) rowNumber);
        createCell(row,0,monthData.getStationName());
        createCell(row,1,monthData.getMonthStartDate().format(YYYY));
        createCell(row,2,monthData.getMonthStartDate().format(MM));
        createCell(row,3,monthData.getTempMinC());
        createCell(row,4,monthData.getTempMedC());
        createCell(row,5,monthData.getTempMaxC());
        createCell(row,6,monthData.getAfDays());
        createCell(row,7,monthData.getRainfallMm());
        createCell(row,8,monthData.getSunHours());
    }

    private void writeAveragesRow(YearlyAverageWeatherData averageData, HSSFSheet sheet, int rowNumber, String location) {
        HSSFRow row = sheet.createRow((short) rowNumber);
        createCell(row,0,location);
        createCell(row,1,averageData.getYearStartDate().getYear());
        createCell(row,2,averageData.getAvgTempMinC());
        createCell(row,3,averageData.getAvgTempMedC());
        createCell(row,4,averageData.getAvgTempMaxC());
        createCell(row,5,averageData.getAvgAfDays());
        createCell(row,6,averageData.getAvgRainfallMm());
        createCell(row,7,averageData.getAvgSunHours());
    }

    private void createCell(HSSFRow row, int column, String value) {
        if (value!=null) {
            row.createCell(column).setCellType(CellType.STRING);
            row.getCell(column).setCellValue(value);
        } else {
            row.createCell(column).setBlank();
        }
    }
    private void createCell(HSSFRow row, int column, Float value) {
        if (value!=null && !value.isNaN()) {
            row.createCell(column).setCellType(CellType.NUMERIC);
            row.getCell(column).setCellValue(Float.parseFloat(FDF.format(value)));
        } else {
            row.createCell(column).setBlank();
        }
    }

    private void createCell(HSSFRow row, int column, Integer value) {
        if (value!=null) {
            row.createCell(column).setCellType(CellType.NUMERIC);
            row.getCell(column).setCellValue(Integer.parseInt(IDF.format(value)));
        } else {
            row.createCell(column).setBlank();
        }
    }

}
