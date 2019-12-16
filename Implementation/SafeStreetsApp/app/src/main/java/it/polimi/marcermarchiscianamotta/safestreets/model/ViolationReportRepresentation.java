package it.polimi.marcermarchiscianamotta.safestreets.model;

import com.google.firebase.Timestamp;

import it.polimi.marcermarchiscianamotta.safestreets.util.ViolationEnum;

public class ViolationReportRepresentation {
	private String userUid;
	private String licencePlate;
	private String description;
	private Double latitude;
	private Double longitude;
	private String locality;
	private Timestamp timestamp;
	private ViolationEnum typeOfViolation;

	public ViolationReportRepresentation(String userUid, String licencePlate, String description, Double latitude, Double longitude, String locality, Timestamp timestamp, ViolationEnum typeOfViolation) {
		this.userUid = userUid;
		this.licencePlate = licencePlate;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
		this.locality = locality;
		this.timestamp = timestamp;
		this.typeOfViolation = typeOfViolation;
	}

	public String getUserUid() {
		return userUid;
	}

	public String getLicencePlate() {
		return licencePlate;
	}

	public String getDescription() {
		return description;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getLocality() {
		return locality;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getTypeOfViolation() {
		return typeOfViolation.toString();
	}
}
