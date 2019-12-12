package com.sik.meto.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

//import com.sik.sihols.HolidayEvent;
import static com.sik.meto.data.MonthlyWeatherData.*;

public class MetoDataManager {

	private static final String IMPORT_FILE = "src/main/resources/weather-station-data.dat";
	private static final String EQ = "=";
	private static final String AX = "\\*";
	private static final String HASH = "#";
	private static final String DOLLAR = "$";
	private static final String MT = "";
	private static final String SPACES = " +";

	private Map<String, String> urlMap = new HashMap<>();
	private Map<Integer, YearlyAverageWeatherData> yearlyAverageWeatherDataMap = new TreeMap<>();

	public Map<Integer, YearlyAverageWeatherData> getYearlyAverageWeatherDataMap() {
		return this.yearlyAverageWeatherDataMap;
	}

	protected Set<MonthlyWeatherData> getMonthlyData() {
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

	private Set<MonthlyWeatherData> readMonthlyDataFromUrl(String weatherStation) {
		System.out.println("Getting: " + urlMap.get(weatherStation));
		Set<MonthlyWeatherData> monthlyWeatherDataSet = new TreeSet<>();
		int linesInFile=0;
		String location = "n/a";
		try {

			URL page = new URL(urlMap.get(weatherStation));
			BufferedReader br = new BufferedReader(new InputStreamReader(page.openStream()));

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
					this.updateYearlyAvarageWeatherDataMap(monthData);
					monthlyWeatherDataSet.add(monthData);
				}
				//br.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("lines=" + linesInFile + " records=" + monthlyWeatherDataSet.size());

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

	public void updateYearlyAvarageWeatherDataMap(MonthlyWeatherData monthlyWeatherData) {
		YearlyAverageWeatherData existing = this.yearlyAverageWeatherDataMap.get(monthlyWeatherData.getMonthStartDate().getYear());
		if (existing == null) {
			this.yearlyAverageWeatherDataMap.put(monthlyWeatherData.getMonthStartDate().getYear(),
					YearlyAverageWeatherData.builder()
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
							.build());
		} else {
			this.yearlyAverageWeatherDataMap.put(existing.getYearStartDate().getYear(),
					YearlyAverageWeatherData.builder()
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
							.build());
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
