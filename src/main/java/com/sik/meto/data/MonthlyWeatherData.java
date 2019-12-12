package com.sik.meto.data;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class MonthlyWeatherData  implements Comparable<MonthlyWeatherData> {

	protected static final int I_YEAR = 1;
	protected static final int I_MNTH = 2;
	protected static final int I_TMAX = 3;
	protected static final int I_TMIN = 4;
	protected static final int I_AFDY = 5;
	protected static final int I_RNMM = 6;
	protected static final int I_SUNH = 7;

	protected static final String LOC = "Location";
	protected static final String LAT = "Lat";
	protected static final String LON = "Lon";

	private String id;
	private String stationName;
	private String stationLocation;
	private LocalDate monthStartDate;
	private Float tempMaxC;
	private Float tempMinC;
	private Integer afDays;
	private Float rainfallMm;
	private Float sunHours;
	
	@Override
	public int compareTo(MonthlyWeatherData that) {
		if (this.monthStartDate.isBefore(that.monthStartDate)) {
			return -1;
		} else if (this.monthStartDate.isAfter(that.monthStartDate)) {
			return 1;
		} else {
			return that.stationName.hashCode() - this.stationName.hashCode();
		}
	}
}
