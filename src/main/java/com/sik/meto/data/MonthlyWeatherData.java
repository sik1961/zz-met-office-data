package com.sik.meto.data;

import java.time.LocalDate;
import java.util.UUID;

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
	
	
	
	public MonthlyWeatherData(String stationName, String stationLocation, LocalDate monthStartDate, Float tempMaxC, Float tempMinC,
			Integer afDays, Float rainfallMm, Float sunHours) {
		super();
		this.id = UUID.randomUUID().toString();
		this.stationName = stationName;
		this.stationLocation = stationLocation;
		this.monthStartDate = monthStartDate;
		this.tempMaxC = tempMaxC;
		this.tempMinC = tempMinC;
		this.afDays = afDays;
		this.rainfallMm = rainfallMm;
		this.sunHours = sunHours;
	}

	public String getId() {
		return this.id;
	}
	public String getStationName() {
		return this.stationName;
	}
	public String getStationLocation() {
		return this.stationLocation;
	}
	public LocalDate getMonthStartDate() {
		return this.monthStartDate;
	}
	public Float getTempMaxC() {
		return this.tempMaxC;
	}
	public Float getTempMinC() {
		return this.tempMinC;
	}
	public Integer getAfDays() {
		return this.afDays;
	}
	public Float getRainfallMm() {
		return this.rainfallMm;
	}
	public Float getSunHours() {
		return this.sunHours;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public void setStationLocation(String stationLocation) {
		this.stationLocation = stationLocation;
	}
	public void setMonthStartDate(LocalDate monthStartDate) {
		this.monthStartDate = monthStartDate;
	}
	public void setTempMaxC(Float tempMaxC) {
		this.tempMaxC = tempMaxC;
	}
	public void setTempMinC(Float tempMinC) {
		this.tempMinC = tempMinC;
	}
	public void setAfDays(Integer afDays) {
		this.afDays = afDays;
	}
	public void setRainfallMm(Float rainfallMm) {
		this.rainfallMm = rainfallMm;
	}
	public void setSunHours(Float sunHours) {
		this.sunHours = sunHours;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.afDays == null) ? 0 : this.afDays.hashCode());
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.monthStartDate == null) ? 0 : this.monthStartDate.hashCode());
		result = prime * result + ((this.rainfallMm == null) ? 0 : this.rainfallMm.hashCode());
		result = prime * result + ((this.stationName == null) ? 0 : this.stationName.hashCode());
		result = prime * result + ((this.stationLocation == null) ? 0 : this.stationLocation.hashCode());
		result = prime * result + ((this.sunHours == null) ? 0 : this.sunHours.hashCode());
		result = prime * result + ((this.tempMaxC == null) ? 0 : this.tempMaxC.hashCode());
		result = prime * result + ((this.tempMinC == null) ? 0 : this.tempMinC.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonthlyWeatherData other = (MonthlyWeatherData) obj;
		if (this.afDays == null) {
			if (other.afDays != null)
				return false;
		} else if (!this.afDays.equals(other.afDays))
			return false;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.monthStartDate == null) {
			if (other.monthStartDate != null)
				return false;
		} else if (!this.monthStartDate.equals(other.monthStartDate))
			return false;
		if (this.rainfallMm == null) {
			if (other.rainfallMm != null)
				return false;
		} else if (!this.rainfallMm.equals(other.rainfallMm))
			return false;
		if (this.stationName == null) {
			if (other.stationName != null)
				return false;
		} else if (!this.stationName.equals(other.stationName))
			return false;
		if (this.stationLocation == null) {
			if (other.stationLocation != null)
				return false;
		} else if (!this.stationLocation.equals(other.stationLocation))
			return false;
		if (this.sunHours == null) {
			if (other.sunHours != null)
				return false;
		} else if (!this.sunHours.equals(other.sunHours))
			return false;
		if (this.tempMaxC == null) {
			if (other.tempMaxC != null)
				return false;
		} else if (!this.tempMaxC.equals(other.tempMaxC))
			return false;
		if (this.tempMinC == null) {
			if (other.tempMinC != null)
				return false;
		} else if (!this.tempMinC.equals(other.tempMinC))
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MonthlyWeatherData [stationName=");
		builder.append(this.stationName);
		builder.append(", monthStartDate=");
		builder.append(this.monthStartDate);
		builder.append(", tempMaxC=");
		builder.append(this.tempMaxC);
		builder.append(", tempMinC=");
		builder.append(this.tempMinC);
		builder.append(", afDays=");
		builder.append(this.afDays);
		builder.append(", rainfallMm=");
		builder.append(this.rainfallMm);
		builder.append(", sunHours=");
		builder.append(this.sunHours);
		builder.append(", stationLocation=");
		builder.append(this.stationLocation);
		builder.append("]");
		return builder.toString();
	}


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
