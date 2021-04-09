
package com.sik.meto.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//import com.sik.sihols.HolidayEvent;
import static com.sik.meto.data.MonthlyWeatherData.*;

public class MetoDataHandler {

	private static final String IMPORT_FILE = "src/main/resources/weather-station-data.dat";
	private static final String EQ = "=";
	private static final String SPACES = " +";
	private String LOC_TIME_FORMAT = "%s %s";
	private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");

	MetoDataUtilities util = new MetoDataUtilities();

	private Map<String, String> urlMap = new HashMap<>();
	private Map<Integer, YearlyAverageWeatherData> yearlyAverageWeatherDataMap = new TreeMap<>();
	private WeatherExtremesData extremes = new WeatherExtremesData();


	public Map<Integer, YearlyAverageWeatherData> getYearlyAverageWeatherDataMap() {
		return this.yearlyAverageWeatherDataMap;
	}

	public WeatherExtremesData getExtremes() {
		return this.extremes;
	}

	protected Set<MonthlyWeatherData> getMonthlyData() throws IOException {
		Set<MonthlyWeatherData> monthlyData = new TreeSet<>();

		this.urlMap = this.buildUrlMap(IMPORT_FILE);

		for (String weatherStation : urlMap.keySet()) {
			monthlyData.addAll(readMonthlyDataFromUrl(weatherStation));
		}

		System.out.println("records=" + monthlyData.size());
		
//		for (MonthlyWeatherData month:monthlyData) {
//			System.out.println(month);
//		}
		
		return monthlyData;

	}

	private Set<MonthlyWeatherData> readMonthlyDataFromUrl(String weatherStation) throws IOException {
		System.out.println("Getting: " + urlMap.get(weatherStation));
		Set<MonthlyWeatherData> monthlyWeatherDataSet = new TreeSet<>();
		int linesInFile=0;
		String location = "n/a";
		URL page = new URL(urlMap.get(weatherStation));
		BufferedReader br =  new BufferedReader(new InputStreamReader(page.openStream()));
		try {

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				linesInFile++;
//				for (String s:inputLine.split(SPACES)) {
//					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>" + s);
//				}
				if (util.isLocationData(inputLine)) {
					location = inputLine;
				}


				String[] fields = inputLine.split(SPACES);

//				System.out.print(">>");
//				for (String f:fields) {
//					System.out.print(f + " ");
//				}
//				System.out.println(" fields.length=" + fields.length);

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
						System.out.println("Warn: " + inputLine + " #fields=" + fields.length);
					}
				}
				if (monthData != null) {
					this.yearlyAverageWeatherDataMap = this.updateYearlyAvarageWeatherDataMap(monthData, this.yearlyAverageWeatherDataMap);
					monthlyWeatherDataSet.add(monthData);
					//this.updateExtremes(monthData);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("lines=" + linesInFile + " records=" + monthlyWeatherDataSet.size());
		br.close();
		return monthlyWeatherDataSet;
	}

	private Map<String, String> buildUrlMap(String fileName) {
		Map<String, String> retVal = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(IMPORT_FILE))) {
			for (String line; (line = br.readLine()) != null;) {
				if (util.isValidUrlProperty(line)) {
					retVal.put(line.split(EQ)[0], line.split(EQ)[1]);
				}
			}
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("File not found: " + IMPORT_FILE, e);
		} catch (IOException e) {
			throw new IllegalStateException("I/O Error on: " + IMPORT_FILE, e);
		}

		return retVal;
	}



	public Map<Integer,YearlyAverageWeatherData> updateYearlyAvarageWeatherDataMap(final MonthlyWeatherData monthlyWeatherData,
																				   final Map<Integer,YearlyAverageWeatherData> yearlyAverageWeatherDataMap) {
		YearlyAverageWeatherData existing = yearlyAverageWeatherDataMap.get(monthlyWeatherData.getMonthStartDate().getYear());
		if (existing == null) {
			yearlyAverageWeatherDataMap.put(monthlyWeatherData.getMonthStartDate().getYear(), util.createYearAverageData(monthlyWeatherData));
		} else {
			yearlyAverageWeatherDataMap.put(existing.getYearStartDate().getYear(),util.updateYearAverageData(monthlyWeatherData, existing));
		}
		return yearlyAverageWeatherDataMap;
	}









}
