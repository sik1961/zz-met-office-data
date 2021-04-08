
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

public class MetoDataImporter {

	private static final String IMPORT_FILE = "src/main/resources/weather-station-data.dat";
	private static final String EQ = "=";
	private static final String AX = "\\*";
	private static final String HASH = "#";
	private static final String DOLLAR = "$";
	private static final String MT = "";
	private static final String SPACES = " +";
	private String LOC_TIME_FORMAT = "%s %s";
	private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");

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
				if (isLocationData(inputLine)) {
					location = inputLine;
				}


				String[] fields = inputLine.split(SPACES);

//				System.out.print(">>");
//				for (String f:fields) {
//					System.out.print(f + " ");
//				}
//				System.out.println(" fields.length=" + fields.length);

				MonthlyWeatherData monthData = null;
				if (this.isMonthlyData(fields)) {
					if (fields.length >= 8) {
						monthData = MonthlyWeatherData.builder()
								.stationName(weatherStation)
								.stationLocation(location)
								.monthStartDate(this.getMonthStartDate(getInt(fields[I_YEAR]), getInt(fields[I_MNTH])))
								.tempMaxC(getFloat(fields[I_TMAX]))
								.tempMinC(getFloat(fields[I_TMIN]))
								.afDays(getInt(fields[I_AFDY]))
								.rainfallMm(getFloat(fields[I_RNMM]))
								.sunHours(getFloat(fields[I_SUNH])).build();
					} else if (fields.length == 7) {
						monthData = MonthlyWeatherData.builder()
								.stationName(weatherStation)
								.stationLocation(location)
								.monthStartDate(this.getMonthStartDate(getInt(fields[I_YEAR]), getInt(fields[I_MNTH])))
								.tempMaxC(getFloat(fields[I_TMAX]))
								.tempMinC(getFloat(fields[I_TMIN]))
								.afDays(getInt(fields[I_AFDY]))
								.rainfallMm(getFloat(fields[I_RNMM])).build();
					} else {
						System.out.println("Warn: " + inputLine + " #fields=" + fields.length);
					}
				}
				if (monthData != null) {
					this.yearlyAverageWeatherDataMap = this.updateYearlyAvarageWeatherDataMap(monthData, this.yearlyAverageWeatherDataMap);
					monthlyWeatherDataSet.add(monthData);
					this.updateExtremes(monthData);
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
				if (isValidUrlProperty(line)) {
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

	public Set<MonthlyWeatherData> filterByDates(final Set<MonthlyWeatherData> monthlyWeatherData, final LocalDate start, final LocalDate end) {
		return monthlyWeatherData.stream()
				.filter(w -> (w.getMonthStartDate().isEqual(start) || w.getMonthStartDate().isAfter(start)) &&
						w.getMonthStartDate().isBefore(end))
				.sorted()
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public Set<MonthlyWeatherData> filterByStation(final Set<MonthlyWeatherData> monthlyWeatherData, final String stationName) {
		return monthlyWeatherData.stream()
				.filter(w -> (w.getStationName().equals(stationName)))
				.sorted()
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public Map<Integer,YearlyAverageWeatherData> updateYearlyAvarageWeatherDataMap(final MonthlyWeatherData monthlyWeatherData,
																				   final Map<Integer,YearlyAverageWeatherData> yearlyAverageWeatherDataMap) {
		YearlyAverageWeatherData existing = yearlyAverageWeatherDataMap.get(monthlyWeatherData.getMonthStartDate().getYear());
		if (existing == null) {
			yearlyAverageWeatherDataMap.put(monthlyWeatherData.getMonthStartDate().getYear(), createYearAverageData(monthlyWeatherData));
		} else {
			yearlyAverageWeatherDataMap.put(existing.getYearStartDate().getYear(),updateYearAverageData(monthlyWeatherData, existing));
		}
		return yearlyAverageWeatherDataMap;
	}

	private YearlyAverageWeatherData createYearAverageData(MonthlyWeatherData monthlyWeatherData) {
		return YearlyAverageWeatherData.builder()
				.yearStartDate(getYearStartDate(monthlyWeatherData.getMonthStartDate().getYear()))
				.countTempMaxC(monthlyWeatherData.getTempMaxC()!=null?1:0)
				.totTempMaxC(monthlyWeatherData.getTempMaxC()!=null?monthlyWeatherData.getTempMaxC():0)
				.countTempMinC(monthlyWeatherData.getTempMinC()!=null?1:0)
				.totTempMinC(monthlyWeatherData.getTempMinC()!=null?monthlyWeatherData.getTempMinC():0)
				.countAfDays(monthlyWeatherData.getAfDays()!=null?1:0)
				.totAfDays(monthlyWeatherData.getAfDays()!=null?intToFloat(monthlyWeatherData.getAfDays()):0)
				.countRainfallMm(monthlyWeatherData.getRainfallMm()!=null?1:0)
				.totRainfallMm(monthlyWeatherData.getRainfallMm()!=null?monthlyWeatherData.getRainfallMm():0)
				.countSunHours(monthlyWeatherData.getSunHours()!=null?1:0)
				.totSunHours(monthlyWeatherData.getSunHours()!=null?monthlyWeatherData.getSunHours():0)
				.build();

	}

	private YearlyAverageWeatherData updateYearAverageData(MonthlyWeatherData monthlyWeatherData,
														   YearlyAverageWeatherData existing) {
		return YearlyAverageWeatherData.builder()
				.yearStartDate(existing.getYearStartDate())
				.countTempMaxC(existing.getCountTempMaxC() + (monthlyWeatherData.getTempMaxC()!=null?1:0))
				.totTempMaxC(existing.getTotTempMaxC() + (monthlyWeatherData.getTempMaxC()!=null?monthlyWeatherData.getTempMaxC():0))
				.countTempMinC(existing.getCountTempMinC() + (monthlyWeatherData.getTempMinC()!=null?1:0))
				.totTempMinC(existing.getTotTempMinC() + (monthlyWeatherData.getTempMinC()!=null?monthlyWeatherData.getTempMinC():0))
				.countAfDays(existing.getCountAfDays() + (monthlyWeatherData.getAfDays()!=null?1:0))
				.totAfDays(existing.getTotAfDays() + (monthlyWeatherData.getAfDays()!=null?intToFloat(monthlyWeatherData.getAfDays()):0))
				.countRainfallMm(existing.getCountRainfallMm() + (monthlyWeatherData.getRainfallMm()!=null?1:0))
				.totRainfallMm(existing.getTotRainfallMm() + (monthlyWeatherData.getRainfallMm()!=null?monthlyWeatherData.getRainfallMm():0))
				.countSunHours(existing.getCountSunHours() + (monthlyWeatherData.getSunHours()!=null?1:0))
				.totSunHours(existing.getTotSunHours() + (monthlyWeatherData.getSunHours()!=null?monthlyWeatherData.getSunHours():0))
				.build();
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

	private Float intToFloat(Integer i) {
		return Float.parseFloat(i.toString());
	}

	private LocalDate getMonthStartDate(final int year, final int month) {
		return LocalDate.of(year, month, 1);
	}

	private LocalDate getYearStartDate(final int year) {
		return LocalDate.of(year, 1, 1);
	}

	private boolean isValidUrlProperty(String s) {
		return s.indexOf(EQ) > 1 && s.split(EQ).length == 2;
	}

	private boolean isMonthlyData(String[] fields) {
		return fields != null &&
			fields.length >= 6 &&
			isNumeric(fields[I_YEAR]) &&
			isNumeric(fields[I_MNTH]);
	}

	private boolean isLocationData(String s) {
		return s != null &&
				s.contains(MonthlyWeatherData.LOC) &&
				s.contains(MonthlyWeatherData.LAT) &&
				s.contains(MonthlyWeatherData.LON);
	}

	private Integer getInt(String i) {
		try {
			return Integer.parseInt(strip(i));
		}
		catch(NumberFormatException nfe) {
			return null;
		}
	}

	private Float getFloat(String i) {
		try {
			return Float.parseFloat(strip(i));
		}
		catch(NumberFormatException nfe) {
			return null;
		}
	}

	private boolean isNumeric(String i) {
		try {
			Float.parseFloat(strip(i));
		}
		catch(NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private String strip(String s) {
		return s.replaceAll(AX, MT)
				.replaceAll(HASH,MT)
				.replaceAll(DOLLAR,MT);
	}

}
