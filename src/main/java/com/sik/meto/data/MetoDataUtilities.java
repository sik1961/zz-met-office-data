package com.sik.meto.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.sik.meto.data.MonthlyWeatherData.I_MNTH;
import static com.sik.meto.data.MonthlyWeatherData.I_YEAR;

public class MetoDataUtilities {

    private static final String EQ = "=";
    private static final String AX = "\\*";
    private static final String HASH = "#";
    private static final String DOLLAR = "$";
    private static final String MT = "";
    private static final String SPACES = " +";
    private String LOC_TIME_FORMAT = "%s %s";
    private static final DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyy/MM");

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

    public Set<MonthlyWeatherData> filterByYear(final Set<MonthlyWeatherData> monthlyWeatherData, final int year) {
        return monthlyWeatherData.stream()
                .filter(w -> (w.getMonthStartDate().getYear()==year))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<Integer,YearlyAverageWeatherData> buildYearlyAvarageWeatherDataMap(final Set<MonthlyWeatherData> monthlyWeatherData) {
        Map<Integer, YearlyAverageWeatherData> yearlyAverageDataMap = new TreeMap<>();
        for (MonthlyWeatherData month: monthlyWeatherData) {
            YearlyAverageWeatherData existing = yearlyAverageDataMap.get(month.getMonthStartDate().getYear());
            if (existing == null) {
                yearlyAverageDataMap.put(month.getMonthStartDate().getYear(), createYearAverageData(month));
            } else {
                yearlyAverageDataMap.put(existing.getYearStartDate().getYear(), updateYearAverageData(month, existing));
            }
        }
        return yearlyAverageDataMap;
    }

    public YearlyAverageWeatherData createYearAverageData(MonthlyWeatherData monthlyWeatherData) {
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

    public YearlyAverageWeatherData updateYearAverageData(MonthlyWeatherData monthlyWeatherData,
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

    public Float intToFloat(Integer i) {
        return Float.parseFloat(i.toString());
    }

    public LocalDate getMonthStartDate(final int year, final int month) {
        return LocalDate.of(year, month, 1);
    }

    public LocalDate getYearStartDate(final int year) {
        return LocalDate.of(year, 1, 1);
    }

    public boolean isValidUrlProperty(String s) {
        return s.indexOf(EQ) > 1 && s.split(EQ).length == 2;
    }

    public boolean isMonthlyData(String[] fields) {
        return fields != null &&
                fields.length >= 6 &&
                isNumeric(fields[I_YEAR]) &&
                isNumeric(fields[I_MNTH]);
    }

    public boolean isLocationData(String s) {
        return s != null &&
                s.contains(MonthlyWeatherData.LOC) &&
                s.contains(MonthlyWeatherData.LAT) &&
                s.contains(MonthlyWeatherData.LON);
    }

    public Integer getInt(String i) {
        try {
            return Integer.parseInt(strip(i));
        }
        catch(NumberFormatException nfe) {
            return null;
        }
    }

    public Float getFloat(String i) {
        try {
            return Float.parseFloat(strip(i));
        }
        catch(NumberFormatException nfe) {
            return null;
        }
    }

    public boolean isNumeric(String i) {
        try {
            Float.parseFloat(strip(i));
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public String strip(String s) {
        return s.replaceAll(AX, MT)
                .replaceAll(HASH,MT)
                .replaceAll(DOLLAR,MT);
    }


}
