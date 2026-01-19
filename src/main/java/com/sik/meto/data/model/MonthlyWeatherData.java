package com.sik.meto.data.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class MonthlyWeatherData  implements Comparable<MonthlyWeatherData> {

	public static final int I_YEAR = 1;
	public static final int I_MNTH = 2;
	public static final int I_TMAX = 3;
	public static final int I_TMIN = 4;
	public static final int I_AFDY = 5;
	public static final int I_RNMM = 6;
	public static final int I_SUNH = 7;

	public static final String LOC = "Location";
	public static final String LAT = "Lat";
	public static final String LON = "Lon";
	public static final String CSV_FMT = "%s,%s,%s,%.2f,%.2f,%.2f,%s,%s,%s";

	private String id;
	private String stationName;
	private String stationLocation;
	private LocalDate monthStartDate;
	private Float tempMaxC;
	private Float tempMedC;
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


	public String toCsv() {
		return String.format(CSV_FMT,this.id,
				this.stationName,
				this.stationLocation,
				this.tempMaxC,
				this.tempMedC,
				this.tempMinC,
				this.afDays,
				this.rainfallMm,
				this.sunHours);
	}
}
