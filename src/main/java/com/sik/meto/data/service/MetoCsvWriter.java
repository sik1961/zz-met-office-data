package com.sik.meto.data.service;

import com.sik.meto.data.model.MonthlyWeatherData;
import com.sik.meto.data.model.WeatherExtremesData;
import com.sik.meto.data.model.YearlyAverageWeatherData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
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
public class MetoCsvWriter {

    private static final Logger LOG = LoggerFactory.getLogger(MetoCsvWriter.class);

    private static final String FILE_PATH = "/Users/sik/met-office/";
    private static final String CSV_EXT = ".csv";
    private static final String MO_HISTORIC = "MetOfficeHistoricData";

    private static final String MSG_SUCCESS = " file has been generated successfully.";

    private String historicCsvFileName;

    public MetoCsvWriter() throws IOException {
        this.historicCsvFileName = FILE_PATH + MO_HISTORIC + CSV_EXT;
    }

    public void writeHistoricWorkbook(Map<String, Set<MonthlyWeatherData>> locationData) throws IOException {

        BufferedWriter writer;
        writer = new BufferedWriter(new FileWriter(historicCsvFileName, true));
        for(String location: locationData.keySet()) {
            for (MonthlyWeatherData monthlyWeatherData : locationData.get(location)) {
                writer.write(monthlyWeatherData.toCsv());
                writer.newLine();
            }
        }
        writer.close();

        LOG.info(historicCsvFileName + MSG_SUCCESS);
    }
}
