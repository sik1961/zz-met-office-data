package com.sik.meto.data;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MetoExcelWriter {

    private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final DecimalFormat DF = new DecimalFormat("##0.0");
    private static final String CR = "\n";

    private String MONTH_DATA_FORMAT = "%1$30s %2$15s %3$15s %4$15s %5$15s %6$15s %7$15s";

    private FileWriter mainWriter;
    private FileWriter locationWriter;
    private FileWriter averageWriter;
    private String historicFileName;
    private FileOutputStream historicOut;
    private String summaryFileName;
    private FileOutputStream summaryOut;


    public MetoExcelWriter() throws IOException {
        //this.extremes = new WeatherExtremesData();
        this.mainWriter = new FileWriter("/Users/sik/met-office/zz-metoffice-full.txt");
        this.averageWriter = new FileWriter("/Users/sik/met-office/metoffice-averages-extremes.txt");
        this.historicFileName = "/Users/sik/met-office/MetOfficeHistoricData.xlsx";
        this.historicOut = new FileOutputStream(this.historicFileName);
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
                    .forEach(r -> this.writeExcelRow(r,sheet, rowCount.incrementAndGet()));
        }
        historicOut = new FileOutputStream(historicFileName);

        workbook.write(historicOut);
        historicOut.close();
        workbook.close();
        System.out.println("Excel file has been generated successfully.");
    }

    private void writeExcelRow(MonthlyWeatherData monthData, HSSFSheet sheet, int cellNumber) {
        HSSFRow row = sheet.createRow((short) cellNumber);
        createCell(row,0,monthData.getStationName());
        createCell(row,1,monthData.getMonthStartDate().format(YYYY_MM));
        createCell(row,2,monthData.getTempMinC());
        createCell(row,3,monthData.getTempMaxC());
        createCell(row,4,monthData.getAfDays());
        createCell(row,5,monthData.getRainfallMm());
        createCell(row,6,monthData.getSunHours());

    }
    private void createCell(HSSFRow row, int column, String value) {
        if (value!=null) {
            row.createCell(column).setCellValue(value);
        } else {
            row.createCell(column).setBlank();
        }
    }
    private void createCell(HSSFRow row, int column, Float value) {
        if (value!=null) {
            row.createCell(column).setCellValue(DF.format(value));
        } else {
            row.createCell(column).setBlank();
        }
    }

    private void createCell(HSSFRow row, int column, Integer value) {
        if (value!=null) {
            row.createCell(column).setCellValue(value);
        } else {
            row.createCell(column).setBlank();
        }
    }

}
