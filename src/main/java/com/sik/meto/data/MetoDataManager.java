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
	private static final String AX = "/*";
	private static final String MT = "";
	private static final String SPACES = " +";

	private Map<String, String> urlMap = new HashMap<>();
	private Set<MonthlyWeatherData> monthlyWeatherData;

	protected Set<MonthlyWeatherData> getMonthlyData() {
		Set<MonthlyWeatherData> monthlyData = new TreeSet<>();

		this.urlMap = this.buildUrlMap(IMPORT_FILE);

		for (String key : urlMap.keySet()) {
			monthlyData.addAll(readMonthlyDataFromUrl(key));
		}

		System.out.println("records=" + monthlyData.size());
		
//		for (MonthlyWeatherData month:monthlyData) {
//			System.out.println(month);
//		}
		
		return monthlyData;

	}

	private Set<MonthlyWeatherData> readMonthlyDataFromUrl(String key) {
		System.out.println("Getting: " + urlMap.get(key));
		Set<MonthlyWeatherData> monthlyData = new TreeSet<>();
		int linesInFile=0;
		String location = "n/a";
		try {

			URL page = new URL(urlMap.get(key));
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

				if (this.isMonthlyData(fields)) {
					if (fields.length == 8) {
						monthlyData.add(new MonthlyWeatherData(key, location,
								this.getMonthStartDate(getInt(fields[I_YEAR]), getInt(fields[I_MNTH])),
								getFloat(fields[I_TMAX]),
								getFloat(fields[I_TMIN]),
								getInt(fields[I_AFDY]),
								getFloat(fields[I_RNMM]),
								getFloat(fields[I_SUNH])));
					} else if (fields.length == 7) {
						monthlyData.add(new MonthlyWeatherData(key, location,
								this.getMonthStartDate(getInt(fields[I_YEAR]), getInt(fields[I_MNTH])),
								getFloat(fields[I_TMAX]),
								getFloat(fields[I_TMIN]),
								getInt(fields[I_AFDY]),
								getFloat(fields[I_RNMM]),
								null));
					}
				}
				//br.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("lines=" + linesInFile + " records=" + monthlyData.size());
		return monthlyData;
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

//		for (Object k : retVal.keySet()) {
//			System.out.println(">>>>>>>>" + k.toString() + " = " + retVal.get(k) );
//		}

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

	private LocalDate getMonthStartDate(final int year, final int month) {
		return LocalDate.of(year, month, 1);
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
		return s.replaceAll(AX, MT);
	}

}
