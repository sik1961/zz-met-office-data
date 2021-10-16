
package com.sik.meto.data.service;

import com.sik.meto.data.model.MonthlyWeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import static com.sik.meto.data.model.MonthlyWeatherData.*;

@Component
public class MetoDataHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MetoDataHandler.class);

	private static final String IMPORT_FILE = "src/main/resources/weather-station-data.dat";
	private static final String EQ = "=";
	private static final String SPACES = " +";

	MetoDataUtilities util = new MetoDataUtilities();

	private Map<String, String> urlMap = new HashMap<>();

	public Set<MonthlyWeatherData> getMonthlyData() throws IOException {
		Set<MonthlyWeatherData> monthlyData = new TreeSet<>();

		this.urlMap = this.buildUrlMap(IMPORT_FILE);

		for (String weatherStation : urlMap.keySet()) {
			monthlyData.addAll(readMonthlyDataFromUrl(weatherStation));
		}

		LOG.info("records=" + monthlyData.size());

		return monthlyData;

	}

	private Set<MonthlyWeatherData> readMonthlyDataFromUrl(String weatherStation) throws IOException {
		LOG.info("Getting: " + urlMap.get(weatherStation));
		Set<MonthlyWeatherData> monthlyWeatherDataSet = new TreeSet<>();
		int linesInFile=0;
		String location = "n/a";
		URL page = new URL(urlMap.get(weatherStation));
		BufferedReader br =  new BufferedReader(new InputStreamReader(page.openStream()));
		try {

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				linesInFile++;

				if (util.isLocationData(inputLine)) {
					location = inputLine;
				}

				String[] fields = inputLine.split(SPACES);

				MonthlyWeatherData monthData = null;
				if (util.isMonthlyData(fields)) {
					if (fields.length >= 8) {
						monthData = MonthlyWeatherData.builder()
								.stationName(weatherStation)
								.stationLocation(location)
								.monthStartDate(util.getMonthStartDate(util.getInt(fields[I_YEAR]), util.getInt(fields[I_MNTH])))
								.tempMaxC(util.getFloat(fields[I_TMAX]))
								.tempMinC(util.getFloat(fields[I_TMIN]))
								.afDays(util.getInt(fields[I_AFDY]))
								.rainfallMm(util.getFloat(fields[I_RNMM]))
								.sunHours(util.getFloat(fields[I_SUNH])).build();
					} else if (fields.length == 7) {
						monthData = MonthlyWeatherData.builder()
								.stationName(weatherStation)
								.stationLocation(location)
								.monthStartDate(util.getMonthStartDate(util.getInt(fields[I_YEAR]), util.getInt(fields[I_MNTH])))
								.tempMaxC(util.getFloat(fields[I_TMAX]))
								.tempMinC(util.getFloat(fields[I_TMIN]))
								.afDays(util.getInt(fields[I_AFDY]))
								.rainfallMm(util.getFloat(fields[I_RNMM])).build();
					} else {
						LOG.info("Warn: " + inputLine + " #fields=" + fields.length);
					}
				}
				if (monthData != null) {
					monthlyWeatherDataSet.add(monthData);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		LOG.info("lines=" + linesInFile + " records=" + monthlyWeatherDataSet.size());
		br.close();
		return monthlyWeatherDataSet;
	}

	private Map<String, String> buildUrlMap(String filename) {
		Map<String, String> retVal = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			for (String line; (line = br.readLine()) != null;) {
				if (util.isValidUrlProperty(line)) {
					retVal.put(line.split(EQ)[0], line.split(EQ)[1]);
				}
			}
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("File not found: " + filename, e);
		} catch (IOException e) {
			throw new IllegalStateException("I/O Error on: " + filename, e);
		}

		return retVal;
	}

}
