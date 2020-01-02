package it.polimi.marcermarchiscianamotta.safestreets.model;

import java.util.Date;

public class Group {
	private double latitude;
	private double longitude;
	private String typeOfViolation;
	private Date firstTimestamp;
	private Date lastTimestamp;
	private ReportStatusEnum groupStatus;
	private ViolationEnum typeOfViolaiton;
	private String licensePlate;

	public Group() {
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getTypeOfViolation() {
		return typeOfViolation;
	}

	public Date getFirstTimestamp() {
		return firstTimestamp;
	}

	public Date getLastTimestamp() {
		return lastTimestamp;
	}

	public ReportStatusEnum getGroupStatus() {
		return groupStatus;
	}

	public ViolationEnum getTypeOfViolaiton() {
		return typeOfViolaiton;
	}

	public String getLicensePlate() {
		return licensePlate;
	}


	@Override
	public String toString() {
		return "Group{" +
				"latitude=" + latitude +
				", longitude=" + longitude +
				", typeOfViolation='" + typeOfViolation + '\'' +
				", firstTimestamp=" + firstTimestamp +
				", lastTimestamp=" + lastTimestamp +
				", groupStatus=" + groupStatus +
				", typeOfViolaiton=" + typeOfViolaiton +
				", licensePlate='" + licensePlate + '\'' +
				'}';
	}
}
